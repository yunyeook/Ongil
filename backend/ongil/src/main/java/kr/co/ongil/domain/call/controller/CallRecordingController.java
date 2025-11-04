package kr.co.ongil.domain.call.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.call.dto.request.CreateCallRecordingRequest;
import kr.co.ongil.domain.call.dto.response.CallRecordingResponse;
import kr.co.ongil.domain.call.service.CallRecordingService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 통화 녹음 메타데이터 컨트롤러
 * 실제 녹음 파일은 클라이언트 로컬에 저장되며, 서버에는 메타데이터만 관리됩니다.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/calls/recordings")
@RequiredArgsConstructor
@Tag(name = "Call Recording API", description = "통화 녹음 메타데이터 관련 API")
public class CallRecordingController {

    private final CallRecordingService callRecordingService;

    /**
     * 통화 녹음 메타데이터 생성
     */
    @PostMapping
    @Operation(summary = "통화 녹음 메타데이터 생성", description = "통화 녹음 파일의 메타데이터를 등록합니다.")
    public ApiResponse<CallRecordingResponse> createCallRecording(
        @Valid @RequestBody CreateCallRecordingRequest request
    ) {
        CallRecordingResponse response = callRecordingService.createCallRecording(request);

        return ApiResponse.success(ResponseMessage.CALL_RECORDING_CREATED, response);
    }

    /**
     * 통화 녹음 정보 조회 (녹음 ID로)
     */
    @GetMapping("/{recordingId}")
    @Operation(summary = "통화 녹음 정보 조회", description = "녹음 ID로 통화 녹음 메타데이터를 조회합니다.")
    public ApiResponse<CallRecordingResponse> getCallRecording(
        @Parameter(description = "녹음 ID", example = "1")
        @PathVariable Integer recordingId
    ) {
        CallRecordingResponse response = callRecordingService.getCallRecording(recordingId);

        return ApiResponse.success(ResponseMessage.CALL_RECORDING_FOUND, response);
    }

    /**
     * 통화 로그로 녹음 정보 조회
     */
    @GetMapping("/call-log/{callLogId}")
    @Operation(summary = "통화 로그로 녹음 정보 조회", description = "통화 로그 ID로 연결된 녹음 메타데이터를 조회합니다.")
    public ApiResponse<CallRecordingResponse> getCallRecordingByCallLogId(
        @Parameter(description = "통화 로그 ID", example = "1")
        @PathVariable Integer callLogId
    ) {
        CallRecordingResponse response = callRecordingService.getCallRecordingByCallLogId(callLogId);

        return ApiResponse.success(ResponseMessage.CALL_RECORDING_FOUND, response);
    }

    /**
     * 통화 녹음 메타데이터 업데이트
     */
    @PatchMapping("/{recordingId}")
    @Operation(summary = "통화 녹음 메타데이터 업데이트", description = "녹음 파일의 크기와 길이 정보를 업데이트합니다.")
    public ApiResponse<CallRecordingResponse> updateCallRecording(
        @Parameter(description = "녹음 ID", example = "1")
        @PathVariable Integer recordingId,

        @Parameter(description = "파일 크기 (바이트)", example = "1048576")
        @RequestParam(required = false) Long fileSize,

        @Parameter(description = "녹음 길이 (초)", example = "295")
        @RequestParam(required = false) Integer duration
    ) {
        CallRecordingResponse response = callRecordingService.updateCallRecording(recordingId, fileSize, duration);

        return ApiResponse.success(ResponseMessage.CALL_RECORDING_FOUND, response);
    }

    /**
     * 통화 녹음 메타데이터 삭제
     */
    @DeleteMapping("/{recordingId}")
    @Operation(summary = "통화 녹음 메타데이터 삭제", description = "통화 녹음 메타데이터를 삭제합니다. (실제 파일은 클라이언트에서 삭제)")
    public ApiResponse<String> deleteCallRecording(
        @Parameter(description = "녹음 ID", example = "1")
        @PathVariable Integer recordingId
    ) {
        callRecordingService.deleteCallRecording(recordingId);

        return ApiResponse.success(ResponseMessage.CALL_RECORDING_DELETED);
    }
}
