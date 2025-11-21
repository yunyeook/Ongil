package kr.co.ongil.domain.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;

import kr.co.ongil.domain.fcm.dto.request.FcmRetryMessage;
import kr.co.ongil.domain.fcm.entity.FcmToken;
import kr.co.ongil.domain.notification.entity.Notification;
import kr.co.ongil.domain.relationship.entity.Relationship;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.global.config.FcmRabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FcmTokenRedisService fcmTokenRedisService;
    private final FcmTokenService fcmTokenService;
    private final RelationshipRepository relationshipRepository;
    private final RabbitTemplate rabbitTemplate;

    public void registerFcmToken(Integer userId, String token) {
        if (token == null || token.isBlank())
            return;
        // DB 저장
        fcmTokenService.saveToken(userId, token);

        // Redis 저장
        fcmTokenRedisService.saveToken(userId, token);
    }

    /**
     * fcm token 으로 알림 전송 : FCM에 관련된 테이블 ID, notification테이블 ID 전송
     */
    public void sendNotification(Notification notification, Integer relatedTableId) {
        Integer senderId = notification.getSender().getId();
        Integer receiverId = notification.getReceiver().getId();

        // 1. 관계 검증
        Relationship relationship = relationshipRepository.findByTwoUserIds(senderId, receiverId).orElse(null);
        if (relationship == null) {
            log.warn("FCM 발송 중단: 등록되지 않은 관계 (senderId={}, receiverId={})", senderId, receiverId);
            return;
        }

        // 2. 토큰 조회
        String token = resolveToken(receiverId);
        if (token == null || token.isBlank()) {
            log.warn("FCM 발송 중단: 수신자(userId={})의 FCM 토큰이 없습니다. 이벤트를 건너뜁니다.", receiverId);
            return;
        }

        // 3. 데이터 구성
        Map<String, String> data = new HashMap<>();
        data.put("title", notification.getTitle());
        data.put("content", notification.getContent());
        data.put("type", notification.getType().name());
        data.put("senderId", senderId.toString());
        data.put("receiverId", receiverId.toString());
        data.put("notificationId", notification.getId().toString());
        data.put("relatedTableId", relatedTableId != null ? relatedTableId.toString() : "");

        // 4. 발송
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("푸시 알림 전송 성공: {}", response);
        } catch (FirebaseMessagingException e) {
            handleFcmException(e, token, data, "NOTIFICATION", receiverId);
        } catch (Exception e) {
            log.error("알 수 없는 푸시 알림 전송 실패 - 재시도 큐로 전송 (receiverId={})", receiverId, e);
            pushToRetryQueue(token, data, "NOTIFICATION");
        }
    }

    /**
     * VoIP 통화 알림 전송 (Android 앱 깨우기용)
     * 백그라운드 상태의 앱을 깨워서 WebSocket 연결을 시작.
     */
    public void sendCallNotification(
            Integer receiverId,
            Integer callerId,
            String callerName,
            Integer callId,
            String sessionId,
            String callType) {

        // 1. 토큰 조회
        String token = resolveToken(receiverId);
        if (token == null || token.isBlank()) {
            log.warn("통화 FCM 발송 중단: 수신자(userId={})의 FCM 토큰이 없습니다.", receiverId);
            return;
        }

        // 2. 데이터 구성
        Map<String, String> data = new HashMap<>();
        data.put("type", "INCOMING_CALL");
        data.put("callId", callId.toString());
        data.put("sessionId", sessionId);
        data.put("callerId", callerId.toString());
        data.put("callerName", callerName);
        data.put("callType", callType);

        // 3. 발송
        try {
            // data-only 메시지 (백그라운드에서 앱 깨우기)
            Message message = Message.builder()
                    .setToken(token)
                    // Android 설정: HIGH 우선순위 + 30초 TTL
                    .setAndroidConfig(com.google.firebase.messaging.AndroidConfig.builder()
                            .setPriority(com.google.firebase.messaging.AndroidConfig.Priority.HIGH)
                            .setTtl(30000L) // 30초 (밀리초)
                            .build())
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("통화 푸시 알림 전송 성공 - receiverId={}, callId={}, response={}", receiverId, callId, response);

        } catch (FirebaseMessagingException e) {
            handleFcmException(e, token, data, "CALL", receiverId);
        } catch (Exception e) {
            log.error("알 수 없는 통화 푸시 알림 전송 실패 - 재시도 큐로 전송 (receiverId={}, callId={})", receiverId, callId, e);
            pushToRetryQueue(token, data, "CALL");
        }
    }

    /**
     * 토큰 조회 및 캐싱 로직 (중복 제거 및 빈 문자열 캐싱 방지)
     */
    private String resolveToken(Integer userId) {
        // 1. Redis에서 우선 조회
        String token = fcmTokenRedisService.getToken(userId);

        // 2. Redis에 없으면 DB 조회
        if (token == null || token.isBlank()) {
            FcmToken tk = fcmTokenService.getTokenByUserId(userId);
            if (tk != null && tk.getToken() != null && !tk.getToken().isBlank()) {
                token = tk.getToken();
                // 유효한 토큰일 때만 Redis에 저장 (빈 문자열 캐싱 방지)
                fcmTokenRedisService.saveToken(userId, token);
            } else {
                token = null; // 명시적으로 null 반환
            }
        }
        return token;
    }

    /**
     * RabbitMQ를 통한 재시도 전송
     */
    public void sendRetry(FcmRetryMessage retryMessage) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(retryMessage.token())
                    .putAllData(retryMessage.data());

            if ("CALL".equals(retryMessage.messageType())) {
                messageBuilder.setAndroidConfig(com.google.firebase.messaging.AndroidConfig.builder()
                        .setPriority(com.google.firebase.messaging.AndroidConfig.Priority.HIGH)
                        .setTtl(30000L)
                        .build());
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("FCM 재시도 전송 성공 (count: {}): {}", retryMessage.retryCount(), response);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();
            if (errorCode == MessagingErrorCode.INVALID_ARGUMENT ||
                    errorCode == MessagingErrorCode.UNREGISTERED) {
                log.warn("FCM 재시도 중 영구적 에러 발생 - 재시도 중단: {}", errorCode);
                return;
            }
            retryOrLogFailure(retryMessage, e);
        } catch (Exception e) {
            retryOrLogFailure(retryMessage, e);
        }
    }

    private void retryOrLogFailure(FcmRetryMessage retryMessage, Exception e) {
        log.error("FCM 재시도 전송 실패 (count: {})", retryMessage.retryCount(), e);
        if (retryMessage.retryCount() < 3) {
            rabbitTemplate.convertAndSend(
                    FcmRabbitConfig.FCM_RETRY_EXCHANGE,
                    FcmRabbitConfig.FCM_RETRY_ROUTING_KEY,
                    retryMessage.incrementRetry());
        } else {
            log.error("FCM 최대 재시도 횟수 초과 - 발송 중단 (token: {})", retryMessage.token());
        }
    }

    private void handleFcmException(FirebaseMessagingException e, String token, Map<String, String> data, String type,
            Integer receiverId) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        // 1. 영구적인 토큰 에러 (재시도 무의미)
        if (errorCode == MessagingErrorCode.INVALID_ARGUMENT ||
                errorCode == MessagingErrorCode.UNREGISTERED) {
            log.warn("FCM 영구적 에러 발생 - 재시도 건너뜀 (receiverId={}, errorCode={})", receiverId, errorCode);
            // 해당 유저의 잘못된 토큰 삭제 처리
            deleteFcmToken(receiverId);
            return;
        }

        // 2. 일시적인 오류는 재시도 큐로 전송
        log.error("FCM 일시적 오류 발생 - 재시도 큐로 전송 (receiverId={}, errorCode={})", receiverId, errorCode);
        pushToRetryQueue(token, data, type);
    }

    private void pushToRetryQueue(String token, Map<String, String> data, String messageType) {
        if (token == null || token.isBlank())
            return;
        FcmRetryMessage retryMessage = new FcmRetryMessage(token, data, messageType);
        rabbitTemplate.convertAndSend(
                FcmRabbitConfig.FCM_RETRY_EXCHANGE,
                FcmRabbitConfig.FCM_RETRY_ROUTING_KEY,
                retryMessage);
    }

    public void deleteFcmToken(Integer userId) {
        fcmTokenService.deleteAllTokensByUserId(userId);
        fcmTokenRedisService.deleteAllTokens(userId);
    }
}
