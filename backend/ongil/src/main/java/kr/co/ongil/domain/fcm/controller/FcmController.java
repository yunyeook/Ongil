package kr.co.ongil.domain.fcm.controller;

import kr.co.ongil.domain.fcm.service.FcmService;
import kr.co.ongil.domain.fcm.service.FcmTokenRedisService;
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


@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm")
public class FcmController {

    private final FcmService fcmService;
    private final FcmTokenRedisService fcmTokenRedisService;

    @PostMapping("/register")
    public ApiResponse<String> registerToken(@RequestBody String token) {
        Integer userId = SecurityUtil.getCurrentUserId();
        fcmService.registerFcmToken(userId, token);
        return ApiResponse.success(ResponseMessage.FCM_TOKEN_REGISTED);
    }

    @PostMapping("/test")
    public ApiResponse<String>  sendTestFcm(@RequestParam Integer userId) {
        String token = fcmTokenRedisService.getToken(userId); // Redis 또는 DB에서 조회
        fcmService.sendNotification(token, "FCM 테스트", "서버에서 보낸 테스트 메시지입니다");
        return ApiResponse.success(ResponseMessage.REQUEST_SUCCESS);
    }
    @DeleteMapping
    public ApiResponse<String> deleteToken() {
        Integer userId = SecurityUtil.getCurrentUserId();
        fcmService.deleteFcmToken(userId);
        return ApiResponse.success(ResponseMessage.FCM_TOKEN_DELETED);
    }
}