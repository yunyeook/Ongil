package kr.co.ongil.domain.patient.safezone.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.patient.safezone.dto.request.SafeZonePatchRequest;
import kr.co.ongil.domain.patient.safezone.dto.request.SafeZoneUpsertRequest;
import kr.co.ongil.domain.patient.safezone.dto.response.SafeZoneResponse;
import kr.co.ongil.domain.patient.safezone.service.SafezoneService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/patients/{patientId}/safezone")
@RequiredArgsConstructor
@Tag(name = "SafeZone API", description = "안전범위 관련 API")
public class SafezoneController {

    private final SafezoneService safezoneService;

    /**
     * 안전범위 생성 또는 전체 교체 (Upsert)
     */
    @PutMapping
    @Operation(summary = "안전범위 설정", description = "환자의 안전범위를 신규 생성하거나 전체 교체합니다.")
    public ApiResponse<SafeZoneResponse> upsertSafeZone(
        @Parameter(description = "환자 ID", required = true, example = "1")
        @PathVariable Integer patientId,

        @Valid @RequestBody SafeZoneUpsertRequest request
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        SafeZoneResponse response = safezoneService.upsertSafeZone(patientId, request, callerId);

        // 신규 생성인지 수정인지 구분하여 메시지 반환
        ResponseMessage message = response.updatedAt().equals(response.updatedAt())
            ? ResponseMessage.SAFEZONE_CREATED
            : ResponseMessage.SAFEZONE_UPDATED;

        return ApiResponse.success(message, response);
    }

    /**
     * 안전범위 기본값으로 복원
     */
    @PutMapping("/reset")
    @Operation(summary = "안전범위 기본값 복원", description = "환자의 안전범위 설정을 서버 기본값으로 복원합니다.")
    public ApiResponse<SafeZoneResponse> resetSafeZone(
        @Parameter(description = "환자 ID", required = true, example = "1")
        @PathVariable Integer patientId
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        SafeZoneResponse response = safezoneService.resetSafeZone(patientId, callerId);
        return ApiResponse.success(ResponseMessage.SAFEZONE_RESET, response);
    }

    /**
     * 안전범위 부분 수정
     */
    @PatchMapping
    @Operation(summary = "안전범위 부분 수정", description = "환자의 안전범위를 부분적으로 수정합니다.")
    public ApiResponse<SafeZoneResponse> patchSafeZone(
        @Parameter(description = "환자 ID", required = true, example = "1")
        @PathVariable Integer patientId,

        @Valid @RequestBody SafeZonePatchRequest request
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        SafeZoneResponse response = safezoneService.patchSafeZone(patientId, request, callerId);
        return ApiResponse.success(ResponseMessage.SAFEZONE_PARTIALLY_UPDATED, response);
    }

    /**
     * 안전범위 조회
     */
    @GetMapping
    @Operation(summary = "안전범위 조회", description = "환자의 안전범위와 이상탐지 시간을 조회합니다.")
    public ApiResponse<SafeZoneResponse> getSafeZone(
        @Parameter(description = "환자 ID", required = true, example = "1")
        @PathVariable Integer patientId
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        SafeZoneResponse response = safezoneService.getSafeZone(patientId, callerId);
        return ApiResponse.success(ResponseMessage.SAFEZONE_FOUND, response);
    }
}
