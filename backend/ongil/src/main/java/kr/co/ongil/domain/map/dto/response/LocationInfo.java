package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Schema(description = "위치 정보")
@Builder
public record LocationInfo(
    @Schema(description = "위도", example = "37.5665")
    Double latitude,

    @Schema(description = "경도", example = "126.9780")
    Double longitude,

    @Schema(description = "장소명", example = "서울역")
    String name
) {

    public static LocationInfo of(Double latitude, Double longitude, String name) {
        return new LocationInfo(latitude, longitude, name);
    }
}