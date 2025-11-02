package kr.co.ongil.domain.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.ongil.domain.map.dto.response.AddressResponse;
import kr.co.ongil.domain.map.dto.response.CoordinateResponse;
import kr.co.ongil.domain.map.dto.response.PlaceDetailResponse;
import kr.co.ongil.domain.map.dto.response.RouteResponse;
import kr.co.ongil.domain.map.dto.response.SearchPlaceListResponse;
import kr.co.ongil.domain.map.service.MapService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/map")
@RequiredArgsConstructor
@Tag(name = "Map API", description = "지도 관련 API")
public class MapController {

    private final MapService mapService;

    @GetMapping("/address")
    @Operation(summary = "좌표로 주소 조회", description = "GPS 좌표를 받아 주소로 변환합니다.")
    public ApiResponse<AddressResponse> getAddress(
        @Parameter(description = "위도", example = "37.5665", required = true)
        @RequestParam Double latitude,

        @Parameter(description = "경도", example = "126.9780", required = true)
        @RequestParam Double longitude
    ) {
        AddressResponse response = mapService.getAddress(latitude, longitude);
        return ApiResponse.success(ResponseMessage.ADDRESS_FOUND.getMessage(), response);
    }

    @GetMapping("/coordinate")
    @Operation(summary = "주소로 좌표 조회", description = "주소 정보를 받아 GPS 좌표로 변환합니다.")
    public ApiResponse<CoordinateResponse> getCoordinate(
        @Parameter(description = "시/도", example = "서울특별시", required = true)
        @RequestParam String cityDo,

        @Parameter(description = "구/군", example = "강남구", required = true)
        @RequestParam String guGun,

        @Parameter(description = "동/읍/면", example = "역삼동", required = true)
        @RequestParam String dong,

        @Parameter(description = "번지", example = "737")
        @RequestParam(required = false) String bunji
    ) {
        CoordinateResponse response = mapService.getCoordinate(cityDo, guGun, dong, bunji);
        return ApiResponse.success(ResponseMessage.COORDINATE_FOUND.getMessage(), response);
    }

    @GetMapping("/search")
    @Operation(summary = "장소 검색", description = "키워드와 현재 위치를 기반으로 주변 장소를 검색합니다.")
    public ApiResponse<SearchPlaceListResponse> searchPlaces(
        @Parameter(description = "검색 키워드", example = "약국")
        @RequestParam(required = false, defaultValue = "지구대") String keyword,

        @Parameter(description = "위도", example = "37.5665", required = true)
        @RequestParam Double latitude,

        @Parameter(description = "경도", example = "126.9780", required = true)
        @RequestParam Double longitude,

        @Parameter(description = "반경(미터)", example = "1000")
        @RequestParam(required = false, defaultValue = "3000") Integer radius,

        @Parameter(description = "페이지 번호", example = "1")
        @RequestParam(required = false, defaultValue = "1") Integer page,

        @Parameter(description = "페이지당 개수", example = "10")
        @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        SearchPlaceListResponse response = mapService.searchPlaces(keyword, latitude, longitude, radius, page, size);
        return  ApiResponse.success(ResponseMessage.PLACE_SEARCH_SUCCESS.getMessage(), response);
    }

    @GetMapping("/places/{poiId}")
    @Operation(summary = "장소 상세 조회", description = "POI ID로 장소 상세 정보를 조회합니다.")
    public ApiResponse<PlaceDetailResponse> getPlaceDetail(
        @Parameter(description = "POI ID", example = "7839545")
        @PathVariable String poiId
    ) {
        PlaceDetailResponse response = mapService.getPlaceDetail(poiId);
        return ApiResponse.success(ResponseMessage.PLACE_SEARCH_DETAIL_SUCCESS.getMessage(), response);
    }
    @GetMapping("/route/pedestrian")
    @Operation(summary = "보행자 경로 탐색", description = "출발지와 도착지 좌표로 보행자 경로를 조회합니다.")
    public ApiResponse<RouteResponse> getPedestrianRoute(
        @Parameter(description = "출발지 위도", example = "37.5665", required = true)
        @RequestParam Double startLatitude,

        @Parameter(description = "출발지 경도", example = "126.9780", required = true)
        @RequestParam Double startLongitude,

        @Parameter(description = "도착지 위도", example = "37.4979", required = true)
        @RequestParam Double endLatitude,

        @Parameter(description = "도착지 경도", example = "127.0276", required = true)
        @RequestParam Double endLongitude,

        @Parameter(description = "출발지 명칭", example = "서울역")
        @RequestParam(required = false) String startName,

        @Parameter(description = "도착지 명칭", example = "강남역")
        @RequestParam(required = false) String endName
    ) {
        RouteResponse response = mapService.getPedestrianRoute(
            startLatitude, startLongitude,
            endLatitude, endLongitude,
            startName, endName
        );
        return ApiResponse.success(ResponseMessage.ROUTE_FOUND.getMessage(), response);
    }

}