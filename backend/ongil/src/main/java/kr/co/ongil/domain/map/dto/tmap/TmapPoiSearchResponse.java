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
        String totalCount,  // 전체 검색 결과 수

        String count,  // 현재 페이지에 반환된 결과 수

        String page,  // 현재 페이지 번호

        Pois pois  // POI 목록
    ) {

    }

    /**
     * POI 목록 래퍼
     */
    public record Pois(
        List<Poi> poi  // POI 리스트
    ) {

    }

    /**
     * POI (Point of Interest) 상세 정보
     */
    public record Poi(
        String id,  // POI 고유 ID

        String name,  // 장소명

        String telNo,  // 전화번호

        String frontLat,  // 시설물 입구 위도 좌표

        String frontLon,  // 시설물 입구 경도 좌표

        String noorLat,  // 중심점 위도 좌표

        String noorLon,  // 중심점 경도 좌표

        String upperAddrName,  // 시/도 (예: 서울특별시)

        String middleAddrName,  // 구/군 (예: 강남구)

        String lowerAddrName,  // 동/읍/면 (예: 역삼동)

        String detailAddrName,  // 상세 주소 (예: 건물명, 아파트 동호수)

        String firstNo,  // 지번 본번

        String secondNo,  // 지번 부번

        String roadName,  // 도로명 (예: 테헤란로)

        String firstBuildNo,  // 건물번호 1 (도로명주소)

        String secondBuildNo,  // 건물번호 2 (도로명주소)

        String mlClass,  // 산/대지 구분 (구주소)

        String groupSubClass,  // POI 그룹 하위 분류

        String radius,  // 검색 중심점으로부터의 거리 (km)

        String distance,  // 요청 좌표에서 떨어진 거리 (km)

        String bizName,  // 대표 업종명

        String upperBizName,  // 업종 대분류명 (예: 의료편의)

        String middleBizName,  // 업종 중분류명 (예: 의료시설)

        String lowerBizName,  // 업종 소분류명 (예: 종합병원)

        NewAddressList newAddressList
    ) {

    }

    public record NewAddressList(
        List<NewAddress> newAddress
    ) {

    }

    public record NewAddress(
        String centerLat,

        String centerLon,

        String frontLat,

        String frontLon,

        String roadName,

        String bldNo1,

        String bldNo2,

        String roadId,

        String fullAddressRoad
    ) {

    }
}