package kr.co.ongil.domain.map.dto.tmap;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmapReverseGeocodeResponse(
    AddressInfo addressInfo
) {

    public record AddressInfo(
        String fullAddress,

        RoadAddressInfo roadAddressInfo,

        String cityDo,

        String guGun,

        String legalDong,

        String bunji,

        String roadName,

        String buildingIndex,

        String buildingName
    ) {
    }

    public record RoadAddressInfo(
        String fullAddress
    ) {
    }
}