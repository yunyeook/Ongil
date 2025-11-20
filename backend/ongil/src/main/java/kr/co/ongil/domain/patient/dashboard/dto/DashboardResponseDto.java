package kr.co.ongil.domain.patient.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import kr.co.ongil.domain.patient.dashboard.entity.DashboardCalc;
import kr.co.ongil.domain.patient.dashboard.entity.DashboardEnum;
import kr.co.ongil.domain.patient.safezone.entity.SafeZone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "환자 대쉬보드 정보")
@Getter
@Builder
@AllArgsConstructor
public class DashboardResponseDto {
    private String favorite;
    private String safezoneExit;

    private Long routeLost;

    @Builder.Default
    private Long routeLostDiff=0L;
    @Builder.Default
    private DashboardEnum routeTransition=DashboardEnum.SAME;

    private Long safezoneEmer;

    @Builder.Default
    private Long safezoneEmerDiff=0L;
    @Builder.Default
    private DashboardEnum safezoneTransition=DashboardEnum.SAME;

    private Long sosSign;
    @Builder.Default
    private Long sosSignDiff=0L;
    @Builder.Default
    private DashboardEnum sosSignTransition=DashboardEnum.SAME;

    private Long emerCall;
    @Builder.Default
    private Long emerCallDiff=0L;
    @Builder.Default
    private DashboardEnum emerCallTransition=DashboardEnum.SAME;

    // 🆕 시간대별 위험도 데이터
    @Schema(description = "시간대별 위험도 (0-1 사이 값, 4개 시간대)")
    private List<TimeSlotRisk> timeSlotRisks;

    // 🆕 일별 위험 행동 누적 데이터 (7일간)
    @Schema(description = "일별 위험 행동 횟수 (최근 7일)")
    private List<DailyRiskCount> dailyRiskCounts;

    @Getter
    @AllArgsConstructor
    public static class TimeSlotRisk {
        @Schema(description = "시간대 범위", example = "00-06시")
        private String timeRange;

        @Schema(description = "위험도 (0.0 ~ 1.0)", example = "0.3")
        private Float intensity;
    }

    @Getter
    @AllArgsConstructor
    public static class DailyRiskCount {
        @Schema(description = "날짜", example = "2025-11-10")
        private LocalDate date;

        @Schema(description = "총 위험 행동 횟수", example = "5")
        private Long totalCount;
    }
}
