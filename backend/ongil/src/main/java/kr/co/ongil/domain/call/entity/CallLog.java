package kr.co.ongil.domain.call.entity;

import jakarta.persistence.*;
import kr.co.ongil.domain.patient.entity.PatientState;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 통화 로그 엔티티
 * VoIP 통화와 기본 전화 통화 기록을 모두 저장합니다.
 * - VoIP: CallService가 Call 종료 시 자동 생성
 * - 기본 전화: 클라이언트가 통화 종료 후 직접 기록
 */
@Entity
@Table(name = "call_logs")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CallLog extends BaseEntity {

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
     * 통화 출처 (앱 내 통화 or 시스템 다이얼러)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CallSource source;

    /**
     * 통화 시점의 환자 상태 (정상, 길안내 중, 이상행동)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "patient_state", nullable = false, length = 20)
    private PatientState patientState;

    /**
     * 환자 위치 (JSON 형식: {"latitude": 37.123, "longitude": 127.456})
     */
    @Column(name = "patient_location", columnDefinition = "TEXT")
    private String patientLocation;

    /**
     * 통화 시작 시간
     */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

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
     * 메모 (보호자가 추가 가능)
     */
    @Column(columnDefinition = "TEXT")
    private String memo;

    // 비즈니스 메서드

    /**
     * 메모 추가/수정
     */
    public void updateMemo(String memo) {
        this.memo = memo;
    }

    /**
     * 긴급 통화 여부 확인
     */
    public boolean isEmergencyCall() {
        return this.callType == CallType.EMERGENCY;
    }

    /**
     * VoIP 통화 여부 확인
     */
    public boolean isVoipCall() {
        return this.source == CallSource.APP;
    }
}
