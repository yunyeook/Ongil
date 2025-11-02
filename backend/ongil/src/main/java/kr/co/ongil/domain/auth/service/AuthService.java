package kr.co.ongil.domain.auth.service;

import kr.co.ongil.domain.auth.dto.request.RegisterRequest;
import kr.co.ongil.domain.auth.dto.request.LoginRequest;
import kr.co.ongil.domain.auth.dto.response.LoginResponse;
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

    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도: {}", request.getPhoneNumber());

        // 사용자 조회
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        // 비밀번호 확인
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 소프트 삭제 확인
        if (user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Redis에 토큰 저장
        refreshTokenRepository.storeRefreshToken(user.getId(), refreshToken, jwtUtil.getRefreshTokenExpiration());

        // 사용자 정보 구성
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .birth(user.getBirth())
                .phoneNumber(user.getPhoneNumber())
                .userType(user.getUserType().name())
                .profileImage(user.getProfileImage())
                .build();

        log.info("로그인 성공: {}", user.getPhoneNumber());

        return LoginResponse.builder()
                .user(userInfo)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
