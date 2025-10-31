package kr.co.ongil.domain.map.dto.tmap;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmapReverseGeocodeResponse(
    AddressInfo addressInfo
) {

    public record AddressInfo(
        @JsonProperty("fullAddress")
        String fullAddress,

        @JsonProperty("roadAddressInfo")
        RoadAddressInfo roadAddressInfo,

        @JsonProperty("city_do")
        String cityDo,

        @JsonProperty("gu_gun")
        String guGun,

        @JsonProperty("legalDong")
        String legalDong,

        @JsonProperty("bunji")
        String bunji,

        @JsonProperty("roadName")
        String roadName,

        @JsonProperty("buildingIndex")
        String buildingIndex,

        @JsonProperty("buildingName")
        String buildingName
    ) {
    }

    public record RoadAddressInfo(
        @JsonProperty("fullAddress")
        String fullAddress
    ) {
    }
}