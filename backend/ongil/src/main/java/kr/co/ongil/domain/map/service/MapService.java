package kr.co.ongil.domain.map.service;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import kr.co.ongil.domain.map.dto.response.AddressResponse;
import kr.co.ongil.domain.map.dto.response.CoordinateResponse;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {

    private final TmapService tmapService;

    /**
     * 좌표로 주소 조회
     */
    public AddressResponse getAddress(Double latitude, Double longitude) {
        log.info("좌표 → 주소 변환 요청: lat={}, lng={}", latitude, longitude);

        // 좌표 유효성 검증
        validateCoordinate(latitude, longitude);

        return tmapService.getAddressFromCoordinate(latitude, longitude);
    }

    /**
     * 주소로 좌표 조회
     */
    public CoordinateResponse getCoordinate(String cityDo, String guGun, String dong, String bunji) {
        log.info("주소 → 좌표 변환 요청: {} {} {} {}", cityDo, guGun, dong, bunji);

        // 주소 유효성 검증
        validateAddress(cityDo, guGun, dong);

        return tmapService.getCoordinateFromAddress(cityDo, guGun, dong, bunji);
    }

    /**
     * 좌표 유효성 검증
     */
    private void validateCoordinate(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_COORDINATE);
        }

        // 대한민국 좌표 범위 검증
        if (latitude < 33.0 || latitude > 43.0) {
            throw new BusinessException(ErrorCode.INVALID_LATITUDE);
        }

        if (longitude < 124.0 || longitude > 132.0) {
            throw new BusinessException(ErrorCode.INVALID_LONGITUDE);
        }
    }

    /**
     * 주소 유효성 검증
     */
    private void validateAddress(String cityDo, String guGun, String dong) {
        if (cityDo == null || cityDo.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ADDRESS);
        }
        if (guGun == null || guGun.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ADDRESS);
        }
        if (dong == null || dong.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ADDRESS);
        }
    }
}