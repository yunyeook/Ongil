package kr.co.ongil.domain.call.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * 간단한 시그널링 컨트롤러 (테스트용)
 *
 * ⚠️ 이 컨트롤러는 인증/권한 검증 없이 메시지를 그대로 전달합니다.
 * ⚠️ WebSocket 연결 테스트 및 간단한 시그널링 테스트 용도로만 사용하세요.
 *
 * 실제 VoIP 통화는 CallSignalController를 사용하세요. (인증 + 권한 검증 포함)
 *
 * 사용법:
 * 1. WebSocket 연결: new SockJS('https://domain.com/api/ws')
 * 2. 메시지 발행: stompClient.send('/app/signal/room123', {}, JSON.stringify(message))
 * 3. 메시지 구독: stompClient.subscribe('/topic/room/room123', callback)
 */
@Controller
public class SignalingController {

    @MessageMapping("/signal/{roomId}") // 클라이언트가 /app/signal/room123으로 메시지 보내면 이 메서드 실행
    @SendTo("/topic/room/{roomId}") // 이 메서드의 리턴값을 /topic/room/room123 구독자들에게 전송
    public Map<String, Object> signal(@DestinationVariable String roomId,
                                      Map<String, Object> message) {
        return message;  // 그냥 전달 (인증 없음, 테스트용)
    }
}