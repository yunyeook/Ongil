package kr.co.ongil.domain.patient.abnormal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.co.ongil.domain.patient.abnormal.entity.Abnormal;
import kr.co.ongil.domain.patient.abnormal.entity.AbnormalType;
import kr.co.ongil.domain.patient.dashboard.dto.AbnormalStatisticsDto;
import kr.co.ongil.domain.patient.safezone.entity.SafeZoneLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AbnormalRepository extends JpaRepository<Abnormal, Integer> {

    @Query("SELECT a FROM Abnormal a WHERE a.patient.id = :patientId " +
        "AND (:type IS NULL OR a.abnormalType = :type) " +
        "AND (:level IS NULL OR a.safeZoneLevel = :level) " +
        "AND a.createdAt BETWEEN :from AND :to " +
        "ORDER BY a.createdAt DESC")
    Page<Abnormal> findAbnormalsByPatientAndFilters(
        @Param("patientId") Integer patientId,
        @Param("type") AbnormalType type,
        @Param("level") SafeZoneLevel level,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        Pageable pageable
    );

    @Query(value = """
    SELECT
        grouped.patient_id,
        jsonb_object_agg(grouped.safe_zone_level, grouped.cnt) 
            FILTER (WHERE grouped.abnormal_type = 'SAFEZONE_EXIT') as safezone_exit_by_level,
        SUM(CASE WHEN grouped.abnormal_type = 'WANDER' THEN grouped.cnt ELSE 0 END) as wander_count,
        SUM(CASE WHEN grouped.abnormal_type = 'DEVIATE_FROM_THE_PATH' THEN grouped.cnt ELSE 0 END) as path_count
    FROM (
        SELECT
            patient_id,
            abnormal_type,
            safe_zone_level,
            COUNT(*) as cnt
        FROM abnormal
        WHERE created_at >= :startDate
        GROUP BY patient_id, abnormal_type, safe_zone_level
    ) AS grouped
    GROUP BY grouped.patient_id
    """, nativeQuery = true)
    List<AbnormalStatisticsDto> getStatistics(@Param("startDate") LocalDate startDate);

    //  환자 ID와 이상탐지 ID로 조회
    @Query("SELECT a FROM Abnormal a WHERE a.id = :abnormalId AND a.patient.id = :patientId")
    Optional<Abnormal> findByIdAndPatientId(
        @Param("abnormalId") Integer abnormalId,
        @Param("patientId") Integer patientId
    );
}