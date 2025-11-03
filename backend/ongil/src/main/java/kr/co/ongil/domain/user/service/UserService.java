package kr.co.ongil.domain.user.service;

import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void deleteMe(Integer userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 삭제된 사용자인지 확인
        if (user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 소프트 삭제
        user.softDelete();

        // 리프레시 토큰 삭제 (모든 세션 무효화)
        refreshTokenRepository.deleteAllTokensForUser(userId);

        log.info("사용자 계정 삭제 완료: userId={}", userId);
    }
}
