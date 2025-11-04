package kr.co.ongil.domain.call.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.ongil.domain.call.entity.CallType;

/**
 * VoIP 통화 요청 생성 Request
 */
@Schema(description = "VoIP 통화 요청 생성")
public record CreateCallRequest(

    @Schema(description = "수신자 ID", example = "2")
    @NotNull(message = "수신자 ID는 필수입니다.")
    Integer receiverId,

    @Schema(description = "통화 유형 (NORMAL, EMERGENCY)", example = "NORMAL")
    @NotNull(message = "통화 유형은 필수입니다.")
    CallType callType,

    @Schema(description = "VoIP 세션 ID (WebRTC 등)", example = "webrtc-session-12345")
    @NotNull(message = "세션 ID는 필수입니다.")
    String sessionId
) {
}
