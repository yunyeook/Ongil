package kr.co.ongil.domain.fcm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import kr.co.ongil.domain.fcm.service.FcmService;
import kr.co.ongil.domain.fcm.service.FcmTokenRedisService;
import kr.co.ongil.domain.notification.dto.request.NotificationRequest;
import kr.co.ongil.domain.notification.service.NotificationService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "FCM", description = "FCM 토큰 및 푸시 알림 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm")
public class FcmController {

    private final FcmService fcmService;
    private final NotificationService notificationService;

    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 등록합니다.")
    @PostMapping("/register")
    public kr.co.ongil.global.common.response.ApiResponse<String> registerToken(
        @Parameter(description = "FCM 토큰", required = true)
        @RequestBody Map<String, String> request) {
       Integer userId = SecurityUtil.getCurrentUserId();
//        Integer userId = 5; // TODO : 로컬 개발시  이거 쓰기
        String token = request.get("token");
        fcmService.registerFcmToken(userId, token);
        return ApiResponse.success(ResponseMessage.FCM_TOKEN_REGISTED);
    }

    @Operation(summary = "FCM 테스트 전송", description = "특정 사용자에게 테스트 푸시 알림을 전송합니다.")
    @PostMapping("/test")
    public ApiResponse<String> sendTestFcm(
        @Parameter(description = "대상 사용자 ID", required = true, example = "1")
        @RequestBody NotificationRequest notificationRequest) {
        notificationService.createNotifications(notificationRequest);
        return ApiResponse.success(ResponseMessage.REQUEST_SUCCESS);
    }

    @Operation(summary = "FCM 토큰 삭제", description = "현재 사용자의 FCM 토큰을 삭제합니다.")
    @DeleteMapping
    public ApiResponse<String> deleteToken() {
        Integer userId = SecurityUtil.getCurrentUserId();
        fcmService.deleteFcmToken(userId);
        return ApiResponse.success(ResponseMessage.FCM_TOKEN_DELETED);
    }
}