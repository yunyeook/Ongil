package kr.co.ongil.domain.password.service;

import kr.co.ongil.domain.password.dto.request.PasswordResetRequest;
import kr.co.ongil.domain.password.dto.request.PasswordResetWithTokenRequest;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void resetPassword(Integer userId, PasswordResetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        // 새 비밀번호가 현재 비밀번호와 다른지 확인
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        // 새 비밀번호와 비밀번호 확인 일치 여부 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        // 비밀번호 업데이트
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);

        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    @Transactional
    public void resetPasswordWithToken(PasswordResetWithTokenRequest request) {
        // 인증 토큰 검증
        if (!jwtUtil.validateToken(request.getVerificationToken())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰에서 전화번호 추출
        String phoneNumber = jwtUtil.getPhoneNumberFromToken(request.getVerificationToken());

        // 사용자 조회 (삭제된 사용자는 @SQLRestriction에 의해 자동으로 필터링됨)
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 새 비밀번호와 비밀번호 확인 일치 여부 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        // 비밀번호 업데이트
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);

        log.info("비밀번호 재설정 완료: userId={}, phoneNumber={}", user.getId(), phoneNumber);
    }
}
