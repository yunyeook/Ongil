// SearchPlaceResponse.java
package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.map.dto.tmap.TmapPoiSearchResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Schema(description = "장소 정보")
public record SearchPlaceResponse(

    @Schema(description = "POI ID", example = "7839545")
    String id,

    @Schema(description = "장소명", example = "온길약국")
    String name,

    @Schema(description = "주소", example = "서울 강남구 역삼동 123")
    String address,

    @Schema(description = "위도", example = "37.5665")
    Double latitude,

    @Schema(description = "경도", example = "126.9780")
    Double longitude,

    @Schema(description = "거리(미터)", example = "120")
    Integer distance,

    @Schema(description = "카테고리")
    CategoryInfo category,

    @Schema(description = "전화번호", example = "02-1234-5678")
    String phoneNumber
) {

    public static SearchPlaceResponse of(
        String id,
        String name,
        String address,
        Double latitude,
        Double longitude,
        Integer distance,
        CategoryInfo category,
        String phoneNumber
    ) {
        return new SearchPlaceResponse(
            id, name, address, latitude, longitude,
            distance, category, phoneNumber
        );
    }

    /**
     * Tmap POI 검색 결과에서 SearchPlaceResponse 생성
     */
    public static SearchPlaceResponse fromTmapPoi(TmapPoiSearchResponse.Poi poi) {
        // 주소
        String address = extractAddress(poi);

        // 좌표
        CoordinateInfo coordinate = CoordinateInfo.ofWithFallback(
            poi.frontLat(), poi.noorLat(),
            poi.frontLon(), poi.noorLon()
        );

        // 거리 (km → 미터)
        Integer distanceInMeters = parseDistance(poi.radius());

        // 카테고리
        CategoryInfo category = CategoryInfo.of(
            poi.upperBizName(),
            poi.middleBizName(),
            poi.lowerBizName()
        );

        return new SearchPlaceResponse(
            poi.id(),
            poi.name(),
            address,
            coordinate.latitude(),
            coordinate.longitude(),
            distanceInMeters,
            category,
            poi.telNo()
        );
    }

    private static String extractAddress(TmapPoiSearchResponse.Poi poi) {
        // newAddressList의 fullAddressRoad 우선 사용
        if (poi.newAddressList() != null &&
            poi.newAddressList().newAddress() != null &&
            !poi.newAddressList().newAddress().isEmpty()) {
            String fullAddressRoad = poi.newAddressList().newAddress().get(0).fullAddressRoad();
            if (fullAddressRoad != null && !fullAddressRoad.isEmpty()) {
                return fullAddressRoad;
            }
        }

        // 대체 주소 생성
        return String.format("%s %s %s",
            poi.upperAddrName() != null ? poi.upperAddrName() : "",
            poi.middleAddrName() != null ? poi.middleAddrName() : "",
            poi.lowerAddrName() != null ? poi.lowerAddrName() : ""
        ).trim();
    }

    private static Integer parseDistance(String distanceField) {
        if (distanceField == null || distanceField.isEmpty()) {
            return null;
        }

        try {
            double distanceKm = Double.parseDouble(distanceField);
            return (int) (distanceKm * 1000);
        } catch (NumberFormatException e) {
            log.warn("거리 파싱 실패: {}", distanceField);
            return null;
        }
    }
}