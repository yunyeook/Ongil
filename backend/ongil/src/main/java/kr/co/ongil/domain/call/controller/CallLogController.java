package kr.co.ongil.domain.call.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.call.dto.request.CreateCallLogRequest;
import kr.co.ongil.domain.call.dto.response.CallLogResponse;
import kr.co.ongil.domain.call.service.CallLogService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 통화 로그 컨트롤러
 * - 통화 기록 조회 및 관리
 * - 기본 전화 통화 로그 생성 (클라이언트 콜백)
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/calls/logs")
@RequiredArgsConstructor
@Tag(name = "Call Log API", description = "통화 로그 관련 API")
public class CallLogController {

    private final CallLogService callLogService;

    /**
     * 기본 전화 통화 로그 생성 (클라이언트 콜백)
     */
    @PostMapping
    @Operation(summary = "통화 로그 생성", description = "기본 전화 통화 종료 후 클라이언트가 통화 로그를 등록합니다.")
    public ApiResponse<CallLogResponse> createCallLog(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody CreateCallLogRequest request
    ) {
        Integer callerId = Integer.parseInt(userDetails.getUsername());
        CallLogResponse response = callLogService.createCallLog(callerId, request);

        return ApiResponse.success(ResponseMessage.CALL_LOG_CREATED, response);
    }

    /**
     * 내 통화 로그 목록 조회 (페이징)
     */
    @GetMapping
    @Operation(summary = "통화 로그 목록 조회", description = "사용자의 통화 로그 목록을 페이징하여 조회합니다.")
    public ApiResponse<List<CallLogResponse>> getCallLogs(
        @AuthenticationPrincipal UserDetails userDetails,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        Page<CallLogResponse> page = callLogService.getCallLogs(userId, pageable);

        return ApiResponse.success(ResponseMessage.CALL_LOG_LIST_FOUND, page.getContent());
    }

    /**
     * 통화 로그 단건 조회
     */
    @GetMapping("/{callLogId}")
    @Operation(summary = "통화 로그 조회", description = "통화 로그 ID로 상세 정보를 조회합니다.")
    public ApiResponse<CallLogResponse> getCallLog(
        @Parameter(description = "통화 로그 ID", example = "1")
        @PathVariable Integer callLogId
    ) {
        CallLogResponse response = callLogService.getCallLog(callLogId);

        return ApiResponse.success(ResponseMessage.CALL_LOG_FOUND, response);
    }

    /**
     * 긴급 통화 기록 조회
     */
    @GetMapping("/emergency")
    @Operation(summary = "긴급 통화 기록 조회", description = "사용자의 긴급 통화 기록만 조회합니다.")
    public ApiResponse<List<CallLogResponse>> getEmergencyCallLogs(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        List<CallLogResponse> response = callLogService.getEmergencyCallLogs(userId);

        return ApiResponse.success(ResponseMessage.CALL_LOG_LIST_FOUND, response);
    }

    /**
     * 기간별 통화 로그 조회
     */
    @GetMapping("/date-range")
    @Operation(summary = "기간별 통화 로그 조회", description = "특정 기간 내의 통화 로그를 조회합니다.")
    public ApiResponse<List<CallLogResponse>> getCallLogsByDateRange(
        @AuthenticationPrincipal UserDetails userDetails,

        @Parameter(description = "시작 날짜", example = "2025-01-01T00:00:00")
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startDate,

        @Parameter(description = "종료 날짜", example = "2025-01-31T23:59:59")
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endDate
    ) {
        Integer userId = Integer.parseInt(userDetails.getUsername());
        List<CallLogResponse> response = callLogService.getCallLogsByDateRange(userId, startDate, endDate);

        return ApiResponse.success(ResponseMessage.CALL_LOG_LIST_FOUND, response);
    }

    /**
     * 통화 로그 메모 추가/수정
     */
    @PatchMapping("/{callLogId}/memo")
    @Operation(summary = "통화 로그 메모 수정", description = "통화 로그에 메모를 추가하거나 수정합니다.")
    public ApiResponse<CallLogResponse> updateMemo(
        @Parameter(description = "통화 로그 ID", example = "1")
        @PathVariable Integer callLogId,

        @Parameter(description = "메모 내용", example = "병원 가는 길에 전화함")
        @RequestParam String memo
    ) {
        CallLogResponse response = callLogService.updateMemo(callLogId, memo);

        return ApiResponse.success(ResponseMessage.CALL_LOG_FOUND, response);
    }

    /**
     * 통화 로그 삭제
     */
    @DeleteMapping("/{callLogId}")
    @Operation(summary = "통화 로그 삭제", description = "통화 로그를 삭제합니다.")
    public ApiResponse<String> deleteCallLog(
        @Parameter(description = "통화 로그 ID", example = "1")
        @PathVariable Integer callLogId
    ) {
        callLogService.deleteCallLog(callLogId);

        return ApiResponse.success(ResponseMessage.CALL_LOG_DELETED);
    }
}
