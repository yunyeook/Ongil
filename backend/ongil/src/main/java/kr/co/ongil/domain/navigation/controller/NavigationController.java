package kr.co.ongil.domain.navigation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.fcm.service.FcmService;
import kr.co.ongil.domain.fcm.service.FcmTokenRedisService;
import kr.co.ongil.domain.navigation.dto.request.EndNavigationRequest;
import kr.co.ongil.domain.navigation.dto.request.StartNavigationRequest;
import kr.co.ongil.domain.navigation.dto.response.EndNavigationResponse;
import kr.co.ongil.domain.navigation.dto.response.NavigationSessionResponse;
import kr.co.ongil.domain.navigation.service.NavigationService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.util.SecurityUtil;
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
        Integer senderId = SecurityUtil.getCurrentUserId();
        NavigationSessionResponse response = navigationService.startNavigation(request,senderId);
        return ApiResponse.success(ResponseMessage.NAVIGATION_START_SUCCESS, response);
    }

    @PostMapping("/end")
    @Operation(summary = "길안내 종료", description = "길안내를 종료합니다.")
    public ApiResponse<EndNavigationResponse> endNavigation(
        @RequestBody @Valid EndNavigationRequest request
    ) {
        Integer senderId = SecurityUtil.getCurrentUserId();
        EndNavigationResponse response = navigationService.endNavigation(request,senderId);
        return ApiResponse.success(ResponseMessage.NAVIGATION_END_SUCCESS, response);
    }

}