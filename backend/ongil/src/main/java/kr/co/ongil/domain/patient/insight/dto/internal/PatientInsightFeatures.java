package kr.co.ongil.domain.patient.insight.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * LLM에 입력할 환자 인사이트 특징 데이터
 * 백엔드에서 집계한 모든 정보를 담음
 */
@Builder
public record PatientInsightFeatures(
    @JsonProperty("patient_id")
    Integer patientId,

    @JsonProperty("patient_profile")
    PatientProfile patientProfile,

    PeriodInfo period,

    ActivityStats activity,

    HealthStats health,

    InsightFlags flags
) {

    @Builder
    public record PatientProfile(
        @JsonProperty("age_group")
        String ageGroup,  // "70대"

        String gender  // "F" / "M"
    ) {}
}
