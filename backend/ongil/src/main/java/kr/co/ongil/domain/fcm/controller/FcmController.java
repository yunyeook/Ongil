package kr.co.ongil.domain.fcm.controller;

import kr.co.ongil.domain.fcm.dto.request.FcmRegisterRequest;
import kr.co.ongil.domain.fcm.service.FcmService;
import kr.co.ongil.domain.fcm.service.FcmTokenRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> registerToken(@RequestBody FcmRegisterRequest request) {
        // 예: 로그인한 사용자 ID와 토큰을 사용하여 저장
        fcmService.registerFcmToken(request.userId(), request.token(), request.deviceInfo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Void> sendTestFcm(@RequestParam Integer userId) {
        String token = fcmTokenRedisService.getToken(userId); // Redis 또는 DB에서 조회
        fcmService.sendNotification(token, "FCM 테스트", "서버에서 보낸 테스트 메시지입니다");
        return ResponseEntity.ok().build();
    }
}