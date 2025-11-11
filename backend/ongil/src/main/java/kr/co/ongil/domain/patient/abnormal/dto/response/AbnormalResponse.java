package kr.co.ongil.domain.patient.abnormal.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.patient.abnormal.entity.Abnormal;
import kr.co.ongil.domain.patient.abnormal.entity.AbnormalType;

import java.time.LocalDateTime;
import kr.co.ongil.domain.patient.safezone.entity.SafeZoneLevel;

@Schema(name = "AbnormalResponse", description = "이상탐지 이벤트 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AbnormalResponse(
    @Schema(description = "이상탐지 이벤트 ID", example = "101")
    Integer abnormalId,

    @Schema(description = "이상탐지 유형", implementation = AbnormalType.class, example = "SAFEZONE_EXIT")
    AbnormalType abnormalType,

    @Schema(description = "안전범위 단계 (1단계/2단계/3단계)", implementation = SafeZoneLevel.class, example = "FIRST")
    SafeZoneLevel safeZoneLevel,

    @Schema(description = "발생 시각", type = "string", format = "date-time", example = "2025-10-22T14:12:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "발생 위도 좌표", example = "37.5665")
    Double latitude,

    @Schema(description = "발생 경도 좌표", example = "126.9780")
    Double longitude,

    @Schema(description = "안전범위 중심 위도 좌표", example = "37.5660")
    Double centerLatitude,

    @Schema(description = "안전범위 중심 경도 좌표", example = "126.9775")
    Double centerLongitude,

    @Schema(description = "중심으로부터의 거리 (미터)", example = "520.5")
    Double distanceFromCenter,

    @Schema(description = "해당 단계의 경계 반경 (미터)", example = "500.0")
    Double boundaryRadius,

    @Schema(description = "배회 시 경과 시간 (초)", example = "35")
    Integer elapsedTime,

    @Schema(description = "배회 기준 시간 (초)", example = "30")
    Integer thresholdTime

) {
    public static AbnormalResponse from(Abnormal abnormal) {
        return new AbnormalResponse(
            abnormal.getId(),
            abnormal.getAbnormalType(),
            abnormal.getSafeZoneLevel(),
            abnormal.getCreatedAt(),
            abnormal.getLatitude(),
            abnormal.getLongitude(),
            abnormal.getCenterLatitude(),
            abnormal.getCenterLongitude(),
            abnormal.getDistanceFromCenter(),
            abnormal.getBoundaryRadius(),
            abnormal.getElapsedTime(),
            abnormal.getThresholdTime()
        );
    }
}