package kr.co.ongil.domain.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import kr.co.ongil.domain.notification.entity.Notification;
import kr.co.ongil.domain.relationship.entity.Relationship;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.domain.relationship.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FcmTokenRedisService fcmTokenRedisService;
    private final FcmTokenService fcmTokenService;
    private final RelationshipRepository relationshipRepository;

    public void registerFcmToken(Integer userId, String token) {
        if (token == null || token.isBlank()) return;
        //DB 저장
        fcmTokenService.saveToken(userId,token);

        // Redis 저장
        fcmTokenRedisService.saveToken(userId, token);
    }

    /**
     * fcm token 으로 알림 전송 : FCM에 관련된 테이블 ID, notification테이블 ID 전송
     */
    public void sendNotification(Notification notification, Integer relatedTableId) {
        try {
            Integer senderId = notification.getSender().getId();
            Integer receiverId = notification.getReceiver().getId();

            Relationship relationship= relationshipRepository.findByTwoUserIds(senderId, receiverId).orElse(null);
            if(relationship==null) {
                log.warn("등록되지 않은 관계라 알림 발송 X");
                return;
            }
            // Redis에서 조회
            String token = fcmTokenRedisService.getToken(receiverId);
            // Redis에 없으면 DB 조회 & Redis 저장
            if(token==null){
                token=fcmTokenService.getTokenByUserId(receiverId).getToken();
                if(token !=null){
                    fcmTokenRedisService.saveToken(receiverId, token);
                }
            }

            if (token.isEmpty()) {
                log.warn("FCM 토큰 없음 - userId={}", receiverId);
                return;

            }
            Message message = Message.builder()
                .setToken(token)
                .putData("title", notification.getTitle())
                .putData("content",notification.getContent())
                .putData("type", notification.getType().name())
                .putData("senderId",senderId.toString())
                .putData("receiverId",receiverId.toString())
                .putData("notificationId",notification.getId().toString())
                .putData("relatedTableId",relatedTableId!=null?relatedTableId.toString():"")
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
