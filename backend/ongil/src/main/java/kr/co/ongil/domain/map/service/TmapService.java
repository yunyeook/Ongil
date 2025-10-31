package kr.co.ongil.domain.map.service;

import kr.co.ongil.domain.map.dto.response.AddressResponse;
import kr.co.ongil.domain.map.dto.response.CoordinateResponse;
import kr.co.ongil.domain.map.dto.tmap.TmapGeocodeResponse;
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
}