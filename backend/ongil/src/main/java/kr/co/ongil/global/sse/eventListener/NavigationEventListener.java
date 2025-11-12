package kr.co.ongil.global.sse.eventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.sse.event.NavigationEvent;
import kr.co.ongil.global.sse.sevice.SSEService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NavigationEventListener {

    private final RelationshipRepository relationshipRepository;
    private final SSEService sseService;

    @EventListener
    public void handleNavigationEvent(NavigationEvent event) {
        Set<Integer> targets = new HashSet<>();

        // 1. 기본적으로 환자의 모든 보호자 추가
        List<User> guardians = relationshipRepository.findGuardiansByPatientId(event.patientId());
        guardians.forEach(g -> targets.add(g.getId()));

        // 2. 환자 본인도 포함 (보호자가 시작한 경우 환자도 수신해야 함)
        targets.add(event.patientId());

        // 3. 이벤트 발신자(initiator)는 제외하고 각 대상자에게 전송
        for (Integer targetId : targets) {
            if (!targetId.equals(event.initiatorId())) {
                sseService.sendNavigationUpdate(targetId, event);
            }
        }

        log.info(
            "길안내 SSE 전송 완료: patientId={}, initiatorId={}, userType={}, 수신자수={}, status={}",
            event.patientId(),
            event.initiatorId(),
            event.userType(), //
            targets.size() - 1,
            event.status()
        );
    }
}
