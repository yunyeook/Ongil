package kr.co.ongil.domain.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FcmTokenRedisService fcmTokenRedisService;
    private final FcmTokenService fcmTokenService;

    public void registerFcmToken(Integer userId, String token) {
        if (token == null || token.isBlank()) return;
        //DB 저장
       fcmTokenService.saveToken(userId,token);

        // Redis 저장
        fcmTokenRedisService.saveToken(userId, token);
    }

    /**
     * userId로 알림 전송
     */
    public void sendNotificationToUser(Integer userId, String title, String body) {
        // Redis에서 조회
        String token = fcmTokenRedisService.getToken(userId);
        // Redis에 없으면 DB 조회 & Redis 저장
        if(token==null){
            token=fcmTokenService.getTokenByUserId(userId).getToken();
            if(token !=null){
                fcmTokenRedisService.saveToken(userId, token);
            }
        }

        if (token.isEmpty()) {
            log.warn("FCM 토큰 없음 - userId={}", userId);
            return;
        }

            sendNotification(token, title, body);
    }

    /**
     * fcm token 으로 알림 전송
     */
    public void sendNotification(String targetToken, String title, String body) {
        try {
            Message message = Message.builder()
                .setToken(targetToken)
                .putData("title", title)
                .putData("body", body)
                .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("푸시 알림 전송 성공: {}", response);
        } catch (Exception e) {
            log.error("푸시 알림 전송 실패", e);
        }
    }

    public void deleteFcmToken(Integer userId) {
        fcmTokenService.deleteAllTokensByUserId(userId);
        fcmTokenRedisService.deleteAllTokens(userId);
    }
}
