package kr.co.ongil.domain.patient.dashboard.repository;

import kr.co.ongil.domain.patient.dashboard.entity.DashboardCalc;
import kr.co.ongil.domain.patient.safezone.entity.SafeZone;
import kr.co.ongil.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DashboardRepository extends JpaRepository<DashboardCalc, Integer> {
    Optional<DashboardCalc> findByPatient(User patient);

    Optional<DashboardCalc> findByPatientId(Integer patientId);

    /**
     * 환자 ID로 대시보드 데이터 존재 여부 확인 (성능 최적화)
     * hasDashboardData()에서 사용
     */
    boolean existsByPatientId(Integer patientId);

    /**
     * 환자의 최신 대시보드 데이터 1개 조회 (createdAt 기준)
     * 실제 최신 데이터가 필요한 곳에서 사용
     */
    Optional<DashboardCalc> findTopByPatientIdOrderByCreatedAtDesc(Integer patientId);

    /**
     * 환자의 최신 대시보드 데이터 2개 조회 (이번 주 + 지난 주 비교용)
     */
    List<DashboardCalc> findTop2ByPatientIdOrderByCreatedAtDesc(Integer patientId);

    void deleteByCreatedAtBefore(LocalDateTime createdAtBefore);
}
