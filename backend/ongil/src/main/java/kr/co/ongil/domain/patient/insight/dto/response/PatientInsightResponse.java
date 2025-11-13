package kr.co.ongil.domain.patient.insight.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.patient.insight.entity.PatientInsight;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 환자 인사이트 응답 DTO
 */
@Slf4j
@Schema(description = "환자 인사이트 응답")
public record PatientInsightResponse(

    @Schema(description = "인사이트 ID", example = "1")
    Integer id,

    @Schema(description = "환자 ID", example = "10")
    @JsonProperty("patient_id")
    Integer patientId,

    @Schema(description = "기간 타입", example = "WEEKLY")
    @JsonProperty("period_type")
    String periodType,

    @Schema(description = "분석 기간 시작일", example = "2025-01-06")
    @JsonProperty("period_start_date")
    LocalDate periodStartDate,

    @Schema(description = "분석 기간 종료일", example = "2025-01-12")
    @JsonProperty("period_end_date")
    LocalDate periodEndDate,

    @Schema(description = "전반적 위험 수준", example = "MEDIUM", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    @JsonProperty("overall_risk_level")
    String overallRiskLevel,

    @Schema(description = "전체 요약 (2-3문장)", example = "이번 주 환자분은 안정적인 생활 패턴을 보였습니다. 긴급 상황 없이 평소와 같은 활동을 유지하였습니다.")
    String summary,

    @Schema(description = "긍정적 신호 목록")
    @JsonProperty("positive_signals")
    List<String> positiveSignals,

    @Schema(description = "경고 신호 목록")
    @JsonProperty("warning_signals")
    List<String> warningSignals,

    @Schema(description = "가능한 해석 목록")
    @JsonProperty("possible_interpretations")
    List<String> possibleInterpretations,

    @Schema(description = "보호자 제안 목록")
    @JsonProperty("caregiver_suggestions")
    List<String> caregiverSuggestions,

    @Schema(description = "데이터 제약사항 목록")
    @JsonProperty("data_notes")
    List<String> dataNotes

) {

    /**
     * PatientInsight Entity를 PatientInsightResponse로 변환
     */
    public static PatientInsightResponse from(PatientInsight entity) {
        ObjectMapper mapper = new ObjectMapper();

        return new PatientInsightResponse(
            entity.getId(),
            entity.getPatientId(),
            entity.getPeriodType().name(),
            entity.getPeriodStartDate(),
            entity.getPeriodEndDate(),
            entity.getOverallRiskLevel(),
            entity.getSummary(),
            parseJsonArray(entity.getPositiveSignals(), mapper),
            parseJsonArray(entity.getWarningSignals(), mapper),
            parseJsonArray(entity.getPossibleInterpretations(), mapper),
            parseJsonArray(entity.getCaregiverSuggestions(), mapper),
            parseJsonArray(entity.getDataNotes(), mapper)
        );
    }

    /**
     * JSON 문자열을 List<String>으로 파싱
     */
    private static List<String> parseJsonArray(String json, ObjectMapper mapper) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("JSON 파싱 실패: {}", json, e);
            return Collections.emptyList();
        }
    }
}
