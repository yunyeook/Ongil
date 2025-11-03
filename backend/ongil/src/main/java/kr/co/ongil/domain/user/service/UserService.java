package kr.co.ongil.domain.user.service;

import kr.co.ongil.domain.user.dto.request.UpdateUserRequest;
import kr.co.ongil.domain.user.dto.response.UpdateUserResponse;
import kr.co.ongil.domain.user.dto.response.UserResponse;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.repository.RefreshTokenRepository;
import kr.co.ongil.global.security.jwt.JwtUtil;
import kr.co.ongil.global.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FileUtil fileUtil;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public UserResponse getMe(Integer userId) {
        // 사용자 조회 (삭제된 사용자는 @SQLRestriction에 의해 자동으로 필터링됨)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        log.info("사용자 정보 조회 완료: userId={}", userId);

        return UserResponse.from(user);
    }

    @Transactional
    public UpdateUserResponse updateMe(Integer userId, UpdateUserRequest request) {
        // 사용자 조회 (삭제된 사용자는 @SQLRestriction에 의해 자동으로 필터링됨)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 전화번호 변경 시 인증 토큰 검증
        String phoneNumberToUpdate = null;
        if (StringUtils.hasText(request.getPhoneNumber()) && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            // 전화번호가 변경되는 경우 verificationToken 필수
            if (!StringUtils.hasText(request.getVerificationToken())) {
                throw new BusinessException(ErrorCode.VERIFICATION_TOKEN_REQUIRED);
            }

            // 인증 토큰 검증
            if (!jwtUtil.validateToken(request.getVerificationToken())) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }

            // 토큰에서 전화번호 추출 및 검증
            String verifiedPhoneNumber = jwtUtil.getPhoneNumberFromToken(request.getVerificationToken());
            if (!request.getPhoneNumber().equals(verifiedPhoneNumber)) {
                throw new BusinessException(ErrorCode.PHONE_NUMBER_MISMATCH);
            }

            // 전화번호 중복 확인
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
            }

            phoneNumberToUpdate = request.getPhoneNumber();
            log.info("전화번호 변경: userId={}, 이전번호={}, 새번호={}", userId, user.getPhoneNumber(), phoneNumberToUpdate);
        }

        // 프로필 이미지 처리
        String profileImagePath = null;
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            profileImagePath = fileUtil.saveProfileImage(request.getProfileImage());
        }

        // 사용자 정보 업데이트
        user.updateProfile(
                request.getName(),
                request.getBirth(),
                phoneNumberToUpdate,
                profileImagePath
        );

        log.info("사용자 정보 수정 완료: userId={}", userId);

        return UpdateUserResponse.from(UserResponse.from(user));
    }

    @Transactional
    public void deleteMe(Integer userId) {
        // 사용자 조회 (삭제된 사용자는 @SQLRestriction에 의해 자동으로 필터링됨)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 소프트 삭제
        user.softDelete();

        // 리프레시 토큰 삭제 (모든 세션 무효화)
        refreshTokenRepository.deleteAllTokensForUser(userId);

        log.info("사용자 계정 삭제 완료: userId={}", userId);
    }
}
