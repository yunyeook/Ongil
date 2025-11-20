package kr.co.ongil.domain.patient.location.service;

import kr.co.ongil.domain.map.dto.response.CoordinateInfo;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.sse.event.LocationUpdatedEvent;
import kr.co.ongil.global.util.PatientAccessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * GPS 위치 추적 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRedisService locationRedisService;
    private final RelationshipRepository relationshipRepository;
    private final PatientAccessValidator patientAccessValidator;

    private final ApplicationEventPublisher eventPublisher;


    /**
     * GPS 위치 저장 /업데이트
     */
    public void createOrUpdateLocation(CoordinateInfo coordinate, Integer patientId,Integer callerId) {
        // 1. 권한 검증
        patientAccessValidator.validateAccess(patientId,callerId);

        // 2. Redis에 최근 위치 저장
        locationRedisService.saveLocation(patientId,coordinate);

        // 3. 해당 환자의 모든 보호자 조회
        List<User> guardians = relationshipRepository.findGuardiansByPatientId(patientId);

        // 4. 모든 보호자에게 SSE로 브로드캐스트
        eventPublisher.publishEvent(LocationUpdatedEvent.of(patientId, coordinate));

        log.debug("GPS 업데이트 완료 (일반 모드): patientId={}, 보호자 수={}",
            patientId, guardians.size());
    }

    /**
     * 환자의 최근 GPS 위치 조회
     */
    public CoordinateInfo getLocation(Integer patientId, Integer guardianId) {

        // 1. 권한 검증
        patientAccessValidator.validateAccess(patientId,guardianId);


        // 2. 레디스에서 환자의 위치 정보 조회

       return locationRedisService.getLocation(patientId);


    }
}