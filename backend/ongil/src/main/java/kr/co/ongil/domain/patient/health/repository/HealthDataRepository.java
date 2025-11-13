package kr.co.ongil.domain.patient.health.repository;

import kr.co.ongil.domain.patient.health.entity.HealthData;
import kr.co.ongil.domain.patient.health.entity.HealthDataType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 건강 데이터 레포지토리
 */
public interface HealthDataRepository extends JpaRepository<HealthData, Integer> {

    /**
     * 환자 ID와 측정 시각 범위로 조회 (모든 타입)
     */
    List<HealthData> findByPatientIdAndMeasuredAtBetween(
        Integer patientId,
        LocalDateTime from,
        LocalDateTime to,
        Sort sort
    );

    /**
     * 환자 ID, 데이터 타입, 측정 시각 범위로 조회
     */
    List<HealthData> findByPatientIdAndTypeAndMeasuredAtBetween(
        Integer patientId,
        HealthDataType type,
        LocalDateTime from,
        LocalDateTime to,
        Sort sort
    );

    /**
     * 환자 ID와 데이터 타입으로 최신 데이터 조회
     */
    HealthData findFirstByPatientIdAndTypeOrderByMeasuredAtDesc(
        Integer patientId,
        HealthDataType type
    );

    /**
     * 환자 ID로 모든 건강 데이터 삭제 (환자 삭제 시)
     */
    void deleteAllByPatientId(Integer patientId);
}
