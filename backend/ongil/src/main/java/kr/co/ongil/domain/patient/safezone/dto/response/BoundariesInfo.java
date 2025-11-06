package kr.co.ongil.domain.patient.safezone.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "안전범위 3단계 정보")
public record BoundariesInfo(

    @Schema(description = "1단계 안전범위")
    BoundaryInfo first,

    @Schema(description = "2단계 안전범위")
    BoundaryInfo second,

    @Schema(description = "3단계 안전범위")
    BoundaryInfo third
) {
    public static BoundariesInfo of(
        BoundaryInfo first,
        BoundaryInfo second,
        BoundaryInfo third
    ) {
        return new BoundariesInfo(first, second, third);
    }
}
