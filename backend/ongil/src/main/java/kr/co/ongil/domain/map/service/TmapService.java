package kr.co.ongil.domain.map.service;

import java.time.Duration;
import java.util.List;
import kr.co.ongil.domain.map.dto.response.AddressResponse;
import kr.co.ongil.domain.map.dto.response.CoordinateResponse;
import kr.co.ongil.domain.map.dto.response.SearchPlaceListResponse;
import kr.co.ongil.domain.map.dto.response.SearchPlaceResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapGeocodeResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapPoiSearchResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapReverseGeocodeResponse;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

            TmapReverseGeocodeResponse.AddressInfo addressInfo = response.addressInfo();

            // 도로명 주소 구성
            String roadAddress = null;
            if (addressInfo.roadName() != null) {
                roadAddress = String.format("%s %s %s %s",
                    addressInfo.cityDo() != null ? addressInfo.cityDo() : "",
                    addressInfo.guGun() != null ? addressInfo.guGun() : "",
                    addressInfo.roadName(),
                    addressInfo.buildingIndex() != null ? addressInfo.buildingIndex() : ""
                ).trim();

                // 건물명 추가
                if (addressInfo.buildingName() != null && !addressInfo.buildingName().isEmpty()) {
                    roadAddress += " " + addressInfo.buildingName();
                }
            }

            // 지번 주소 구성
            String jibunAddress = String.format("%s %s %s %s",
                addressInfo.cityDo() != null ? addressInfo.cityDo() : "",
                addressInfo.guGun() != null ? addressInfo.guGun() : "",
                addressInfo.legalDong() != null ? addressInfo.legalDong() : "",
                addressInfo.bunji() != null ? addressInfo.bunji() : ""
            ).trim();

            return AddressResponse.from(roadAddress, jibunAddress);

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
        // URL 인코딩
        String encodedCityDo = URLEncoder.encode(cityDo, StandardCharsets.UTF_8);
        String encodedGuGun = URLEncoder.encode(guGun, StandardCharsets.UTF_8);
        String encodedDong = URLEncoder.encode(dong, StandardCharsets.UTF_8);
        String encodedBunji = bunji != null
            ? URLEncoder.encode(bunji, StandardCharsets.UTF_8)
            : "";

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

            // 도로명주소 좌표가 있으면 우선 사용, 없으면 지번 좌표 사용
            String latitude = coordinateInfo.newLat() != null && !coordinateInfo.newLat().isEmpty()
                ? coordinateInfo.newLat()
                : coordinateInfo.latitude();
            String longitude = coordinateInfo.newLon() != null && !coordinateInfo.newLon().isEmpty()
                ? coordinateInfo.newLon()
                : coordinateInfo.longitude();

            // 좌표 값이 비어있는지 최종 검증
            if (latitude == null || latitude.isEmpty() || longitude == null || longitude.isEmpty()) {
                throw new BusinessException(ErrorCode.COORDINATE_NOT_FOUND);
            }

            return CoordinateResponse.of(
                Double.parseDouble(latitude),
                Double.parseDouble(longitude)
            );

        } catch (WebClientResponseException.TooManyRequests e) {
            log.error("Tmap API 호출 제한 초과", e);
            throw new BusinessException(ErrorCode.MAP_API_LIMIT_EXCEEDED);
        } catch (WebClientResponseException e) {
            log.error("Tmap API 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.MAP_API_ERROR);
        } catch (NumberFormatException e) {
            log.error("좌표 파싱 실패: latitude or longitude is empty", e);
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
        // Tmap API: radius는 km 단위 (미터 → km 변환 및 범위 제한: 1~33km)
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
                .map(poi -> {
                    // 주소: newAddressList의 fullAddressRoad 사용
                    String address = null;
                    if (poi.newAddressList() != null &&
                        poi.newAddressList().newAddress() != null &&
                        !poi.newAddressList().newAddress().isEmpty()) {
                        address = poi.newAddressList().newAddress().get(0).fullAddressRoad();
                    }

                    if (address == null || address.isEmpty()) {
                        address = String.format("%s %s %s",
                            poi.upperAddrName() != null ? poi.upperAddrName() : "",
                            poi.middleAddrName() != null ? poi.middleAddrName() : "",
                            poi.lowerAddrName() != null ? poi.lowerAddrName() : ""
                        ).trim();
                    }

                    // 위도/경도
                    String lat = poi.frontLat() != null && !poi.frontLat().isEmpty()
                        ? poi.frontLat()
                        : poi.noorLat();
                    String lon = poi.frontLon() != null && !poi.frontLon().isEmpty()
                        ? poi.frontLon()
                        : poi.noorLon();

                    // 거리: radius 필드 사용 (km → 미터 변환)
                    Integer distanceInMeters = null;
                    String distanceField = poi.radius();
                    if (distanceField != null && !distanceField.isEmpty()) {
                        try {
                            double distanceKm = Double.parseDouble(distanceField);
                            distanceInMeters = (int) (distanceKm * 1000);
                        } catch (NumberFormatException e) {
                            log.warn("거리 파싱 실패: {}", distanceField);
                        }
                    }

                    // 카테고리: 대/중/소 분류로 분리
                    SearchPlaceResponse.CategoryInfo category = SearchPlaceResponse.CategoryInfo.of(
                        poi.upperBizName(),
                        poi.middleBizName(),
                        poi.lowerBizName()
                    );

                    return SearchPlaceResponse.of(
                        poi.name(),
                        address,
                        lat != null && !lat.isEmpty() ? Double.parseDouble(lat) : null,
                        lon != null && !lon.isEmpty() ? Double.parseDouble(lon) : null,
                        distanceInMeters,
                        category,
                        poi.telNo()
                    );
                })
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
}