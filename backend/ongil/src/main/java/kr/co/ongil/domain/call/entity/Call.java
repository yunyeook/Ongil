package kr.co.ongil.domain.call.entity;

import jakarta.persistence.*;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * VoIP 통화 세션 엔티티
 * 앱 내 실시간 통화 상태를 관리합니다.
 * 통화가 종료되면 자동으로 CallLog가 생성됩니다.
 */
@Entity
@Table(name = "call")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Call extends BaseEntity {

    /**
     * 발신자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    /**
     * 수신자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /**
     * 통화 유형 (일반, 긴급)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false, length = 20)
    private CallType callType;

    /**
     * 통화 상태 (CREATED, RINGING, CONNECTED, ENDED, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CallStatus status;

    /**
     * VoIP 세션 ID (WebRTC 등에서 사용)
     */
    @Column(name = "session_id", length = 100, unique = true)
    private String sessionId;

    /**
     * 통화 요청 시작 시간
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * 통화 연결 시간
     */
    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    /**
     * 통화 종료 시간
     */
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    /**
     * 통화 시간 (초 단위)
     */
    @Column
    private Integer duration;

    /**
     * 낙관적 락 버전 (동시성 제어)
     */
    @Version
    private Long version;

    // 비즈니스 메서드

    /**
     * 통화 상태를 RINGING으로 변경
     */
    public void ring() {
        this.status = CallStatus.RINGING;
    }

    /**
     * 통화 연결
     */
    public void connect() {
        this.status = CallStatus.CONNECTED;
        this.connectedAt = LocalDateTime.now();
    }

    /**
     * 통화 종료
     * @param finalStatus 최종 상태 (ENDED, CANCELED, REJECTED, etc.)
     */
    public void end(CallStatus finalStatus) {
        this.status = finalStatus;
        this.endedAt = LocalDateTime.now();

        // 통화 시간 계산 (연결된 경우만)
        if (this.connectedAt != null) {
            this.duration = (int) java.time.Duration.between(this.connectedAt, this.endedAt).getSeconds();
        }
    }

    /**
     * 통화가 종료되었는지 확인
     */
    public boolean isEnded() {
        return status == CallStatus.ENDED
            || status == CallStatus.CANCELED
            || status == CallStatus.REJECTED
            || status == CallStatus.FAILED
            || status == CallStatus.MISSED
            || status == CallStatus.DROPPED;
    }

    /**
     * 통화가 타임아웃되었는지 확인
     * @param timeoutSeconds 타임아웃 시간(초)
     * @return 타임아웃 여부
     */
    public boolean isExpired(int timeoutSeconds) {
        if (isEnded()) {
            return false;  // 이미 종료된 통화는 만료 대상이 아님
        }

        if (this.startedAt == null) {
            return false;
        }

        LocalDateTime expiryTime = this.startedAt.plusSeconds(timeoutSeconds);
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * 통화를 타임아웃으로 종료
     * - CREATED, RINGING 상태 → MISSED
     * - CONNECTED 상태는 타임아웃 안됨 (실제 통화 중)
     */
    public void expireCall() {
        if (status == CallStatus.CREATED || status == CallStatus.RINGING) {
            end(CallStatus.MISSED);
        }
    }
}
