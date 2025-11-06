package kr.co.ongil.domain.call.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.ongil.domain.call.entity.CallSource;
import kr.co.ongil.domain.call.entity.CallType;
import kr.co.ongil.domain.patient.entity.PatientState;

import java.time.LocalDateTime;

/**
 * 통화 로그 생성 Request (기본 전화 통화 후 클라이언트 콜백용)
 */
@Schema(description = "통화 로그 생성 (기본 전화)")
public record CreateCallLogRequest(

    @Schema(description = "수신자 ID", example = "2")
    @NotNull(message = "수신자 ID는 필수입니다.")
    Integer receiverId,

    @Schema(description = "통화 유형 (NORMAL, EMERGENCY)", example = "NORMAL")
    @NotNull(message = "통화 유형은 필수입니다.")
    CallType callType,

    @Schema(description = "통화 출처 (APP, SYSTEM_DIALER)", example = "SYSTEM_DIALER")
    @NotNull(message = "통화 출처는 필수입니다.")
    CallSource source,

    @Schema(description = "환자 상태 (NORMAL, NAVIGATING, ABNORMAL)", example = "NORMAL")
    @NotNull(message = "환자 상태는 필수입니다.")
    PatientState patientState,

    @Schema(
        description = "환자 위치 (JSON 형식)",
        example = "{\"latitude\": 37.5665, \"longitude\": 126.9780}"
    )
    String patientLocation,

    @Schema(description = "통화 시작 시간", example = "2025-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "통화 시작 시간은 필수입니다.")
    LocalDateTime startedAt,

    @Schema(description = "통화 종료 시간", example = "2025-01-15T10:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endedAt,

    @Schema(description = "통화 시간 (초)", example = "295")
    Integer duration,

    @Schema(description = "메모", example = "병원 가는 길에 전화함")
    String memo
) {
}
