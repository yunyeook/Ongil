package kr.co.ongil.domain.call.controller;

import kr.co.ongil.domain.call.dto.signal.SignalMessage;
import kr.co.ongil.domain.call.entity.Call;
import kr.co.ongil.domain.call.repository.CallRepository;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * VoIP 통화 시그널링 컨트롤러 (WebSocket)
 * WebRTC OFFER/ANSWER/ICE candidate 교환을 위한 시그널링 서버
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CallSignalController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallRepository callRepository;
    private final UserRepository userRepository;

    /**
     * 시그널링 메시지 중계
     * 클라이언트 -> 서버 -> 대상 사용자
     *
     * 클라이언트는 /app/calls/{callId}/signal로 메시지 전송
     * 서버는 /user/{toUserId}/queue/calls/{callId}로 전달
     */
    @MessageMapping("/calls/{callId}/signal")
    public void relaySignal(
        @DestinationVariable Integer callId,
        @Payload SignalMessage message,
        Principal principal
    ) {
        // 1) 발신자 ID는 CustomUserDetails에서 정확히 꺼낸다 (전화번호 아님)
        Integer fromUserId = extractUserId(principal);

        // 2) 통화 세션 확인
        Call call = callRepository.findById(callId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_NOT_FOUND));

        // 3) 권한 확인 (발신자/수신자만 허용)
        boolean isAuthorized = call.getCaller().getId().equals(fromUserId)
            || call.getReceiver().getId().equals(fromUserId);

        if (!isAuthorized) {
            log.warn("시그널링 권한 없음: callId={}, userId={}", callId, fromUserId);
            throw new BusinessException(ErrorCode.CALL_PERMISSION_DENIED);
        }

        // 4) 수신 대상 사용자 ID 계산 (명시 없으면 상대편으로)
        Integer toUserId = message.toUserId();
        if (toUserId == null) {
            toUserId = call.getCaller().getId().equals(fromUserId)
                ? call.getReceiver().getId()
                : call.getCaller().getId();
        }

        // 5) convertAndSendToUser 라우팅 키는 'Principal.getName()'과 동일해야 하므로 "전화번호" 사용
        //    LAZY 엔티티를 건드리지 않고 전화번호만 안전하게 조회
        String targetPrincipalName = userRepository.findPhoneNumberById(toUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 6) 메시지 전송
        messagingTemplate.convertAndSendToUser(
            targetPrincipalName,  // phoneNumber
            "/queue/calls",
            message
        );

        log.info("시그널 전달: type={}, callId={}, fromUserId={}, toUserId={}, toPrincipalName={}",
            message.type(), callId, fromUserId, toUserId, targetPrincipalName);
    }

    private Integer extractUserId(Principal principal) {
        if (principal instanceof Authentication auth) {
            Object p = auth.getPrincipal();
            if (p instanceof CustomUserDetails cud) {
                return cud.getUserId();
            }
        }
        // 혹시 모를 Fallback (하지만 현재 구조에선 phoneNumber가 들어오므로 거의 안 탑니다)
        return Integer.parseInt(principal.getName());
    }

    /**
     * 통화 요청 알림 전송 (INCOMING)
     * REST API에서 호출 용도
     */
    public void sendIncomingCall(Integer callId, Integer fromUserId, Integer toUserId) {
        SignalMessage message = SignalMessage.incoming(callId, fromUserId, toUserId);

//        messagingTemplate.convertAndSendToUser(
//            toUserId.toString(),
//            "/queue/calls",  // 통합 destination 사용
//            message
//        );

        String targetPrincipalName = userRepository.findPhoneNumberById(toUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        messagingTemplate.convertAndSendToUser(
            targetPrincipalName,
            "/queue/calls",  // 통합 destination 사용
            message
        );

        log.info("INCOMING 시그널 전송: callId={}, from={}, to={}", callId, fromUserId, toUserId);

        // TODO: FCM 푸시 알림도 함께 전송 (앱이 백그라운드일 경우 대비)
    }

    /**
     * 통화 종료 알림 전송 (HANGUP)
     * REST API에서 호출 용도
     */
    public void sendHangup(Integer callId, Integer fromUserId, Integer toUserId) {
        SignalMessage message = SignalMessage.hangup(callId, fromUserId, toUserId);

//        messagingTemplate.convertAndSendToUser(
//            toUserId.toString(),
//            "/queue/calls",  // 통합 destination 사용
//            message
//        );

        String targetPrincipalName = userRepository.findPhoneNumberById(toUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        messagingTemplate.convertAndSendToUser(
            targetPrincipalName,
            "/queue/calls",
            message
        );

        log.info("HANGUP 시그널 전송: callId={}, from={}, to={}", callId, fromUserId, toUserId);
    }
}
