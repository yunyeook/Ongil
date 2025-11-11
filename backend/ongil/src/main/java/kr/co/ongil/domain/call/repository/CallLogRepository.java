package kr.co.ongil.domain.call.repository;

import kr.co.ongil.domain.call.entity.CallLog;
import kr.co.ongil.domain.call.entity.CallType;
import kr.co.ongil.domain.patient.dashboard.dto.CallStatisticsDto;
import kr.co.ongil.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 통화 로그 레포지토리
 */
public interface CallLogRepository extends JpaRepository<CallLog, Integer> {

    /**
     * 사용자가 참여한 통화 기록 조회 (발신자 또는 수신자, 페이징)
     */
    @Query("SELECT cl FROM CallLog cl WHERE cl.caller = :user OR cl.receiver = :user " +
           "ORDER BY cl.startedAt DESC")
    Page<CallLog> findByUser(@Param("user") User user, Pageable pageable);

    /**
     * 사용자가 참여한 긴급 통화 기록 조회
     */
    @Query("SELECT cl FROM CallLog cl WHERE (cl.caller = :user OR cl.receiver = :user) " +
           "AND cl.callType = :callType ORDER BY cl.startedAt DESC")
    List<CallLog> findByUserAndCallType(@Param("user") User user, @Param("callType") CallType callType);

    /**
     * 특정 기간 내 통화 기록 조회
     */
    @Query("SELECT cl FROM CallLog cl WHERE (cl.caller = :user OR cl.receiver = :user) " +
           "AND cl.startedAt BETWEEN :startDate AND :endDate ORDER BY cl.startedAt DESC")
    List<CallLog> findByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 사용자 간 통화 기록 조회
     */
    @Query("SELECT cl FROM CallLog cl " +
           "WHERE ((cl.caller = :user1 AND cl.receiver = :user2) OR (cl.caller = :user2 AND cl.receiver = :user1)) " +
           "ORDER BY cl.startedAt DESC")
    List<CallLog> findCallLogsBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT cl.caller AS patientId, COUNT(*) AS callCount FROM CallLog cl " +
            "WHERE cl.createdAt >= :startDate " +
            "AND cl.callType = :callType " +
            "GROUP BY cl.caller")
    List<CallStatisticsDto> findCallStatisticsByUser(@Param("startDate") LocalDate startDate,
    @Param("callType") CallType callType);

    /**
     * 긴급 통화 기록 조회 (전체, 최신순)
     */
    List<CallLog> findByCallTypeOrderByStartedAtDesc(CallType callType);
}
