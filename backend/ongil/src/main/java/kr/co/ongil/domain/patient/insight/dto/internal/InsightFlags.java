package kr.co.ongil.domain.patient.insight.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 룰 기반 위험 신호 플래그
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
    boolean panicResponsePattern
) {

    public static InsightFlags allNormal() {
        return new InsightFlags(false, false, false, false, false, false);
    }

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

    public String estimateRiskLevel() {
        int active = activeCount();
        if (active == 0) return "LOW";
        if (active >= 3) return "HIGH";
        return "MEDIUM";
    }
}
