package kr.co.ongil.domain.auth.service;

import kr.co.ongil.domain.auth.dto.request.RegisterRequest;
import kr.co.ongil.domain.auth.dto.request.LoginRequest;
import kr.co.ongil.domain.auth.dto.request.RefreshRequest;
import kr.co.ongil.domain.auth.dto.response.LoginResponse;
import kr.co.ongil.domain.auth.dto.response.RefreshResponse;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.entity.Provider;
import kr.co.ongil.domain.user.entity.UserType;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.util.FileUtil;
import kr.co.ongil.global.security.jwt.JwtUtil;
import kr.co.ongil.global.repository.RefreshTokenRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final FileUtil fileUtil;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public void register(RegisterRequest request) {

        // 중복 확인
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_MEMBER);
        }

        // 프로필 이미지 처리
        String profileImagePath = fileUtil.saveProfileImage(request.getProfileImage());

        // User 엔티티 생성
        User user = User.builder()
                .provider(Provider.fromString(request.getProvider()))
                .providerMemberId(request.getProviderMemberId())
                .name(request.getName())
                .birth(request.getBirth())
                .phoneNumber(request.getPhoneNumber())
                .password(request.getPassword()) // 암호화는 일단 생략
                .userType(UserType.fromString(request.getUserType()))
                .profileImage(profileImagePath)
                .build();

        // 사용자 저장
        User savedUser = userRepository.save(user);
    }

    public RefreshResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // 리프레시 토큰 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰 타입 확인
        String tokenType = jwtUtil.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰에서 사용자 ID 추출
        Integer userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 리프레시 토큰 소비 시도
        if (!refreshTokenRepository.consumeRefreshToken(userId, refreshToken)) {

            log.warn("잠재적인 리프레시 토큰 재사용 시도 감지: userId={}", userId);

            // 모든 리프레시 토큰 삭제 (보안 조치)
            refreshTokenRepository.deleteAllTokensForUser(userId);

            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // 사용자 존재 확인 (삭제된 사용자는 @SQLRestriction에 의해 자동으로 필터링됨)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 새로운 토큰 생성 (userType 포함)
        String userType = user.getUserType().name();
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhoneNumber(), userType);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhoneNumber(), userType);

        // Redis에 새로운 리프레시 토큰 저장 (기존 토큰 덮어쓰기)
        refreshTokenRepository.storeRefreshToken(userId, newRefreshToken, jwtUtil.getRefreshTokenExpiration());

        log.info("토큰 재발급 완료: userId={}", userId);

        return RefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(String accessToken) {
        // 액세스 토큰 검증
        if (!jwtUtil.validateToken(accessToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰 타입 확인
        String tokenType = jwtUtil.getTokenType(accessToken);
        if (!"access".equals(tokenType)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰에서 사용자 ID 추출
        Integer userId = jwtUtil.getUserIdFromToken(accessToken);

        // 액세스 토큰의 남은 만료 시간 계산
        long remainingExpiration = jwtUtil.getRemainingExpiration(accessToken);

        // 액세스 토큰을 블랙리스트에 추가 (남은 만료 시간만큼만 유지)
        if (remainingExpiration > 0) {
            refreshTokenRepository.addAccessTokenToBlacklist(accessToken, remainingExpiration);
        }

        // 리프레시 토큰 삭제 (모든 세션 무효화)
        refreshTokenRepository.deleteAllTokensForUser(userId);

        log.info("로그아웃 완료: userId={}", userId);
    }

}
