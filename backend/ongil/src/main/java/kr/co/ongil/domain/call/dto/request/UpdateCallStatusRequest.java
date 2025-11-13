package kr.co.ongil.domain.call.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.ongil.domain.call.entity.CallStatus;

/**
 * VoIP 통화 상태 업데이트 Request
 */
@Schema(description = "VoIP 통화 상태 업데이트")
public record UpdateCallStatusRequest(

    @Schema(
        description = "통화 상태",
        example = "CONNECTED",
        allowableValues = {"RINGING", "CONNECTED", "ENDED", "CANCELED", "REJECTED", "FAILED", "MISSED", "DROPPED"}
    )
    @NotNull(message = "통화 상태는 필수입니다.")
    CallStatus status
) {
}
