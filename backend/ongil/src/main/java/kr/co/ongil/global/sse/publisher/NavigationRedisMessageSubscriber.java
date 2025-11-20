package kr.co.ongil.global.sse.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.sse.event.NavigationEvent;
import kr.co.ongil.global.sse.sevice.SSEService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Navigation Redis Pub/Sub 메시지 구독자
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NavigationRedisMessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SSEService sseService;
    private final RelationshipRepository relationshipRepository;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 1. JSON 메시지를 NavigationEvent로 변환
            String messageBody = new String(message.getBody());
            NavigationEvent event = objectMapper.readValue(
                messageBody,
                NavigationEvent.class
            );

            log.debug("Navigation Redis Pub/Sub 수신: patientId={}, status={}",
                event.patientId(), event.status()
            );

            // 2. 전송 대상 결정
            Set<Integer> targets = new HashSet<>();

            // 환자의 모든 보호자 추가
            List<User> guardians = relationshipRepository
                .findGuardiansByPatientId(event.patientId());
            guardians.forEach(g -> targets.add(g.getId()));

            // 환자 본인도 포함
            targets.add(event.patientId());

            // 3. 이벤트 발신자를 제외하고 각 대상자에게 전송
            int sentCount = 0;
            for (Integer targetId : targets) {
                if (!targetId.equals(event.initiatorId())) {
                    // 이 서버에 연결된 사용자에게만 전송됨
                    sseService.sendNavigationUpdate(targetId, event);
                    sentCount++;
                }
            }

            log.debug("Navigation SSE 브로드캐스트 완료: patientId={}, status={}, 전송시도={}",
                event.patientId(),
                event.status(),
                sentCount
            );

        } catch (Exception e) {
            log.error("Navigation Redis Pub/Sub 메시지 처리 실패", e);
        }
    }
}