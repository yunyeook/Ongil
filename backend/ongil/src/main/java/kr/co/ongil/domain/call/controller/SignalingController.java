package kr.co.ongil.domain.call.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class SignalingController {
    @MessageMapping("/signal/{roomId}") // 클라이언트가 /app/signal/room123으로 메시지 보내면 이 메서드 실행
    @SendTo("/topic/room/{roomId}") // 이 메서드의 리턴값을 /topic/room/room123 구독자들에게 전송
    public Map<String, Object> signal(@DestinationVariable String roomId,
                                      Map<String, Object> message) {
        return message;  // 그냥 전달
    }
    //프론트에서
    //const socket = new SockJS('https://domain.com/api/ws');
    //const stompClient = Stomp.over(socket);
    //로 연결 가능
}