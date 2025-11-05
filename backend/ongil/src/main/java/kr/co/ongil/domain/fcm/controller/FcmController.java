package kr.co.ongil.domain.fcm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.ongil.domain.fcm.service.FcmService;
import kr.co.ongil.domain.fcm.service.FcmTokenRedisService;
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
    private final FcmTokenRedisService fcmTokenRedisService;

    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 등록합니다.")
    @PostMapping("/register")
    public kr.co.ongil.global.common.response.ApiResponse<String> registerToken(
        @Parameter(description = "FCM 토큰", required = true)
        @RequestBody String token) {
        Integer userId = SecurityUtil.getCurrentUserId();
        fcmService.registerFcmToken(userId, token);
        return kr.co.ongil.global.common.response.ApiResponse.success(ResponseMessage.FCM_TOKEN_REGISTED);
    }

//    @Operation(summary = "FCM 테스트 전송", description = "특정 사용자에게 테스트 푸시 알림을 전송합니다.")
//    @PostMapping("/test")
//    public kr.co.ongil.global.common.response.ApiResponse<String> sendTestFcm(
//        @Parameter(description = "대상 사용자 ID", required = true, example = "1")
//        @RequestParam Integer userId) {
//        String token = fcmTokenRedisService.getToken(userId); // Redis 또는 DB에서 조회
//        fcmService.sendNotification(token, "FCM 테스트", "서버에서 보낸 테스트 메시지입니다");
//        return kr.co.ongil.global.common.response.ApiResponse.success(ResponseMessage.REQUEST_SUCCESS);
//    }

    @Operation(summary = "FCM 토큰 삭제", description = "현재 사용자의 FCM 토큰을 삭제합니다.")
    @DeleteMapping
    public kr.co.ongil.global.common.response.ApiResponse<String> deleteToken() {
        Integer userId = SecurityUtil.getCurrentUserId();
        fcmService.deleteFcmToken(userId);
        return kr.co.ongil.global.common.response.ApiResponse.success(ResponseMessage.FCM_TOKEN_DELETED);
    }
}