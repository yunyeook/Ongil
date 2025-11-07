package kr.co.ongil.domain.patient.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.map.dto.response.CoordinateInfo;
import kr.co.ongil.domain.patient.location.service.LocationService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.util.PatientAccessValidator;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/patients/{patientId}/location")
@RequiredArgsConstructor
@Tag(name = "Location API", description = "환자 위치 추적 API")
public class LocationController {

    private final LocationService locationService;

    /**
     * 환자가 GPS 위치 저장/업데이트
     */
    @PostMapping
    @Operation(summary = "GPS 위치 저장 및 업데이트", description = "환자의 현재 위치를 저장하거나 업데이트합니다.")
    public ApiResponse<String> updateLocation(
        @Parameter(description = "환자 ID") @PathVariable Integer patientId,
        @Parameter(description = "좌표 정보") @RequestBody @Valid CoordinateInfo coordinate
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();

        locationService.createOrUpdateLocation(coordinate, patientId,callerId);

        return ApiResponse.success(ResponseMessage.LOCATION_UPDATE);

    }

    /**
     * 환자 위치 조회
     */
    @GetMapping
    @Operation(summary = "최근 위치 조회", description = "환자의 최근 위치를 조회합니다.")
    public ApiResponse<CoordinateInfo> getLocation(
        @Parameter(description = "환자 ID") @PathVariable Integer patientId
    ) {
        Integer guardianId = SecurityUtil.getCurrentUserId();
        log.debug("guardianId: {})", guardianId);
        log.debug("patientId: {})", patientId);
        CoordinateInfo coordinate = locationService.getLocation(patientId,guardianId);

        return ApiResponse.success(ResponseMessage.REQUEST_SUCCESS, coordinate);
    }
}