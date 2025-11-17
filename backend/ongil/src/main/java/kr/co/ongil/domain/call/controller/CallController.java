package kr.co.ongil.domain.call.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.call.dto.request.CreateCallRequest;
import kr.co.ongil.domain.call.dto.request.UpdateCallStatusRequest;
import kr.co.ongil.domain.call.dto.response.CallResponse;
import kr.co.ongil.domain.call.dto.response.TurnCredentialsResponse;
import kr.co.ongil.domain.call.service.CallService;
import kr.co.ongil.domain.call.service.TurnCredentialsService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.security.userdetails.CustomUserDetails;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * VoIP 통화 컨트롤러
 * 앱 내 실시간 통화 세션 관리
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/calls")
@RequiredArgsConstructor
@Tag(name = "Call API", description = "VoIP 통화 관련 API")
public class CallController {

    private final CallService callService;
    private final TurnCredentialsService turnCredentialsService;

    /**
     * VoIP 통화 요청 생성
     */
    @PostMapping
    @Operation(summary = "VoIP 통화 요청 생성", description = "앱 내 VoIP 통화 세션을 생성합니다.")
    public ApiResponse<CallResponse> createCall(
        @Valid @RequestBody CreateCallRequest request
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        CallResponse response = callService.createCall(callerId, request);

        return ApiResponse.success(ResponseMessage.CALL_REQUESTED, response);
    }

    /**
     * 발신자 준비 완료 알림
     */
    @PostMapping("/{callId}/caller-ready")
    public ApiResponse<Void> notifyCallerReady(
            @PathVariable Integer callId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        callService.notifyCallerReady(callId, userDetails.getUserId());
        return ApiResponse.success(ResponseMessage.CALL_REQUIRED, null);
    }

    /**
     * VoIP 통화 상태 업데이트
     */
    @PutMapping("/{callId}/status")
    @Operation(summary = "VoIP 통화 상태 업데이트", description = "통화 상태를 변경합니다 (RINGING, CONNECTED, ENDED 등).")
    public ApiResponse<CallResponse> updateCallStatus(
        @Parameter(description = "통화 ID", example = "1")
        @PathVariable Integer callId,

        @Valid @RequestBody UpdateCallStatusRequest request
    ) {
        CallResponse response = callService.updateCallStatus(callId, request);

        return ApiResponse.success(ResponseMessage.CALL_ACCEPTED, response);
    }

    /**
     * VoIP 통화 조회 (세션 ID로)
     */
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "세션 ID로 통화 조회", description = "VoIP 세션 ID로 통화 정보를 조회합니다.")
    public ApiResponse<CallResponse> getCallBySessionId(
        @Parameter(description = "세션 ID", example = "webrtc-session-12345")
        @PathVariable String sessionId
    ) {
        CallResponse response = callService.getCallBySessionId(sessionId);

        return ApiResponse.success(ResponseMessage.CALL_ACCEPTED, response);
    }

    /**
     * VoIP 통화 조회 (통화 ID로)
     */
    @GetMapping("/{callId}")
    @Operation(summary = "통화 ID로 조회", description = "통화 ID로 VoIP 세션 정보를 조회합니다.")
    public ApiResponse<CallResponse> getCallById(
        @Parameter(description = "통화 ID", example = "1")
        @PathVariable Integer callId
    ) {
        CallResponse response = callService.getCallById(callId);

        return ApiResponse.success(ResponseMessage.CALL_ACCEPTED, response);
    }

    /**
     * TURN/STUN 서버 자격증명 발급
     */
    @GetMapping("/rtc/turn-credentials")
    @Operation(
        summary = "TURN/STUN 자격증명 발급",
        description = "WebRTC P2P 연결을 위한 TURN/STUN 서버 자격증명을 발급합니다. (TTL: 1시간)"
    )
    public ApiResponse<TurnCredentialsResponse> getTurnCredentials() {
        TurnCredentialsResponse response = turnCredentialsService.generateCredentials();

        return ApiResponse.success(ResponseMessage.CALL_ACCEPTED, response);
    }
}
