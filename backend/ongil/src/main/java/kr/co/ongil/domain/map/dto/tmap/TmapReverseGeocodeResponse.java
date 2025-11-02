package kr.co.ongil.domain.map.dto.tmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmapReverseGeocodeResponse(
    AddressInfo addressInfo
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
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