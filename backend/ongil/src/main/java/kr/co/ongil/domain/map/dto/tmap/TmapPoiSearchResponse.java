package kr.co.ongil.domain.map.dto.tmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Tmap POI 통합 검색 API 응답
 */
public record TmapPoiSearchResponse(
    SearchPoiInfo searchPoiInfo
) {

    /**
     * 검색 결과 정보
     */
    public record SearchPoiInfo(
        @JsonProperty("totalCount")
        String totalCount,  // 전체 검색 결과 수

        @JsonProperty("count")
        String count,  // 현재 페이지에 반환된 결과 수

        @JsonProperty("page")
        String page,  // 현재 페이지 번호

        @JsonProperty("pois")
        Pois pois  // POI 목록
    ) {

    }

    /**
     * POI 목록 래퍼
     */
    public record Pois(
        @JsonProperty("poi")
        List<Poi> poi  // POI 리스트
    ) {

    }

    /**
     * POI (Point of Interest) 상세 정보
     */
    public record Poi(
        @JsonProperty("id")
        String id,  // POI 고유 ID

        @JsonProperty("name")
        String name,  // 장소명

        @JsonProperty("telNo")
        String telNo,  // 전화번호

        @JsonProperty("frontLat")
        String frontLat,  // 시설물 입구 위도 좌표

        @JsonProperty("frontLon")
        String frontLon,  // 시설물 입구 경도 좌표

        @JsonProperty("noorLat")
        String noorLat,  // 중심점 위도 좌표

        @JsonProperty("noorLon")
        String noorLon,  // 중심점 경도 좌표

        @JsonProperty("upperAddrName")
        String upperAddrName,  // 시/도 (예: 서울특별시)

        @JsonProperty("middleAddrName")
        String middleAddrName,  // 구/군 (예: 강남구)

        @JsonProperty("lowerAddrName")
        String lowerAddrName,  // 동/읍/면 (예: 역삼동)

        @JsonProperty("detailAddrName")
        String detailAddrName,  // 상세 주소 (예: 건물명, 아파트 동호수)

        @JsonProperty("firstNo")
        String firstNo,  // 지번 본번

        @JsonProperty("secondNo")
        String secondNo,  // 지번 부번

        @JsonProperty("roadName")
        String roadName,  // 도로명 (예: 테헤란로)

        @JsonProperty("firstBuildNo")
        String firstBuildNo,  // 건물번호 1 (도로명주소)

        @JsonProperty("secondBuildNo")
        String secondBuildNo,  // 건물번호 2 (도로명주소)

        @JsonProperty("mlClass")
        String mlClass,  // 산/대지 구분 (구주소)

        @JsonProperty("groupSubClass")
        String groupSubClass,  // POI 그룹 하위 분류

        @JsonProperty("radius")
        String radius,  // 검색 중심점으로부터의 거리 (km)

        @JsonProperty("distance")
        String distance,  // 요청 좌표에서 떨어진 거리 (km)

        @JsonProperty("bizName")
        String bizName,  // 대표 업종명

        @JsonProperty("upperBizName")
        String upperBizName,  // 업종 대분류명 (예: 의료편의)

        @JsonProperty("middleBizName")
        String middleBizName,  // 업종 중분류명 (예: 의료시설)

        @JsonProperty("lowerBizName")
        String lowerBizName,  // 업종 소분류명 (예: 종합병원)

        @JsonProperty("newAddressList")
        NewAddressList newAddressList
    ) {

    }

    public record NewAddressList(
        @JsonProperty("newAddress")
        List<NewAddress> newAddress
    ) {

    }

    public record NewAddress(
        @JsonProperty("centerLat")
        String centerLat,

        @JsonProperty("centerLon")
        String centerLon,

        @JsonProperty("frontLat")
        String frontLat,

        @JsonProperty("frontLon")
        String frontLon,

        @JsonProperty("roadName")
        String roadName,

        @JsonProperty("bldNo1")
        String bldNo1,

        @JsonProperty("bldNo2")
        String bldNo2,

        @JsonProperty("roadId")
        String roadId,

        @JsonProperty("fullAddressRoad")
        String fullAddressRoad
    ) {

    }
}