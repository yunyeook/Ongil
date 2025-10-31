package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.map.dto.tmap.TmapPoiDetailResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapReverseGeocodeResponse;

@Schema(description = "주소 정보 응답")
public record AddressResponse(

    @Schema(description = "도로명 주소", example = "서울 중구 세종대로 110")
    String roadAddress,

    @Schema(description = "지번 주소", example = "서울 중구 태평로1가 31")
    String jibunAddress
) {

    public static AddressResponse of(String roadNameAddress, String lotNumberAddress) {
        return new AddressResponse(roadNameAddress, lotNumberAddress);
    }

    /**
     * POI 상세 정보에서 주소 생성
     */
    public static AddressResponse fromPoiDetail(TmapPoiDetailResponse.PoiDetailInfo poi) {
        return new AddressResponse(
            buildRoadAddress(poi),
            buildJibunAddress(poi)
        );
    }

    /**
     * Reverse Geocoding 응답에서 주소 생성
     */
    public static AddressResponse fromReverseGeocode(TmapReverseGeocodeResponse.AddressInfo info) {
        String roadAddress = null;
        if (info.roadName() != null) {
            roadAddress = String.format("%s %s %s %s",
                info.cityDo() != null ? info.cityDo() : "",
                info.guGun() != null ? info.guGun() : "",
                info.roadName(),
                info.buildingIndex() != null ? info.buildingIndex() : ""
            ).trim();

            if (info.buildingName() != null && !info.buildingName().isEmpty()) {
                roadAddress += " " + info.buildingName();
            }
        }

        String jibunAddress = String.format("%s %s %s %s",
            info.cityDo() != null ? info.cityDo() : "",
            info.guGun() != null ? info.guGun() : "",
            info.legalDong() != null ? info.legalDong() : "",
            info.bunji() != null ? info.bunji() : ""
        ).trim();

        return new AddressResponse(roadAddress, jibunAddress);
    }

    private static String buildRoadAddress(TmapPoiDetailResponse.PoiDetailInfo poi) {
        if (poi.bldAddr() == null || poi.bldAddr().isEmpty()) {
            return null;
        }

        String roadAddress = poi.bldAddr();
        if (poi.bldNo1() != null && !poi.bldNo1().isEmpty()) {
            roadAddress += " " + poi.bldNo1();
            if (poi.bldNo2() != null && !poi.bldNo2().isEmpty() && !poi.bldNo2().equals("0")) {
                roadAddress += "-" + poi.bldNo2();
            }
        }
        return roadAddress;
    }

    private static String buildJibunAddress(TmapPoiDetailResponse.PoiDetailInfo poi) {
        String jibunAddress = String.format("%s %s %s",
            poi.lcdName() != null ? poi.lcdName() : "",
            poi.mcdName() != null ? poi.mcdName() : "",
            poi.scdName() != null ? poi.scdName() : ""
        ).trim();

        if (poi.firstNo() != null && !poi.firstNo().isEmpty()) {
            jibunAddress += " " + poi.firstNo();
            if (poi.secondNo() != null && !poi.secondNo().isEmpty() && !poi.secondNo().equals("0")) {
                jibunAddress += "-" + poi.secondNo();
            }
        }
        return jibunAddress;
    }
}