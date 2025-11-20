package kr.co.ongil.domain.patient.insight.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 활동 통계 (DashboardCalc, Abnormal, Sos 등에서 집계)
 */
@Builder
public record ActivityStats(
    @JsonProperty("safezone_exit")
    SafezoneExitStats safezoneExit,

    @JsonProperty("route")
    RouteStats route,

    @JsonProperty("wander")
    WanderStats wander,

    @JsonProperty("emergency")
    EmergencyStats emergency,

    @JsonProperty("favorite")
    FavoriteStats favorite
) {

    @Builder
    public record SafezoneExitStats(
        int current,
        int previous,
        @JsonProperty("by_level")
        Map<String, Integer> byLevel,
        @JsonProperty("time_patterns")
        List<TimeSlotPattern> timePatterns
    ) {}

    @Builder
    public record RouteStats(
        int current,
        int previous,
        @JsonProperty("change_rate")
        double changeRate
    ) {}

    @Builder
    public record WanderStats(
        int current,
        int previous,
        @JsonProperty("avg_duration_minutes")
        double avgDurationMinutes,
        @JsonProperty("night_occurrences")
        int nightOccurrences
    ) {}

    @Builder
    public record EmergencyStats(
        @JsonProperty("emer_call_current")
        int emerCallCurrent,
        @JsonProperty("emer_call_previous")
        int emerCallPrevious,
        @JsonProperty("sos_sign_current")
        int sosSignCurrent,
        @JsonProperty("sos_sign_previous")
        int sosSignPrevious,
        @JsonProperty("sos_not_responded")
        int sosNotResponded
    ) {}

    @Builder
    public record FavoriteStats(
        @JsonProperty("top_current")
        List<PlaceFrequency> topCurrent,
        @JsonProperty("top_previous")
        List<PlaceFrequency> topPrevious,
        @JsonProperty("diversity_index")
        double diversityIndex,
        @JsonProperty("night_outing_count")
        int nightOutingCount
    ) {}

    public record PlaceFrequency(
        @JsonProperty("place_name")
        String placeName,
        @JsonProperty("visit_count")
        int visitCount
    ) {}

    public record TimeSlotPattern(
        @JsonProperty("time_slot")
        String timeSlot,
        int occurrences,
        @JsonProperty("concentration_rate")
        double concentrationRate
    ) {}
}
