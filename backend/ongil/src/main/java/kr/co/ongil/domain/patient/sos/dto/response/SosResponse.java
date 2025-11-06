package kr.co.ongil.domain.patient.sos.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.patient.sos.entity.Sos;

import java.time.LocalDateTime;

@Schema(description = "SOS 응답")
public record SosResponse(
    @Schema(description = "sos 고유 ID", example = "3456")
    Long sosId,

    @Schema(description = "sos를 보낸 보호자 고유 ID", example = "9")
    Integer senderId,

    @Schema(description = "sos를 받을 환자 고유 ID", example = "1")
    Integer receiverId,
    @Schema(description = "음성 도움요청을 보낸 시간", example = "2025-10-22T14:35:10")
    LocalDateTime createdAt
) {
    public static SosResponse from(Sos sos) {
        return new SosResponse(
            sos.getId().longValue(),
            sos.getGuardian().getId(),
            sos.getPatient().getId(),
            sos.getCreatedAt()
        );
    }
}