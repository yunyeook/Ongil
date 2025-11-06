package kr.co.ongil.domain.patient.safezone.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.patient.safezone.entity.SafeZone;

import java.time.LocalDateTime;

@Schema(description = "안전범위 조회 응답")
public record SafeZoneResponse(

    @Schema(description = "환자 ID", example = "1")
    Integer patientId,

    @Schema(description = "안전범위 3단계 정보")
    BoundariesInfo boundaries,

    @Schema(description = "최종 수정 시각", example = "2025-10-22T12:00:00")
    LocalDateTime updatedAt
) {
    public static SafeZoneResponse from(SafeZone safeZone) {
        BoundaryInfo first = BoundaryInfo.of(
            safeZone.getFirstBoundary(),
            safeZone.getFirstTime()
        );

        BoundaryInfo second = BoundaryInfo.of(
            safeZone.getSecondBoundary(),
            safeZone.getSecondTime()
        );

        BoundaryInfo third = BoundaryInfo.of(
            safeZone.getThirdBoundary(),
            safeZone.getThirdTime()
        );

        BoundariesInfo boundaries = BoundariesInfo.of(first, second, third);

        return new SafeZoneResponse(
            safeZone.getPatient().getId(),
            boundaries,
            safeZone.getUpdatedAt()
        );
    }
}
