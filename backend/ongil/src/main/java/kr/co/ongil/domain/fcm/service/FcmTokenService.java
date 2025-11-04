package kr.co.ongil.domain.fcm.service;

import java.util.List;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import kr.co.ongil.domain.fcm.entity.FcmToken;
import kr.co.ongil.domain.fcm.repository.FcmTokenRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    /**
     * FCM 토큰 저장
     */
    @Transactional
    public FcmToken saveToken(Integer userId, String token) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // DB없으면 저장.
        return fcmTokenRepository.findByToken(token)
            .orElseGet(() -> fcmTokenRepository.save(FcmToken.builder()
                .user(user)
                .token(token)
                .build()));
    }

    public FcmToken getTokenByUserId(Integer userId) {
        return fcmTokenRepository.findByUserId(userId).get();
    }
    public List<FcmToken> getTokensByUserId(Integer userId) {
        return fcmTokenRepository.findAllByUserId(userId);
    }

    @Transactional
    public void deleteAllTokensByUserId(Integer userId) {
        fcmTokenRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public void deleteTokenByValue(String token) {
        fcmTokenRepository.findByToken(token).ifPresent(fcmTokenRepository::delete);
    }
}
