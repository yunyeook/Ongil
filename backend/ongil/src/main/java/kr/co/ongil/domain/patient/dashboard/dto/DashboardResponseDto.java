package kr.co.ongil.domain.patient.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import kr.co.ongil.domain.patient.dashboard.entity.DashboardCalc;
import kr.co.ongil.domain.patient.dashboard.entity.DashboardEnum;
import kr.co.ongil.domain.patient.safezone.entity.SafeZone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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


}
