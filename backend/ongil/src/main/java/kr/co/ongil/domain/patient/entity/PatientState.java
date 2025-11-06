package kr.co.ongil.domain.patient.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 환자 상태
 * 통화 시점에 환자가 어떤 상태였는지 기록하는 용도로 사용됩니다.
 */
@Getter
@RequiredArgsConstructor
public enum PatientState {

    /**
     * 정상 상태
     */
    NORMAL("정상"),

    /**
     * 길안내 중
     */
    NAVIGATING("길안내 중"),

    /**
     * 이상행동 감지
     */
    ABNORMAL("이상행동 감지");

    private final String description;
}
