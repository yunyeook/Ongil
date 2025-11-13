package kr.co.ongil.domain.patient.abnormal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.patient.abnormal.dto.request.AbnormalCreateRequest;
import kr.co.ongil.domain.patient.abnormal.dto.request.AbnormalSearchRequest;
import kr.co.ongil.domain.patient.abnormal.dto.response.AbnormalListResponse;
import kr.co.ongil.domain.patient.abnormal.dto.response.AbnormalResponse;
import kr.co.ongil.domain.patient.abnormal.service.AbnormalService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/patients/{patientId}/abnormals")
@RequiredArgsConstructor
@Tag(name = "Abnormal API", description = "이상탐지 이벤트 관련 API")
public class AbnormalController {

    private final AbnormalService abnormalService;

    @GetMapping
    @Operation(
        summary = "이상탐지 이벤트 목록 조회",
        description = "지정 기간 동안 환자에게 발생한 이상탐지 이벤트 목록을 조회합니다. " +
            "(안전범위 이탈, 배회 감지, 길찾기 실패, 응급 상황 등)"
    )
    public ApiResponse<AbnormalListResponse> getAbnormals(
        @Parameter(description = "환자 고유 ID") @PathVariable Integer patientId,
        @Parameter(description = "필터링할 이상탐지 유형") @RequestParam(required = false) String type,
        @Parameter(description = "안전범위 단계 필터") @RequestParam(required = false) String level,
        @Parameter(description = "조회 시작날짜 (YYYYMMDD)") @RequestParam(required = false) String from,
        @Parameter(description = "조회 종료날짜 (YYYYMMDD)") @RequestParam(required = false) String to,
        @Parameter(description = "페이지 번호 (1부터 시작)") @RequestParam(required = false) Integer page,
        @Parameter(description = "한 페이지당 개수") @RequestParam(required = false) Integer size
    ) {
        Integer senderId = SecurityUtil.getCurrentUserId();

        AbnormalSearchRequest searchRequest = AbnormalSearchRequest.of(
            type, level, from, to, page, size
        );

        AbnormalListResponse response = abnormalService.getAbnormals(senderId, patientId, searchRequest);
        return ApiResponse.success(ResponseMessage.ABNORMAL_LIST_FOUND, response);
    }

    @GetMapping("/{abnormalId}")
    @Operation(
        summary = "이상탐지 이벤트 상세 조회",
        description = "특정 이상탐지 이벤트의 세부 정보를 조회합니다."
    )
    public ApiResponse<AbnormalResponse> getAbnormalDetail(
        @Parameter(description = "환자 고유 ID") @PathVariable Integer patientId,
        @Parameter(description = "이상탐지 이벤트 ID") @PathVariable Integer abnormalId
    ) {
        Integer senderId = SecurityUtil.getCurrentUserId();
        AbnormalResponse response = abnormalService.getAbnormalDetail(senderId, patientId, abnormalId);
        return ApiResponse.success(ResponseMessage.ABNORMAL_DETAIL_FOUND, response);
    }

    @PostMapping
    @Operation(
        summary = "이상탐지 이벤트 등록",
        description = "환자의 이상탐지 이벤트를 등록합니다. (안전범위 이탈, 배회 감지, 경로 이탈 등)"
    )
    public ApiResponse<AbnormalResponse> createAbnormal(
        @Parameter(description = "환자 고유 ID") @PathVariable Integer patientId,
        @Valid @RequestBody AbnormalCreateRequest request
    ) {
        Integer senderId = SecurityUtil.getCurrentUserId();

        AbnormalResponse response = abnormalService.createAbnormal(senderId,patientId, request);
        return ApiResponse.success(ResponseMessage.ABNORMAL_CREATED, response);
    }
}