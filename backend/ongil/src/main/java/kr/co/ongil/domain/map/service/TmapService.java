// TmapService.java (간결해진 버전)
package kr.co.ongil.domain.map.service;

import java.time.Duration;
import java.util.List;
import kr.co.ongil.domain.map.dto.response.AddressResponse;
import kr.co.ongil.domain.map.dto.response.BusinessInfo;
import kr.co.ongil.domain.map.dto.response.CategoryInfo;
import kr.co.ongil.domain.map.dto.response.CoordinateResponse;
import kr.co.ongil.domain.map.dto.response.PlaceDetailResponse;
import kr.co.ongil.domain.map.dto.response.SearchPlaceListResponse;
import kr.co.ongil.domain.map.dto.response.SearchPlaceResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapGeocodeResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapPoiDetailResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapPoiSearchResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapReverseGeocodeResponse;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        String uri = String.format(
            "/tmap/geo/reversegeocoding?version=1&lat=%s&lon=%s&coordType=WGS84GEO&addressType=A10",
            latitude, longitude
        );

        try {
            TmapReverseGeocodeResponse response = tmapWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(TmapReverseGeocodeResponse.class)
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
    public CoordinateResponse getCoordinateFromAddress(String cityDo, String guGun, String dong, String bunji) {
        String encodedCityDo = URLEncoder.encode(cityDo, StandardCharsets.UTF_8);
        String encodedGuGun = URLEncoder.encode(guGun, StandardCharsets.UTF_8);
        String encodedDong = URLEncoder.encode(dong, StandardCharsets.UTF_8);
        String encodedBunji = bunji != null ? URLEncoder.encode(bunji, StandardCharsets.UTF_8) : "";

        String uri = String.format(
            "/tmap/geo/geocoding?version=1&city_do=%s&gu_gun=%s&dong=%s&bunji=%s&coordType=WGS84GEO&addressFlag=F00",
            encodedCityDo, encodedGuGun, encodedDong, encodedBunji
        );

        try {
            TmapGeocodeResponse response = tmapWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(TmapGeocodeResponse.class)
                .block();

            if (response == null || response.coordinateInfo() == null) {
                throw new BusinessException(ErrorCode.COORDINATE_NOT_FOUND);
            }

            TmapGeocodeResponse.CoordinateInfo coordinateInfo = response.coordinateInfo();

            String latitude = coordinateInfo.newLat() != null && !coordinateInfo.newLat().isEmpty()
                ? coordinateInfo.newLat()
                : coordinateInfo.latitude();
            String longitude = coordinateInfo.newLon() != null && !coordinateInfo.newLon().isEmpty()
                ? coordinateInfo.newLon()
                : coordinateInfo.longitude();

            if (latitude == null || latitude.isEmpty() || longitude == null || longitude.isEmpty()) {
                throw new BusinessException(ErrorCode.COORDINATE_NOT_FOUND);
            }

            return CoordinateResponse.parse(latitude, longitude);

        } catch (WebClientResponseException.TooManyRequests e) {
            log.error("Tmap API 호출 제한 초과", e);
            throw new BusinessException(ErrorCode.MAP_API_LIMIT_EXCEEDED);
        } catch (WebClientResponseException e) {
            log.error("Tmap API 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.MAP_API_ERROR);
        } catch (NumberFormatException e) {
            log.error("좌표 파싱 실패", e);
            throw new BusinessException(ErrorCode.COORDINATE_NOT_FOUND);
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
            String uri = String.format("/tmap/pois/%s?version=1&resCoordType=WGS84GEO", poiId);

            TmapPoiDetailResponse response = tmapWebClient.get()
                .uri(uri)
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
                CoordinateResponse.ofWithFallback(poi.frontLat(), poi.lat(), poi.frontLon(), poi.lon()),
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
}