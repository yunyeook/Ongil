package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌표 정보 응답")
public record CoordinateResponse(

    @Schema(description = "위도", example = "37.5665")
    Double latitude,

    @Schema(description = "경도", example = "126.9780")
    Double longitude

) {
    public static CoordinateResponse of(Double latitude, Double longitude) {
        return new CoordinateResponse(latitude, longitude);
    }
}