package kr.co.ongil.domain.call.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.call.entity.Call;
import kr.co.ongil.domain.call.entity.CallStatus;
import kr.co.ongil.domain.call.entity.CallType;

import java.time.LocalDateTime;

/**
 * VoIP 통화 세션 응답
 */
@Schema(description = "VoIP 통화 세션 정보")
public record CallResponse(

    @Schema(description = "통화 ID", example = "1")
    Integer id,

    @Schema(description = "발신자 정보")
    UserInfo caller,

    @Schema(description = "수신자 정보")
    UserInfo receiver,

    @Schema(description = "통화 유형", example = "NORMAL")
    CallType callType,

    @Schema(description = "통화 상태", example = "CONNECTED")
    CallStatus status,

    @Schema(description = "VoIP 세션 ID", example = "webrtc-session-12345")
    String sessionId,

    @Schema(description = "통화 시작 시간", example = "2025-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startedAt,

    @Schema(description = "통화 연결 시간", example = "2025-01-15T10:30:05")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime connectedAt,

    @Schema(description = "통화 종료 시간", example = "2025-01-15T10:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endedAt,

    @Schema(description = "통화 시간 (초)", example = "295")
    Integer duration
) {

    /**
     * Entity -> Response 변환
     */
    public static CallResponse from(Call call) {
        return new CallResponse(
            call.getId(),
            UserInfo.from(call.getCaller()),
            UserInfo.from(call.getReceiver()),
            call.getCallType(),
            call.getStatus(),
            call.getSessionId(),
            call.getStartedAt(),
            call.getConnectedAt(),
            call.getEndedAt(),
            call.getDuration()
        );
    }

    /**
     * 사용자 정보 (간략)
     */
    @Schema(description = "사용자 정보")
    public record UserInfo(

        @Schema(description = "사용자 ID", example = "1")
        Integer id,

        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber
    ) {

        public static UserInfo from(kr.co.ongil.domain.user.entity.User user) {
            return new UserInfo(
                user.getId(),
                user.getName(),
                user.getPhoneNumber()
            );
        }
    }
}
