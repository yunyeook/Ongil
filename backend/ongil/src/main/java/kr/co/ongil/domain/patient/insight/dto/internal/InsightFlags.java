package kr.co.ongil.domain.patient.insight.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 룰 기반 위험 신호 플래그 (심각도 점수 기반)
 *
 * 각 플래그는 단순 true/false뿐만 아니라 0-10점의 심각도 점수를 가짐
 *
 * 심각도 분류:
 * - 0-3점: 경미 (정상 또는 가벼운 이상)
 * - 4-6점: 주의 필요
 * - 7-10점: 심각 (즉시 대응 필요)
 *
 * 최종 위험도 평가: 매트릭스 방식
 *
 *            주의(4+) 개수
 *            0    1    2    3+
 * 심각  0    L    L    M    M
 * (7+)  1    L    M    H    H
 * 개수  2+   M    H    H    H
 */
public record InsightFlags(
    @JsonProperty("routine_change_detected")
    boolean routineChangeDetected,

    @JsonProperty("spatial_confusion_detected")
    boolean spatialConfusionDetected,

    @JsonProperty("anxiety_or_risk_escalation")
    boolean anxietyOrRiskEscalation,

    @JsonProperty("physical_condition_drop")
    boolean physicalConditionDrop,

    @JsonProperty("sleep_activity_correlation")
    boolean sleepActivityCorrelation,

    @JsonProperty("panic_response_pattern")
    boolean panicResponsePattern,

    // 각 플래그의 심각도 점수 (0-10점)
    @JsonProperty("routine_change_severity")
    int routineChangeSeverity,

    @JsonProperty("spatial_confusion_severity")
    int spatialConfusionSeverity,

    @JsonProperty("anxiety_escalation_severity")
    int anxietyEscalationSeverity,

    @JsonProperty("physical_drop_severity")
    int physicalDropSeverity,

    @JsonProperty("sleep_activity_severity")
    int sleepActivitySeverity,

    @JsonProperty("panic_response_severity")
    int panicResponseSeverity
) {

    // 심각도 기준
    private static final int SEVERE_THRESHOLD = 7;   // 7-10점: 심각
    private static final int MODERATE_THRESHOLD = 4; // 4-6점: 주의

    public static InsightFlags allNormal() {
        return new InsightFlags(
            false, false, false, false, false, false,
            0, 0, 0, 0, 0, 0
        );
    }

    /**
     * 활성화된 플래그 개수 (참고용)
     */
    public int activeCount() {
        int count = 0;
        if (routineChangeDetected) count++;
        if (spatialConfusionDetected) count++;
        if (anxietyOrRiskEscalation) count++;
        if (physicalConditionDrop) count++;
        if (sleepActivityCorrelation) count++;
        if (panicResponsePattern) count++;
        return count;
    }

    /**
     * 심각(7점 이상) 항목 개수
     */
    public int severeCount() {
        int count = 0;
        if (routineChangeSeverity >= SEVERE_THRESHOLD) count++;
        if (spatialConfusionSeverity >= SEVERE_THRESHOLD) count++;
        if (anxietyEscalationSeverity >= SEVERE_THRESHOLD) count++;
        if (physicalDropSeverity >= SEVERE_THRESHOLD) count++;
        if (sleepActivitySeverity >= SEVERE_THRESHOLD) count++;
        if (panicResponseSeverity >= SEVERE_THRESHOLD) count++;
        return count;
    }

    /**
     * 주의(4점 이상) 항목 개수
     */
    public int moderateCount() {
        int count = 0;
        if (routineChangeSeverity >= MODERATE_THRESHOLD) count++;
        if (spatialConfusionSeverity >= MODERATE_THRESHOLD) count++;
        if (anxietyEscalationSeverity >= MODERATE_THRESHOLD) count++;
        if (physicalDropSeverity >= MODERATE_THRESHOLD) count++;
        if (sleepActivitySeverity >= MODERATE_THRESHOLD) count++;
        if (panicResponseSeverity >= MODERATE_THRESHOLD) count++;
        return count;
    }

    /**
     * 총 심각도 점수 (참고용)
     */
    public int totalSeverity() {
        return routineChangeSeverity +
               spatialConfusionSeverity +
               anxietyEscalationSeverity +
               physicalDropSeverity +
               sleepActivitySeverity +
               panicResponseSeverity;
    }

    /**
     * 매트릭스 방식 위험도 평가
     *
     *            주의(4+) 개수
     *            0    1    2    3+
     * 심각  0    L    L    M    M
     * (7+)  1    L    M    H    H
     * 개수  2+   M    H    H    H
     */
    public String estimateRiskLevel() {
        int severe = severeCount();
        int moderate = moderateCount();

        // severe == 0 (심각한 항목 없음)
        if (severe == 0) {
            if (moderate <= 1) return "LOW";
            return "MEDIUM";
        }

        // severe == 1 (심각한 항목 1개)
        if (severe == 1) {
            if (moderate <= 1) return "LOW";  // 심각 1개만 있고 주의도 1개 이하면 아직 LOW
            return moderate == 2 ? "MEDIUM" : "HIGH";
        }

        // severe >= 2 (심각한 항목 2개 이상)
        if (moderate == 0) return "MEDIUM";  // 심각 2개지만 나머지 지표 괜찮으면 MEDIUM
        return "HIGH";
    }
}
