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
import kr.co.ongil.domain.patient.safezone.entity.SafeZone;
import kr.co.ongil.domain.patient.safezone.repository.SafeZoneRepository;
import kr.co.ongil.global.util.FileUtil;
import kr.co.ongil.global.security.jwt.JwtUtil;
import kr.co.ongil.global.repository.RefreshTokenRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final FileUtil fileUtil;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SafeZoneRepository safeZoneRepository;

    @Transactional
    public void register(RegisterRequest request) {

        // 전화번호 인증 토큰 검증
        if (!jwtUtil.validateToken(request.getVerificationToken())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰에서 전화번호 추출 및 검증
        String verifiedPhoneNumber = jwtUtil.getPhoneNumberFromToken(request.getVerificationToken());
        if (!request.getPhoneNumber().equals(verifiedPhoneNumber)) {
            throw new BusinessException(ErrorCode.PHONE_NUMBER_MISMATCH);
        }

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

        // 환자로 회원가입 시 안전범위 기본값 자동 생성
        if (savedUser.getUserType() == UserType.PATIENT) {
            SafeZone safeZone = SafeZone.builder()
                    .patient(savedUser)
                    .build();
            safeZoneRepository.save(safeZone);
            log.info("환자 안전범위 기본값 생성 완료: patientId={}", savedUser.getId());
        }

        log.info("회원가입 완료: phoneNumber={}, userType={}", request.getPhoneNumber(), request.getUserType());
    }

    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도: phoneNumber={}", request.getPhoneNumber());

        // 1. 사용자 조회
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 3. JWT 토큰 생성
        String userType = user.getUserType().name();
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhoneNumber(), userType);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhoneNumber(), userType);

        // 4. Redis에 리프레시 토큰 저장
        refreshTokenRepository.storeRefreshToken(user.getId(), refreshToken, jwtUtil.getRefreshTokenExpiration());

        log.info("로그인 성공: userId={}, userType={}", user.getId(), userType);

        // 5. 응답 생성
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .birth(user.getBirth())
                .phoneNumber(user.getPhoneNumber())
                .userType(user.getUserType().name())
                .profileImage(user.getProfileImage())
                .build();

        return LoginResponse.builder()
                .user(userInfo)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
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
