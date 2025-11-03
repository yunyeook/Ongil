package kr.co.ongil.domain.navigation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.navigation.dto.request.EndNavigationRequest;
import kr.co.ongil.domain.navigation.dto.request.StartNavigationRequest;
import kr.co.ongil.domain.navigation.dto.response.EndNavigationResponse;
import kr.co.ongil.domain.navigation.dto.response.NavigationSessionResponse;
import kr.co.ongil.domain.navigation.service.NavigationService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/navigation")
@RequiredArgsConstructor
@Tag(name = "Navigation API", description = "길안내 네비게이션 API")
public class NavigationController {

    private final NavigationService navigationService;

    @PostMapping("/start")
    @Operation(summary = "길안내 시작", description = "환자 또는 보호자가 길안내를 시작합니다.")
    public ApiResponse<NavigationSessionResponse> startNavigation(
        @RequestBody  StartNavigationRequest request
    ) {
        NavigationSessionResponse response = navigationService.startNavigation(request);
        return ApiResponse.success(ResponseMessage.NAVIGATION_START_SUCCESS, response);
    }

    @PostMapping("/end")
    @Operation(summary = "길안내 종료", description = "길안내를 종료합니다.")
    public ApiResponse<EndNavigationResponse> endNavigation(
        @RequestBody @Valid EndNavigationRequest request
    ) {
        EndNavigationResponse response = navigationService.endNavigation(request);
        return ApiResponse.success(ResponseMessage.NAVIGATION_END_SUCCESS, response);
    }

//    @PutMapping("/{navigationId}/location")
//    @Operation(summary = "위치 업데이트", description = "환자의 현재 위치를 업데이트합니다.")
//    public ResponseEntity<ApiResponse<NavigationStateResponse>> updateLocation(
//        @PathVariable String navigationId,
//        @RequestBody @Valid UpdateLocationRequest request
//    ) {
//        NavigationStateResponse response = navigationService.updateLocation(navigationId, request);
//        return ResponseEntity.ok(ApiResponse.success("위치가 업데이트되었습니다.", response));
//    }
//
//    @GetMapping("/{navigationId}")
//    @Operation(summary = "상태 조회", description = "길안내 진행 상황을 조회합니다.")
//    public ResponseEntity<ApiResponse<NavigationStateResponse>> getNavigationState(
//        @PathVariable String navigationId
//    ) {
//        NavigationStateResponse response = navigationService.getNavigationState(navigationId);
//        return ResponseEntity.ok(ApiResponse.success("상태를 조회했습니다.", response));
//    }
//

//
//    @GetMapping("/statistics/{patientId}")
//    @Operation(summary = "통계 조회", description = "환자의 길안내 통계를 조회합니다.")
//    public ResponseEntity<ApiResponse<NavigationStatistics>> getStatistics(
//        @PathVariable Long patientId
//    ) {
//        NavigationStatistics statistics = historyService.getStatistics(patientId);
//        return ResponseEntity.ok(ApiResponse.success("통계를 조회했습니다.", statistics));
//    }
}