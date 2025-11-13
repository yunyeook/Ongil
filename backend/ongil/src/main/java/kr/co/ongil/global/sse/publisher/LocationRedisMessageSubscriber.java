package kr.co.ongil.global.sse.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.sse.event.LocationUpdatedEvent;
import kr.co.ongil.global.sse.sevice.SSEService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Redis Pub/Sub 메시지 구독자
 *
 * 역할: 다른 서버에서 발행한 위치 업데이트를 받아서
 *      이 서버에 연결된 보호자들에게 SSE로 전송
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocationRedisMessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SSEService sseService;
    private final RelationshipRepository relationshipRepository;

    /**
     * Redis 메시지 수신 시 자동 호출됨
     *
     * @param message Redis에서 받은 메시지
     * @param pattern 구독 패턴 (사용 안 함)
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 1. JSON 메시지를 LocationUpdatedEvent로 변환
            String messageBody = new String(message.getBody());
            LocationUpdatedEvent event = objectMapper.readValue(
                messageBody,
                LocationUpdatedEvent.class
            );

            log.debug("Redis Pub/Sub 수신: patientId={}", event.patientId());

            // 2. 해당 환자의 보호자 목록 조회
            List<User> guardians = relationshipRepository
                .findGuardiansByPatientId(event.patientId());

            // 3. 이 서버에 연결된 보호자들에게만 SSE 전송
            guardians.forEach(guardian -> {
                // SSEService는 이 서버의 emitters만 가지고 있음
                // 따라서 이 서버에 연결된 보호자에게만 전송됨
                sseService.sendGPSUpdate(guardian.getId(), event);
            });

            log.debug("SSE 브로드캐스트 완료: patientId={}, guardianCount={}",
                event.patientId(), guardians.size()
            );

        } catch (Exception e) {
            log.error("Redis Pub/Sub 메시지 처리 실패", e);
        }
    }
}