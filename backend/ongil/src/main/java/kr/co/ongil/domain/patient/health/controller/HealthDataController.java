package kr.co.ongil.domain.patient.health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.patient.health.dto.request.HealthDataUploadRequest;
import kr.co.ongil.domain.patient.health.dto.response.HealthDataListResponse;
import kr.co.ongil.domain.patient.health.dto.response.HealthDataSummaryResponse;
import kr.co.ongil.domain.patient.health.dto.response.HealthDataUploadResponse;
import kr.co.ongil.domain.patient.health.entity.HealthDataType;
import kr.co.ongil.domain.patient.health.service.HealthDataService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 건강 데이터 컨트롤러
 * Samsung Health SDK에서 수집한 생체 데이터 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/patients/{patientId}/health-data")
@RequiredArgsConstructor
@Tag(name = "Health Data API", description = "환자 생체 데이터 API")
public class HealthDataController {

    private final HealthDataService healthDataService;

    /**
     * 생체 데이터 업로드
     * POST /api/v1/patients/{patientId}/health-data
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "생체 데이터 업로드",
        description = "Samsung Health에서 수집한 생체 데이터를 서버로 업로드합니다. " +
            "심박수, 걸음수, 수면, 혈중산소 등 다양한 타입의 데이터를 한 번에 전송할 수 있습니다."
    )
    public ApiResponse<HealthDataUploadResponse> uploadHealthData(
        @Parameter(description = "환자 ID", example = "1", required = true)
        @PathVariable Integer patientId,

        @Valid @RequestBody HealthDataUploadRequest request
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        Integer count = healthDataService.uploadHealthData(patientId, request, callerId);
        HealthDataUploadResponse response = HealthDataUploadResponse.from(count);
        return ApiResponse.success(ResponseMessage.HEALTH_DATA_UPLOADED, response);
    }

    /**
     * 생체 데이터 조회
     * GET /api/v1/patients/{patientId}/health-data?type=HEART_RATE&from=20251017&to=20251018&sort=measuredAt,desc
     */
    @GetMapping
    @Operation(
        summary = "생체 데이터 조회",
        description = "특정 환자의 생체 데이터(심박수, 걸음수, 수면, 산소포화도 등)를 기간별·유형별로 조회합니다."
    )
    public ApiResponse<HealthDataListResponse> getHealthData(
        @Parameter(description = "환자 ID", example = "1", required = true)
        @PathVariable Integer patientId,

        @Parameter(description = "조회할 데이터 종류 (null이면 전체)", example = "HEART_RATE")
        @RequestParam(required = false) HealthDataType type,

        @Parameter(description = "조회 시작 날짜 (yyyyMMdd)", example = "20251017")
        @RequestParam(required = false)
        @DateTimeFormat(pattern = "yyyyMMdd") LocalDate from,

        @Parameter(description = "조회 종료 날짜 (yyyyMMdd)", example = "20251018")
        @RequestParam(required = false)
        @DateTimeFormat(pattern = "yyyyMMdd") LocalDate to,

        @Parameter(description = "정렬 기준 (measuredAt,desc 기본)", example = "measuredAt,desc")
        @RequestParam(required = false, defaultValue = "measuredAt,desc") String sort
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        Sort sortObj = parseSort(sort);
        HealthDataListResponse response = healthDataService.getHealthData(
            patientId, type, from, to, sortObj, callerId
        );
        return ApiResponse.success(ResponseMessage.HEALTH_DATA_FOUND, response);
    }

    /**
     * 생체 데이터 요약 통계 조회
     * GET /api/v1/patients/{patientId}/health-data/summary?type=HEART_RATE&from=20251010&to=20251017
     */
    @GetMapping("/summary")
    @Operation(
        summary = "생체 데이터 요약 통계 조회",
        description = "환자의 생체 데이터를 일별 단위로 요약하여 반환합니다. " +
            "심박수, 걸음수, 수면시간 등의 평균·최대·최소·합계를 통계 형태로 제공합니다."
    )
    public ApiResponse<HealthDataSummaryResponse> getHealthDataSummary(
        @Parameter(description = "환자 ID", example = "1", required = true)
        @PathVariable Integer patientId,

        @Parameter(description = "통계할 데이터 종류 (null이면 전체)", example = "HEART_RATE")
        @RequestParam(required = false) HealthDataType type,

        @Parameter(description = "조회 시작 날짜 (yyyyMMdd)", example = "20251010")
        @RequestParam(required = false)
        @DateTimeFormat(pattern = "yyyyMMdd") LocalDate from,

        @Parameter(description = "조회 종료 날짜 (yyyyMMdd)", example = "20251017")
        @RequestParam(required = false)
        @DateTimeFormat(pattern = "yyyyMMdd") LocalDate to
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        HealthDataSummaryResponse response = healthDataService.getHealthDataSummary(
            patientId, type, from, to, callerId
        );
        return ApiResponse.success(ResponseMessage.HEALTH_DATA_SUMMARY_FOUND, response);
    }

    /**
     * 생체 데이터 삭제
     * DELETE /api/v1/patients/{patientId}/health-data/{healthDataId}
     */
    @DeleteMapping("/{healthDataId}")
    @Operation(
        summary = "생체 데이터 삭제",
        description = "환자의 특정 생체 데이터를 삭제합니다."
    )
    public ApiResponse<String> deleteHealthData(
        @Parameter(description = "환자 ID", example = "1", required = true)
        @PathVariable Integer patientId,

        @Parameter(description = "건강 데이터 ID", example = "1001", required = true)
        @PathVariable Integer healthDataId
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        healthDataService.deleteHealthData(patientId, healthDataId, callerId);
        return ApiResponse.success(ResponseMessage.HEALTH_DATA_DELETED, "");
    }

    /**
     * 정렬 문자열 파싱 (measuredAt,desc -> Sort 객체)
     */
    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "measuredAt");
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
