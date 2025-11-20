package kr.co.ongil.domain.patient.insight.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 인사이트 분석 기간 타입
 */
@Getter
@RequiredArgsConstructor
public enum PeriodType {

    /**
     * 주간 리포트 (월~일)
     */
    WEEKLY("주간"),

    /**
     * 월간 리포트 (1일~말일)
     */
    MONTHLY("월간");

    private final String description;
}
