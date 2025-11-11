package kr.co.ongil.domain.patient.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.ongil.domain.patient.dashboard.dto.DashboardResponseDto;
import kr.co.ongil.domain.patient.dashboard.dto.MainboardResponseDto;
import kr.co.ongil.domain.patient.dashboard.service.DashboardService;
import kr.co.ongil.domain.patient.safezone.dto.response.SafeZoneResponse;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/aggregation")
@RequiredArgsConstructor
@Tag(name = "Dashboard API", description = "환자 대쉬보드 API")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/dashboard/{patientId}")
    @Operation(summary = "대쉬보드 조회", description = "환자의 요약정보를 가져옵니다.")
    public ApiResponse<DashboardResponseDto> getDashboard(
            @Parameter(description = "환자 ID", required = true, example = "1")
            @PathVariable Integer patientId) {
        DashboardResponseDto response=dashboardService.getDashboardResponseDto(patientId);
        if(response!=null){return ApiResponse.success(ResponseMessage.DASHBOARD_SUCCESS,response);}
        else return ApiResponse.success(ResponseMessage.DASHBOARD_FAIL,null);
    }

    @GetMapping("/mainboard/{patientId}")
    @Operation(summary = "메인보드 조회", description = "환자의 메인화면 정보를 가져옵니다.")
    public ApiResponse<MainboardResponseDto> getMainboard(
            @Parameter(description = "환자 ID", required = true, example = "1")
            @PathVariable Integer patientId) {
        MainboardResponseDto response=dashboardService.getMainboardResponseDto(patientId);
        if(response!=null){return ApiResponse.success(ResponseMessage.DASHBOARD_SUCCESS,response);}
        else return ApiResponse.success(ResponseMessage.DASHBOARD_FAIL,null);
    }

    @PostMapping
    @Operation(summary = "대쉬보드 저장", description = "환자의 요약정보를 저장합니다.")
    public void saveDashboard() {
        dashboardService.saveDashboards();
    }
}
