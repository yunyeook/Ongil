package kr.co.ongil.domain.patient.insight.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * LLM이 반환하는 인사이트 응답
 */
public record LLMInsightResponse(
    String summary,

    @JsonProperty("overall_risk_level")
    String overallRiskLevel,  // LOW, MEDIUM, HIGH

    @JsonProperty("positive_signals")
    List<String> positiveSignals,

    @JsonProperty("warning_signals")
    List<String> warningSignals,

    @JsonProperty("possible_interpretations")
    List<String> possibleInterpretations,

    @JsonProperty("caregiver_suggestions")
    List<String> caregiverSuggestions,

    @JsonProperty("data_notes")
    List<String> dataNotes
) {
}
