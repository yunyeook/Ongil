package kr.co.ongil.domain.patient.safezone.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "안전범위 단계별 정보")
public record BoundaryInfo(

    @Schema(description = "반경 (미터)", example = "150.0")
    Double radius,

    @Schema(description = "배회 탐지 시간 (분)", example = "60")
    Integer time
) {
    public static BoundaryInfo of(Double radius, Integer time) {
        return new BoundaryInfo(radius, time);
    }
}
