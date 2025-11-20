package kr.co.ongil.domain.patient.health.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.patient.health.entity.HealthData;
import kr.co.ongil.domain.patient.health.entity.HealthDataType;

import java.time.LocalDateTime;

/**
 * 단일 건강 데이터 레코드 응답
 */
@Schema(description = "건강 데이터 레코드 응답")
public record HealthDataRecordResponse(

    @Schema(description = "레코드 ID", example = "1001")
    Integer recordId,

    @Schema(description = "데이터 종류", example = "HEART_RATE")
    HealthDataType type,

    @Schema(description = "평균값", example = "78.0")
    Double average,

    @Schema(description = "최대값", example = "120.0")
    Double max,

    @Schema(description = "최소값", example = "55.0")
    Double min,

    @Schema(description = "단위", example = "bpm")
    String unit,

    @Schema(description = "측정 시각", example = "2025-10-18T14:22:00")
    LocalDateTime measuredAt
) {

    /**
     * Entity에서 Response DTO로 변환
     */
    public static HealthDataRecordResponse from(HealthData entity) {
        return new HealthDataRecordResponse(
            entity.getId(),
            entity.getType(),
            entity.getAverage(),
            entity.getMax(),
            entity.getMin(),
            entity.getUnit(),
            entity.getMeasuredAt()
        );
    }
}
