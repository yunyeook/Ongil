package kr.co.ongil.domain.patient.insight.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.patient.insight.dto.internal.ActivityStats;
import kr.co.ongil.domain.patient.insight.dto.internal.HealthStats;

/**
 * 인사이트 분석 데이터 가용성 정보
 *
 * 각 데이터 유형의 존재 여부를 구조화하여 반환
 * - 프론트엔드: 데이터 연동 상태 UI 표시
 * - AI 프롬프트: 데이터 부족 시 보수적 평가 유도
 */
@Schema(description = "인사이트 분석 데이터 가용성 정보")
public record InsightDataAvailability(

    // ===== 활동 데이터 =====
    @Schema(description = "활동 데이터 전체 존재 여부", example = "true")
    @JsonProperty("has_activity_data")
    boolean hasActivityData,

    @Schema(description = "안전구역 이탈 데이터 존재 여부", example = "true")
    @JsonProperty("has_safezone_exit_data")
    boolean hasSafezoneExitData,

    @Schema(description = "길찾기 이탈 데이터 존재 여부", example = "true")
    @JsonProperty("has_route_data")
    boolean hasRouteData,

    @Schema(description = "배회 데이터 존재 여부", example = "true")
    @JsonProperty("has_wander_data")
    boolean hasWanderData,

    @Schema(description = "긴급 상황 데이터 존재 여부 (응급전화/SOS)", example = "true")
    @JsonProperty("has_emergency_data")
    boolean hasEmergencyData,

    @Schema(description = "자주 가는 장소 데이터 존재 여부", example = "true")
    @JsonProperty("has_favorite_data")
    boolean hasFavoriteData,

    // ===== 건강 데이터 =====
    @Schema(description = "건강 데이터 전체 존재 여부", example = "false")
    @JsonProperty("has_health_data")
    boolean hasHealthData,

    @Schema(description = "수면 데이터 존재 여부", example = "false")
    @JsonProperty("has_sleep_data")
    boolean hasSleepData,

    @Schema(description = "걸음수 데이터 존재 여부", example = "false")
    @JsonProperty("has_step_data")
    boolean hasStepData,

    @Schema(description = "심박수 데이터 존재 여부", example = "false")
    @JsonProperty("has_heart_rate_data")
    boolean hasHeartRateData,

    @Schema(description = "산소포화도 데이터 존재 여부", example = "false")
    @JsonProperty("has_oxygen_data")
    boolean hasOxygenData
) {

    /**
     * ActivityStats와 HealthStats로부터 데이터 가용성 정보 생성
     */
    public static InsightDataAvailability from(ActivityStats activity, HealthStats health) {
        // 활동 데이터 가용성
        boolean hasActivity = activity != null;
        boolean hasSafezoneExit = activity != null && activity.safezoneExit() != null;
        boolean hasRoute = activity != null && activity.route() != null;
        boolean hasWander = activity != null && activity.wander() != null;
        boolean hasEmergency = activity != null && activity.emergency() != null;
        boolean hasFavorite = activity != null && activity.favorite() != null;

        // 건강 데이터 가용성
        boolean hasHealth = health != null && (
            health.dataAvailability().sleep() ||
            health.dataAvailability().steps() ||
            health.dataAvailability().heartRate() ||
            health.dataAvailability().oxygen()
        );

        boolean hasSleep = health != null && health.dataAvailability().sleep();
        boolean hasStep = health != null && health.dataAvailability().steps();
        boolean hasHeartRate = health != null && health.dataAvailability().heartRate();
        boolean hasOxygen = health != null && health.dataAvailability().oxygen();

        return new InsightDataAvailability(
            hasActivity,
            hasSafezoneExit,
            hasRoute,
            hasWander,
            hasEmergency,
            hasFavorite,
            hasHealth,
            hasSleep,
            hasStep,
            hasHeartRate,
            hasOxygen
        );
    }

    /**
     * 데이터가 전혀 없는 경우
     */
    public static InsightDataAvailability noData() {
        return new InsightDataAvailability(
            false, false, false, false, false, false,  // 활동 데이터 (6개)
            false, false, false, false, false          // 건강 데이터 (5개)
        );
    }
}
