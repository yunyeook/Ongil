package kr.co.ongil.domain.patient.dashboard.repository;

import kr.co.ongil.domain.patient.dashboard.entity.DashboardCalc;
import kr.co.ongil.domain.patient.safezone.entity.SafeZone;
import kr.co.ongil.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DashboardRepository extends JpaRepository<DashboardCalc, Integer> {
    Optional<DashboardCalc> findByPatient(User patient);

    Optional<DashboardCalc> findByPatientId(Integer patientId);

    List<DashboardCalc> findTop2ByPatientIdOrderByCreatedAtDesc(Integer patientId);
}
