package kr.co.ongil.domain.call.repository;

import kr.co.ongil.domain.call.entity.Call;
import kr.co.ongil.domain.call.entity.CallStatus;
import kr.co.ongil.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * VoIP 통화 세션 레포지토리
 */
public interface CallRepository extends JpaRepository<Call, Integer> {

    /**
     * 세션 ID로 통화 조회
     */
    Optional<Call> findBySessionId(String sessionId);

    /**
     * 사용자가 참여 중인 활성 통화 조회 (발신자 또는 수신자)
     */
    @Query("SELECT c FROM Call c WHERE (c.caller = :user OR c.receiver = :user) " +
           "AND c.status NOT IN ('ENDED', 'CANCELED', 'REJECTED', 'FAILED', 'MISSED', 'DROPPED')")
    Optional<Call> findActiveCallByUser(@Param("user") User user);

    /**
     * 특정 상태의 통화 목록 조회
     */
    List<Call> findByStatus(CallStatus status);

    /**
     * 사용자 간 진행 중인 통화 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Call c " +
           "WHERE ((c.caller = :user1 AND c.receiver = :user2) OR (c.caller = :user2 AND c.receiver = :user1)) " +
           "AND c.status NOT IN ('ENDED', 'CANCELED', 'REJECTED', 'FAILED', 'MISSED', 'DROPPED')")
    boolean existsActiveCallBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 만료된 통화 조회 (타임아웃 시간 기준)
     * CREATED, RINGING 상태이면서 startedAt이 특정 시간 이전인 통화들
     *
     * @param expiryTime 만료 기준 시간 (현재 시간 - 타임아웃 초)
     * @return 만료된 통화 목록
     */
    @Query("SELECT c FROM Call c WHERE c.status IN ('CREATED', 'RINGING') " +
           "AND c.startedAt < :expiryTime")
    List<Call> findExpiredCalls(@Param("expiryTime") java.time.LocalDateTime expiryTime);
}
