package kr.co.ongil.global.sse.eventListener;

import java.util.List;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.global.sse.event.LocationUpdatedEvent;
import kr.co.ongil.global.sse.sevice.SSEService;
import kr.co.ongil.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationEventListener {

    private final RelationshipRepository relationshipRepository;
    private final SSEService SSEService;

    @EventListener
    public void handleLocationUpdate(LocationUpdatedEvent event) {
        // 1. 해당 환자의 보호자 조회
        List<User> guardians = relationshipRepository.findGuardiansByPatientId(event.getPatientId());

        // 2. 모든 보호자에게 SSE 전송
        guardians.forEach(guardian -> {
            SSEService.sendGPSUpdate(guardian.getId(), event);
        });

        log.info("GPS 업데이트 SSE 전송 완료: patientId={}, guardianCount={}",
            event.getPatientId(), guardians.size());
    }
}