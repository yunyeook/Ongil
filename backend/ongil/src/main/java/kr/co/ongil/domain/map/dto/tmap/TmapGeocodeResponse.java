package kr.co.ongil.domain.map.dto.tmap;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmapGeocodeResponse(
    CoordinateInfo coordinateInfo
) {

    public record CoordinateInfo(
        @JsonProperty("lat") //지번주소일때 위도
        String latitude,

        @JsonProperty("lon") //지번주소일때 경도
        String longitude,

        @JsonProperty("newLat") //도로명주소일때 위도
        String newLat,

        @JsonProperty("newLon") //도로명주소일때 경도
        String newLon
    ) {
    }
}