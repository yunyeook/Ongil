package kr.co.ongil.domain.patient.health.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 건강 데이터 요약 통계 항목
 * 일별 집계 데이터
 */
@Schema(description = "건강 데이터 요약 통계 항목")
public record HealthDataSummaryItemResponse(

    @Schema(description = "날짜", example = "2025-10-10")
    LocalDate date,

    @Schema(description = "평균값", example = "84.3")
    Double average,

    @Schema(description = "최대값", example = "112.0")
    Double max,

    @Schema(description = "최소값", example = "62.0")
    Double min,

    @Schema(description = "데이터 개수", example = "38")
    Long count
) {

    /**
     * 정적 팩토리 메서드
     */
    public static HealthDataSummaryItemResponse of(LocalDate date, Double average, Double max, Double min, Long count) {
        return new HealthDataSummaryItemResponse(date, average, max, min, count);
    }
}
