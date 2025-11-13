package kr.co.ongil.global.eventListener;

import java.util.List;
import kr.co.ongil.domain.patient.location.service.LocationSSEService;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
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
    private final LocationSSEService locationSSEService;

    @EventListener
    public void handleLocationUpdate(LocationUpdatedEvent event) {
        // 1. 해당 환자의 보호자 조회
        List<User> guardians = relationshipRepository.findGuardiansByPatientId(event.getPatientId());

        // 2. 모든 보호자에게 SSE 전송
        guardians.forEach(guardian -> {
            locationSSEService.sendGPSUpdate(guardian.getId(), event);
        });

        log.info("GPS 업데이트 SSE 전송 완료: patientId={}, guardianCount={}",
            event.getPatientId(), guardians.size());
    }
}