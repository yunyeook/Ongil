package kr.co.ongil.global.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 설정
 * VoIP 통화 시그널링용
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthChannelInterceptor authChannelInterceptor;

    /**
     * STOMP 엔드포인트 등록
     * 클라이언트는 /ws로 연결
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")  // CORS 설정 (운영에서는 구체적으로 지정)
            .withSockJS();  // SockJS fallback 지원
    }

    /**
     * 메시지 브로커 설정
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트 → 서버 메시지 프리픽스
        config.setApplicationDestinationPrefixes("/app");

        // 서버 → 클라이언트 메시지 브로커
        // 운영 환경에서는 RabbitMQ/Redis 사용 권장
        config.enableSimpleBroker("/topic", "/queue");

        // 유저별 메시지 전송 프리픽스
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 인바운드 채널 설정 (인증 인터셉터 추가)
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}
