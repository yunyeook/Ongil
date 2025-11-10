package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌표 정보 응답")
public record CoordinateInfo(

    @Schema(description = "위도", example = "37.5665")
    Double latitude,

    @Schema(description = "경도", example = "126.9780")
    Double longitude

) {
    public static CoordinateInfo of(Double latitude, Double longitude) {
        return new CoordinateInfo(latitude, longitude);
    }
    /**
     * 우선순위를 가진 좌표값에서 생성
     */
    public static CoordinateInfo ofWithFallback(
        String primaryLat, String fallbackLat,
        String primaryLon, String fallbackLon
    ) {
        String lat = isValid(primaryLat) ? primaryLat : fallbackLat;
        String lon = isValid(primaryLon) ? primaryLon : fallbackLon;

        return new CoordinateInfo(
            isValid(lat) ? Double.parseDouble(lat) : null,
            isValid(lon) ? Double.parseDouble(lon) : null
        );
    }

    /**
     * String 좌표값 파싱
     */
    public static CoordinateInfo parse(String latitude, String longitude) {
        return new CoordinateInfo(
            isValid(latitude) ? Double.parseDouble(latitude) : null,
            isValid(longitude) ? Double.parseDouble(longitude) : null
        );
    }

    private static boolean isValid(String value) {
        return value != null && !value.isEmpty();
    }
}