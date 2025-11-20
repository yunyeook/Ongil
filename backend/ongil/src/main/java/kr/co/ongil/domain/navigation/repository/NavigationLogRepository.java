package kr.co.ongil.domain.navigation.repository;

import kr.co.ongil.domain.navigation.entity.NavigationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NavigationLogRepository extends JpaRepository<NavigationLog, Long> {

    Optional<NavigationLog> findById(Integer navigationId);

    Page<NavigationLog> findByPatientIdOrderByStartedAtDesc(Integer patientId, Pageable pageable);

    List<NavigationLog> findByPatientIdAndStartedAtBetween(
        Integer patientId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    // 성공한 길안내 수
    @Query("SELECT COUNT(l) FROM NavigationLog l WHERE l.patientId = :patientId AND l.isSuccessful = true")
    Long countSuccessfulNavigationsByPatient(@Param("patientId") Integer patientId);

    // 전체 길안내 수
    @Query("SELECT COUNT(l) FROM NavigationLog l WHERE l.patientId = :patientId")
    Long countTotalNavigationsByPatient(@Param("patientId") Integer patientId);
}