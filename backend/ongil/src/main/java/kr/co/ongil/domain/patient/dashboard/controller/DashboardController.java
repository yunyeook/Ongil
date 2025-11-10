package kr.co.ongil.domain.patient.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.ongil.domain.patient.dashboard.dto.DashboardResponseDto;
import kr.co.ongil.domain.patient.dashboard.service.DashboardService;
import kr.co.ongil.domain.patient.safezone.dto.response.SafeZoneResponse;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard API", description = "환자 대쉬보드 API")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/{patientId}")
    @Operation(summary = "대쉬보드 조회", description = "환자의 요약정보를 가져옵니다.")
    public ApiResponse<DashboardResponseDto> getDashboard(
            @Parameter(description = "환자 ID", required = true, example = "1")
            @PathVariable Integer patientId) {
        DashboardResponseDto response=dashboardService.getDashboardResponseDto(patientId);
        if(response!=null){return ApiResponse.success(ResponseMessage.DASHBOARD_SUCCESS,response);}
        else return ApiResponse.success(ResponseMessage.DASHBOARD_FAIL,null);
    }
}
