package kr.co.ongil.domain.patient.insight.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * 건강 통계 (HealthDataSummaryResponse에서 집계)
 */
@Builder
public record HealthStats(
    SleepStats sleep,
    StepStats steps,
    @JsonProperty("heart_rate")
    HeartRateStats heartRate,
    @JsonProperty("oxygen_saturation")
    OxygenStats oxygenSaturation,
    @JsonProperty("data_availability")
    DataAvailability dataAvailability
) {

    @Builder
    public record SleepStats(
        @JsonProperty("avg_hours_current")
        Double avgHoursCurrent,
        @JsonProperty("avg_hours_previous")
        Double avgHoursPrevious,
        String trend  // "INCREASE", "DECREASE", "STABLE"
    ) {}

    @Builder
    public record StepStats(
        @JsonProperty("avg_steps_current")
        Double avgStepsCurrent,
        @JsonProperty("avg_steps_previous")
        Double avgStepsPrevious,
        String trend,
        @JsonProperty("change_rate")
        double changeRate
    ) {}

    @Builder
    public record HeartRateStats(
        @JsonProperty("avg_current")
        Double avgCurrent,
        @JsonProperty("max_current")
        Double maxCurrent,
        @JsonProperty("variability_current")
        Double variabilityCurrent  // max - min
    ) {}

    @Builder
    public record OxygenStats(
        @JsonProperty("avg_current")
        Double avgCurrent
    ) {}

    @Builder
    public record DataAvailability(
        boolean sleep,
        boolean steps,
        @JsonProperty("heart_rate")
        boolean heartRate,
        @JsonProperty("oxygen")
        boolean oxygen
    ) {}
}
