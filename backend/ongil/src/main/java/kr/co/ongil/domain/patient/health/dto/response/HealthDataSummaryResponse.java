package kr.co.ongil.domain.patient.health.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.patient.health.entity.HealthDataType;

import java.util.List;

/**
 * 건강 데이터 요약 통계 응답
 * 일별/주별/지정기간 단위 통계
 */
@Schema(description = "건강 데이터 요약 통계 응답")
public record HealthDataSummaryResponse(

    @Schema(description = "환자 ID", example = "2")
    Integer patientId,

    @Schema(description = "데이터 종류 (null이면 전체)", example = "HEART_RATE")
    HealthDataType type,

    @Schema(description = "단위", example = "bpm")
    String unit,

    @Schema(description = "일별 요약 통계 목록")
    List<HealthDataSummaryItemResponse> summary
) {

    /**
     * 정적 팩토리 메서드
     */
    public static HealthDataSummaryResponse of(Integer patientId, HealthDataType type, String unit, List<HealthDataSummaryItemResponse> summary) {
        return new HealthDataSummaryResponse(patientId, type, unit, summary);
    }
}
