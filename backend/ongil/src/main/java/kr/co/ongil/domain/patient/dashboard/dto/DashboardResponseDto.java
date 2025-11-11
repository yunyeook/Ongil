package kr.co.ongil.domain.patient.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import kr.co.ongil.domain.patient.dashboard.entity.DashboardCalc;
import kr.co.ongil.domain.patient.safezone.entity.SafeZone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "환자 대쉬보드 정보")
@Getter
@Builder
@AllArgsConstructor
public class DashboardResponseDto {
    private Long routeLost;

    private Long safezoneEmer;

    private Long emerCall;

    private Long sosSign;

    private String safezoneExit;

    private String favorite;

    public static DashboardResponseDto from(DashboardCalc dashboardCalc) {
        return new DashboardResponseDto(
                dashboardCalc.getRouteLost(),
                dashboardCalc.getSafezoneEmer(),
                dashboardCalc.getEmerCall(),
                dashboardCalc.getSosSign(),
                dashboardCalc.getSafezoneExit(),
                dashboardCalc.getFavorite()
        );
    }
}
