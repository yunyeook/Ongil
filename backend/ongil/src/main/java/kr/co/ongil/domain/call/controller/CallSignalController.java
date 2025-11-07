package kr.co.ongil.domain.call.controller;

import kr.co.ongil.domain.call.dto.signal.SignalMessage;
import kr.co.ongil.domain.call.entity.Call;
import kr.co.ongil.domain.call.repository.CallRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
        Integer fromUserId = Integer.parseInt(principal.getName());
        log.info("시그널링 메시지 수신: type={}, callId={}, from={}, to={}",
            message.type(), callId, fromUserId, message.toUserId());

        // 1. 통화 세션 존재 여부 확인
        Call call = callRepository.findById(callId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_NOT_FOUND));

        // 2. 권한 검증 (발신자 또는 수신자인지 확인)
        boolean isAuthorized = call.getCaller().getId().equals(fromUserId)
            || call.getReceiver().getId().equals(fromUserId);

        if (!isAuthorized) {
            log.warn("시그널링 권한 없음: callId={}, userId={}", callId, fromUserId);
            throw new BusinessException(ErrorCode.CALL_PERMISSION_DENIED);
        }

        // 3. 대상 사용자 ID 계산 (toUserId가 null이면 자동 계산)
        Integer toUserId = message.toUserId();
        if (toUserId == null) {
            // fromUserId가 caller면 receiver에게, receiver면 caller에게 전송
            toUserId = call.getCaller().getId().equals(fromUserId)
                ? call.getReceiver().getId()
                : call.getCaller().getId();
            log.info("toUserId가 null이어서 자동 계산: from={}, to={}", fromUserId, toUserId);
        }

        // 4. 대상 사용자에게 메시지 전달
        String destination = "/queue/calls";  // 통합 destination 사용

        messagingTemplate.convertAndSendToUser(
            toUserId.toString(),
            destination,
            message
        );

        log.info("시그널링 메시지 전달 완료: type={}, from={}, to={}, destination={}",
            message.type(), fromUserId, toUserId, destination);
    }

    /**
     * 통화 요청 알림 전송 (INCOMING)
     * REST API에서 호출 용도
     */
    public void sendIncomingCall(Integer callId, Integer fromUserId, Integer toUserId) {
        SignalMessage message = SignalMessage.incoming(callId, fromUserId, toUserId);

        messagingTemplate.convertAndSendToUser(
            toUserId.toString(),
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

        messagingTemplate.convertAndSendToUser(
            toUserId.toString(),
            "/queue/calls",  // 통합 destination 사용
            message
        );

        log.info("HANGUP 시그널 전송: callId={}, from={}, to={}", callId, fromUserId, toUserId);
    }
}
