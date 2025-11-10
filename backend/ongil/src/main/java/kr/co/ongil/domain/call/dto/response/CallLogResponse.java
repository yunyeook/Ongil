package kr.co.ongil.domain.call.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.call.entity.CallLog;
import kr.co.ongil.domain.call.entity.CallSource;
import kr.co.ongil.domain.call.entity.CallType;
import kr.co.ongil.domain.patient.entity.PatientState;

import java.time.LocalDateTime;

/**
 * 통화 로그 응답
 */
@Schema(description = "통화 로그 정보")
public record CallLogResponse(

    @Schema(description = "통화 로그 ID", example = "1")
    Integer id,

    @Schema(description = "발신자 정보")
    CallResponse.UserInfo caller,

    @Schema(description = "수신자 정보 (앱 내 사용자인 경우)")
    CallResponse.UserInfo receiver,

    @Schema(description = "수신자 전화번호 (외부 번호인 경우)", example = "01012345678")
    String receiverPhoneNumber,

    @Schema(description = "통화 유형", example = "NORMAL")
    CallType callType,

    @Schema(description = "통화 출처", example = "APP")
    CallSource source,

    @Schema(description = "환자 상태", example = "NORMAL")
    PatientState patientState,

    @Schema(
        description = "환자 위치 (JSON 형식)",
        example = "{\"latitude\": 37.5665, \"longitude\": 126.9780}"
    )
    String patientLocation,

    @Schema(description = "통화 시작 시간", example = "2025-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startedAt,

    @Schema(description = "통화 종료 시간", example = "2025-01-15T10:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endedAt,

    @Schema(description = "통화 시간 (초)", example = "295")
    Integer duration,

    @Schema(description = "메모", example = "병원 가는 길에 전화함")
    String memo,

    @Schema(description = "생성 시간", example = "2025-01-15T10:36:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt
) {

    /**
     * Entity -> Response 변환
     */
    public static CallLogResponse from(CallLog callLog) {
        return new CallLogResponse(
            callLog.getId(),
            CallResponse.UserInfo.from(callLog.getCaller()),
            callLog.getReceiver() != null ? CallResponse.UserInfo.from(callLog.getReceiver()) : null,
            callLog.getReceiverPhoneNumber(),
            callLog.getCallType(),
            callLog.getSource(),
            callLog.getPatientState(),
            callLog.getPatientLocation(),
            callLog.getStartedAt(),
            callLog.getEndedAt(),
            callLog.getDuration(),
            callLog.getMemo(),
            callLog.getCreatedAt()
        );
    }
}
