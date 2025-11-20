package kr.co.ongil.domain.patient.health.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.ongil.domain.patient.health.entity.HealthDataType;

import java.time.LocalDateTime;

/**
 * 단일 건강 데이터 레코드 요청
 */
@Schema(description = "건강 데이터 레코드")
public record HealthDataRecordRequest(

    @Schema(description = "데이터 종류", example = "HEART_RATE", required = true)
    @NotNull(message = "데이터 종류는 필수입니다.")
    HealthDataType type,

    @Schema(description = "평균값", example = "78.0", required = true)
    @NotNull(message = "평균값은 필수입니다.")
    Double average,

    @Schema(description = "최대값", example = "120.0", required = true)
    @NotNull(message = "최대값은 필수입니다.")
    Double max,

    @Schema(description = "최소값", example = "55.0", required = true)
    @NotNull(message = "최소값은 필수입니다.")
    Double min,

    @Schema(description = "단위", example = "bpm", required = true)
    @NotBlank(message = "단위는 필수입니다.")
    String unit,

    @Schema(description = "측정 시각 (ISO 8601)", example = "2025-10-18T14:22:00", required = true)
    @NotNull(message = "측정 시각은 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime measuredAt
) {
}
