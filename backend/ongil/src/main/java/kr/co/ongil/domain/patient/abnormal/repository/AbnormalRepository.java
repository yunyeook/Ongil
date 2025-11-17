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
        patient_id as patientId,
        COALESCE(jsonb_object_agg(safe_zone_level, cnt)
            FILTER (WHERE abnormal_type = 'SAFEZONE_EXIT')::text, '{}') as safezoneExitByLevel,
        COALESCE(SUM(CASE WHEN abnormal_type = 'WANDER' THEN cnt ELSE 0 END), 0) as wanderCount,
        COALESCE(SUM(CASE WHEN abnormal_type = 'DEVIATE_FROM_THE_PATH' THEN cnt ELSE 0 END), 0) as pathCount
    FROM (
        SELECT
            patient_id,
            abnormal_type,
            safe_zone_level,
            COUNT(*) as cnt
        FROM abnormal_logs
        WHERE created_at >= :startDate
        GROUP BY patient_id, abnormal_type, safe_zone_level
    ) AS grouped
    GROUP BY patient_id
    """, nativeQuery = true)
        List<AbnormalStatisticsDto> getStatistics(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(a) FROM Abnormal a " +
            "WHERE a.patient.id = :patientId " +
            "AND a.createdAt >= :weekStart " +
            "AND a.abnormalType=:abnormalType")
    Long countThisWeek(@Param("patientId") Integer patientId,
                       @Param("weekStart") LocalDateTime weekStart,
                       @Param("abnormalType")  AbnormalType abnormalType);

    //  환자 ID와 이상탐지 ID로 조회
    @Query("SELECT a FROM Abnormal a WHERE a.id = :abnormalId AND a.patient.id = :patientId")
    Optional<Abnormal> findByIdAndPatientId(
        @Param("abnormalId") Integer abnormalId,
        @Param("patientId") Integer patientId
    );

    // 🆕 시간대별 이상탐지 횟수 조회 (최근 7일)
    @Query(value = """
        SELECT
            CASE
                WHEN EXTRACT(HOUR FROM created_at) BETWEEN 0 AND 5 THEN '00-06'
                WHEN EXTRACT(HOUR FROM created_at) BETWEEN 6 AND 11 THEN '06-12'
                WHEN EXTRACT(HOUR FROM created_at) BETWEEN 12 AND 17 THEN '12-18'
                ELSE '18-24'
            END as timeSlot,
            COUNT(*) as count
        FROM abnormal_logs
        WHERE patient_id = :patientId
        AND created_at >= :startDate
        GROUP BY timeSlot
        """, nativeQuery = true)
    List<Object[]> findCountByTimeSlots(@Param("patientId") Integer patientId,
                                        @Param("startDate") LocalDateTime startDate);

    // 🆕 일별 이상탐지 횟수 조회 (최근 7일)
    @Query(value = """
        SELECT
            DATE(created_at) as date,
            COUNT(*) as count
        FROM abnormal_logs
        WHERE patient_id = :patientId
        AND DATE(created_at) BETWEEN :startDate AND :endDate
        GROUP BY DATE(created_at)
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> findCountByDay(@Param("patientId") Integer patientId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);
}