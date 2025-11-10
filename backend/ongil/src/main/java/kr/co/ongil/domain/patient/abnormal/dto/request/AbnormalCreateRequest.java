package kr.co.ongil.domain.patient.abnormal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.ongil.domain.patient.abnormal.entity.Abnormal;
import kr.co.ongil.domain.patient.abnormal.entity.AbnormalType;
import kr.co.ongil.domain.patient.safezone.entity.SafeZoneLevel;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;

@Schema(name = "AbnormalCreateRequest", description = "이상탐지 이벤트 등록 요청")
public record AbnormalCreateRequest(
    @Schema(description = "이상탐지 유형", example = "SAFEZONE_EXIT", required = true)
    @NotNull(message = "이상탐지 유형은 필수입니다.")
    String abnormalType,

    @Schema(description = "안전범위 단계 (안전범위 관련 이벤트만)", example = "SECOND")
    String safeZoneLevel,

    @Schema(description = "발생 위도 좌표", example = "37.5665", required = true)
    @NotNull(message = "위도는 필수입니다.")
    Double latitude,

    @Schema(description = "발생 경도 좌표", example = "126.9780", required = true)
    @NotNull(message = "경도는 필수입니다.")
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
    /**
     * AbnormalType Enum으로 변환
     */
    public AbnormalType getAbnormalType() {
        try {
            return AbnormalType.valueOf(abnormalType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * SafeZoneLevel Enum으로 변환
     */
    public SafeZoneLevel getSafeZoneLevel() {
        if (safeZoneLevel == null || safeZoneLevel.isEmpty()) {
            return null;
        }
        try {
            return SafeZoneLevel.valueOf(safeZoneLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }



    /**
     * Entity로 변환
     */
    public Abnormal to(User patient) {
        return Abnormal.builder()
            .patient(patient)
            .abnormalType(getAbnormalType())
            .safeZoneLevel(getSafeZoneLevel())
            .latitude(latitude)
            .longitude(longitude)
            .centerLatitude(centerLatitude)
            .centerLongitude(centerLongitude)
            .distanceFromCenter(distanceFromCenter)
            .boundaryRadius(boundaryRadius)
            .elapsedTime(elapsedTime)
            .thresholdTime(thresholdTime)
            .build();
    }
}