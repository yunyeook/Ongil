package kr.co.ongil.domain.patient.safezone.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

@Schema(description = "안전범위 부분 수정 요청")
public record SafeZonePatchRequest(

    @Schema(description = "1단계 반경 (미터)", example = "150.0", minimum = "50", maximum = "150")
    @DecimalMin(value = "0.0", inclusive = false, message = "반경은 0보다 커야 합니다.")
    Double firstBoundary,

    @Schema(description = "2단계 반경 (미터)", example = "500.0", minimum = "200", maximum = "500")
    @DecimalMin(value = "0.0", inclusive = false, message = "반경은 0보다 커야 합니다.")
    Double secondBoundary,

    @Schema(description = "3단계 반경 (미터)", example = "1000.0", minimum = "550", maximum = "1000")
    @DecimalMin(value = "0.0", inclusive = false, message = "반경은 0보다 커야 합니다.")
    Double thirdBoundary,

    @Schema(description = "1단계 이상탐지 시간 (분)", example = "60")
    @Min(value = 1, message = "시간은 최소 1분 이상이어야 합니다.")
    Integer firstTime,

    @Schema(description = "2단계 이상탐지 시간 (분)", example = "30")
    @Min(value = 1, message = "시간은 최소 1분 이상이어야 합니다.")
    Integer secondTime,

    @Schema(description = "3단계 이상탐지 시간 (분)", example = "15")
    @Min(value = 1, message = "시간은 최소 1분 이상이어야 합니다.")
    Integer thirdTime
) {
}
