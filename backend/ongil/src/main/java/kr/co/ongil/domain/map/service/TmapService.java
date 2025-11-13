package kr.co.ongil.domain.map.service;

import java.time.Duration;
import java.util.List;
import kr.co.ongil.domain.map.dto.response.AddressResponse;
import kr.co.ongil.domain.map.dto.response.BusinessInfo;
import kr.co.ongil.domain.map.dto.response.CategoryInfo;
import kr.co.ongil.domain.map.dto.response.CoordinateInfo;
import kr.co.ongil.domain.map.dto.response.PlaceDetailResponse;
import kr.co.ongil.domain.map.dto.response.RouteResponse;
import kr.co.ongil.domain.map.dto.response.SearchPlaceListResponse;
import kr.co.ongil.domain.map.dto.response.SearchPlaceResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapGeocodeResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapPedestrianRouteResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapPoiDetailResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapPoiSearchResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapReverseGeocodeResponse;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmapService {

    @Qualifier("tmapWebClient")
    private final WebClient tmapWebClient;

    /**
     * 좌표 → 주소 변환 (Reverse Geocoding)
     */
    public AddressResponse getAddressFromCoordinate(Double latitude, Double longitude) {
        try {
            TmapReverseGeocodeResponse response = tmapWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/tmap/geo/reversegeocoding")
                    .queryParam("version", 1)
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("coordType", "WGS84GEO")
                    .queryParam("addressType", "A10")
                    .build())
                .retrieve()
                .bodyToMono(TmapReverseGeocodeResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response == null || response.addressInfo() == null) {
                throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
            }

            return AddressResponse.fromReverseGeocode(response.addressInfo());

        } catch (WebClientResponseException.TooManyRequests e) {
            log.error("Tmap API 호출 제한 초과", e);
            throw new BusinessException(ErrorCode.MAP_API_LIMIT_EXCEEDED);
        } catch (WebClientResponseException e) {
            log.error("Tmap API 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.MAP_API_ERROR);
        } catch (Exception e) {
            log.error("좌표 → 주소 변환 중 예외 발생", e);
            throw new BusinessException(ErrorCode.MAP_API_ERROR);
        }
    }

    /**
     * 주소 → 좌표 변환 (Geocoding)
     */
    public CoordinateInfo getCoordinateFromAddress(String cityDo, String guGun, String dong, String bunji) {
        try {
            TmapGeocodeResponse response = tmapWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                        .path("/tmap/geo/geocoding")
                        .queryParam("version", 1)
                        .queryParam("city_do", cityDo)
                        .queryParam("gu_gun", guGun)
                        .queryParam("dong", dong)
                        .queryParam("coordType", "WGS84GEO")
                        .queryParam("addressFlag", "F00");

                    // bunji가 있을 때만 추가
                    if (bunji != null && !bunji.trim().isEmpty()) {
                        builder.queryParam("bunji", bunji);
                    }

                    return builder.build();
                })
                .retrieve()
                .bodyToMono(TmapGeocodeResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            log.info("Tmap Geocoding API 응답: {}", response);

            if (response == null || response.coordinateInfo() == null) {
                log.error("Tmap API 응답이 null이거나 coordinateInfo가 없습니다. response={}", response);
                throw new BusinessException(ErrorCode.COORDINATE_NOT_FOUND);
            }

            TmapGeocodeResponse.CoordinateInfo coordinateInfo = response.coordinateInfo();
            log.info("CoordinateInfo: newLat={}, newLon={}, lat={}, lon={}",
                coordinateInfo.newLat(), coordinateInfo.newLon(),
                coordinateInfo.latitude(), coordinateInfo.longitude());

            String latitude = (coordinateInfo.newLat() != null && !coordinateInfo.newLat().trim().isEmpty())
                ? coordinateInfo.newLat()
                : coordinateInfo.latitude();

            String longitude = (coordinateInfo.newLon() != null && !coordinateInfo.newLon().trim().isEmpty())
                ? coordinateInfo.newLon()
                : coordinateInfo.longitude();

            if (latitude == null || latitude.isEmpty() || longitude == null || longitude.isEmpty()) {
                log.error("좌표 값이 비어있습니다. latitude={}, longitude={}", latitude, longitude);
                throw new BusinessException(ErrorCode.COORDINATE_NOT_FOUND);
            }

            return CoordinateInfo.parse(latitude, longitude);

        } catch (WebClientResponseException.TooManyRequests e) {
            log.error("Tmap API 호출 제한 초과", e);
            throw new BusinessException(ErrorCode.MAP_API_LIMIT_EXCEEDED);
        } catch (WebClientResponseException e) {
            log.error("Tmap API 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.MAP_API_ERROR);
        } catch (NumberFormatException e) {
            log.error("좌표 파싱 실패", e);
            throw new BusinessException(ErrorCode.COORDINATE_NOT_FOUND);
        } catch (BusinessException e) {
            throw e;  // BusinessException은 그대로 던지기
        } catch (Exception e) {
            log.error("주소 → 좌표 변환 중 예외 발생", e);
            throw new BusinessException(ErrorCode.MAP_API_ERROR);
        }
    }

    /**
     * 장소 검색 (POI 통합 검색)
     */
    public SearchPlaceListResponse searchPlaces(String keyword, Double latitude, Double longitude,
        Integer radius, Integer page, Integer count) {
        final int radiusInKm = Math.max(1, Math.min(33, radius / 1000));

        try {
            TmapPoiSearchResponse response = tmapWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/tmap/pois")
                    .queryParam("version", 1)
                    .queryParam("searchKeyword", keyword)
                    .queryParam("centerLat", latitude)
                    .queryParam("centerLon", longitude)
                    .queryParam("radius", radiusInKm)
                    .queryParam("searchtypCd", "R")
                    .queryParam("reqCoordType", "WGS84GEO")
                    .queryParam("resCoordType", "WGS84GEO")
                    .queryParam("page", page)
                    .queryParam("count", count)
                    .build())
                .retrieve()
                .bodyToMono(TmapPoiSearchResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response == null || response.searchPoiInfo() == null) {
                return SearchPlaceListResponse.of(0, page, count, List.of());
            }

            TmapPoiSearchResponse.SearchPoiInfo searchPoiInfo = response.searchPoiInfo();

            if (searchPoiInfo.pois() == null || searchPoiInfo.pois().poi() == null) {
                return SearchPlaceListResponse.of(0, page, count, List.of());
            }

            List<SearchPlaceResponse> places = searchPoiInfo.pois().poi().stream()
                .map(SearchPlaceResponse::fromTmapPoi)
                .sorted((p1, p2) -> {
                    if (p1.distance() == null && p2.distance() == null) return 0;
                    if (p1.distance() == null) return 1;
                    if (p2.distance() == null) return -1;
                    return p1.distance().compareTo(p2.distance());
                })
                .toList();

            int totalCount = Integer.parseInt(searchPoiInfo.totalCount());

            return SearchPlaceListResponse.of(totalCount, page, count, places);

        } catch (WebClientResponseException.TooManyRequests e) {
            log.error("Tmap API 호출 제한 초과", e);
            throw new BusinessException(ErrorCode.MAP_API_LIMIT_EXCEEDED);
        } catch (WebClientResponseException e) {
            log.error("Tmap API 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.MAP_API_ERROR);
        } catch (Exception e) {
            log.error("장소 검색 중 예외 발생", e);
            throw new BusinessException(ErrorCode.PLACE_SEARCH_FAILED);
        }
    }

    /**
     * 장소 상세 조회 (POI 상세 정보)
     */
    public PlaceDetailResponse getPlaceDetail(String poiId) {
        try {
            TmapPoiDetailResponse response = tmapWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/tmap/pois/{poiId}")
                    .queryParam("version", 1)
                    .queryParam("resCoordType", "WGS84GEO")
                    .build(poiId))
                .retrieve()
                .bodyToMono(TmapPoiDetailResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response == null || response.poiDetailInfo() == null) {
                throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);
            }

            TmapPoiDetailResponse.PoiDetailInfo poi = response.poiDetailInfo();

            return PlaceDetailResponse.of(
                poi.id(),
                poi.name(),
                AddressResponse.fromPoiDetail(poi),
                CoordinateInfo.ofWithFallback(poi.frontLat(), poi.lat(), poi.frontLon(), poi.lon()),
                CategoryInfo.of(poi.bizCatName(), null, null),
                poi.tel(),
                poi.desc(),
                poi.zipCode(),
                poi.parkFlag() != null && poi.parkFlag().equals("1"),
                BusinessInfo.fromAdditionalInfo(poi.additionalInfo(), poi.twFlag(), poi.yaFlag())
            );

        } catch (WebClientResponseException.NotFound e) {
            log.error("장소를 찾을 수 없음: poiId={}", poiId);
            throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);
        } catch (WebClientResponseException.TooManyRequests e) {
            log.error("Tmap API 호출 제한 초과", e);
            throw new BusinessException(ErrorCode.MAP_API_LIMIT_EXCEEDED);
        } catch (WebClientResponseException e) {
            log.error("Tmap API 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.MAP_API_ERROR);
        } catch (Exception e) {
            log.error("장소 상세 조회 중 예외 발생", e);
            throw new BusinessException(ErrorCode.PLACE_DETAIL_FAILED);
        }
    }
    /**
     * 보행자 경로 탐색
     */
    public RouteResponse getPedestrianRoute(
        Double startLat, Double startLon,
        Double endLat, Double endLon,
        String startName, String endName
    ) {
        try {
            TmapPedestrianRouteResponse response = tmapWebClient.post()
                .uri("/tmap/routes/pedestrian?version=1")
                .body(BodyInserters.fromFormData("startX", String.valueOf(startLon))
                    .with("startY", String.valueOf(startLat))
                    .with("endX", String.valueOf(endLon))
                    .with("endY", String.valueOf(endLat))
                    .with("reqCoordType", "WGS84GEO")
                    .with("resCoordType", "WGS84GEO")
                    .with("startName", startName != null ? startName : "출발지")
                    .with("endName", endName != null ? endName : "도착지")
                    .with("searchOption", "0")
                )
                .retrieve()
                .bodyToMono(TmapPedestrianRouteResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response == null || response.features() == null || response.features().isEmpty()) {
                throw new BusinessException(ErrorCode.ROUTE_NOT_FOUND);
            }

            return RouteResponse.parseRouteResponse(
                response, startLat, startLon, endLat, endLon, startName, endName
            );

        } catch (WebClientResponseException.TooManyRequests e) {
            log.error("Tmap API 호출 제한 초과", e);
            throw new BusinessException(ErrorCode.MAP_API_LIMIT_EXCEEDED);
        } catch (WebClientResponseException e) {
            log.error("Tmap API 호출 실패: status={}, body={}",
                e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.MAP_API_ERROR);
        } catch (Exception e) {
            log.error("경로 탐색 중 예외 발생", e);
            throw new BusinessException(ErrorCode.ROUTE_SEARCH_FAILED);
        }
    }
}