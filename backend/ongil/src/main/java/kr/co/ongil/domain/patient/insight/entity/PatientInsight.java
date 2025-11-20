package kr.co.ongil.domain.patient.insight.entity;

import jakarta.persistence.*;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

/**
 * 환자 인사이트 엔티티
 * LLM이 생성한 환자 상태 분석 결과를 저장 (주간/월간)
 */
@Entity
@Table(
    name = "patient_insights",
    indexes = {
        @Index(name = "idx_insight_patient_period", columnList = "patient_id, period_type, period_end_date")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PatientInsight extends BaseEntity {

    /**
     * 환자 ID
     */
    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    /**
     * 기간 타입 (WEEKLY, MONTHLY)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    /**
     * 기간 시작 날짜
     */
    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    /**
     * 기간 종료 날짜
     */
    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    /**
     * 전체 위험 수준 (LOW, MEDIUM, HIGH)
     */
    @Column(name = "overall_risk_level", length = 20)
    private String overallRiskLevel;

    /**
     * 한 줄 요약
     */
    @Column(length = 500)
    private String summary;

    /**
     * 긍정적 신호 (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "positive_signals", columnDefinition = "jsonb")
    private String positiveSignals;

    /**
     * 경고 신호 (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "warning_signals", columnDefinition = "jsonb")
    private String warningSignals;

    /**
     * 가능한 해석 (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "possible_interpretations", columnDefinition = "jsonb")
    private String possibleInterpretations;

    /**
     * 보호자 조언 (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "caregiver_suggestions", columnDefinition = "jsonb")
    private String caregiverSuggestions;

    /**
     * 데이터 주석 (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_notes", columnDefinition = "jsonb")
    private String dataNotes;

    /**
     * LLM 입력 데이터 (디버깅/재분석용)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_features", columnDefinition = "jsonb")
    private String inputFeatures;

    /**
     * LLM 원본 응답 (디버깅용)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "llm_raw_response", columnDefinition = "jsonb")
    private String llmRawResponse;
}
