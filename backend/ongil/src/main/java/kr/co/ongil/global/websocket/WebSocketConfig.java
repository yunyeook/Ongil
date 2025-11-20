package kr.co.ongil.global.websocket;

import kr.co.ongil.global.websocket.gps.GPSWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket 설정
 * - VoIP 통화 시그널링용 (STOMP)
 * - GPS 추적용 (순수 WebSocket)
 */
@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    private final AuthChannelInterceptor authChannelInterceptor;
    private final GPSWebSocketHandler gpsWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    // ==== RabbitMQ STOMP Relay 설정 ====
    @Value("${RABBITMQ_HOST:localhost}")
    private String relayHost;

    @Value("${RABBITMQ_STOMP_PORT:61613}")
    private int relayPort;

    @Value("${RABBITMQ_USERNAME:guest}")
    private String relayUsername;

    @Value("${RABBITMQ_PASSWORD:guest}")
    private String relayPassword;


    // ========== STOMP WebSocket (VoIP용) ==========

    /**
     * STOMP 엔드포인트 등록 (VoIP용)
     * 클라이언트는 /api/ws로 연결
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/ws")
            .setAllowedOriginPatterns("*")  // CORS 설정 (운영에서는 구체적으로 지정)
            .withSockJS();  // SockJS fallback 지원
    }

    /**
     * 메시지 브로커 설정 (VoIP용)
     * SimpleBroker 대신 RabbitMQ STOMP Broker Relay 사용
     * → 서버 여러 대 띄워도 메시지가 RabbitMQ를 통해 공유됨
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트 → 서버 메시지 프리픽스
        config.setApplicationDestinationPrefixes("/app");

        // 서버 → 클라이언트 메시지는 RabbitMQ STOMP 브로커를 통해 라우팅
        config.enableStompBrokerRelay("/topic", "/queue")
            .setRelayHost(relayHost)           // rabbitmq (Docker service name)
            .setRelayPort(relayPort)           // 61613 (STOMP port)
            .setClientLogin(relayUsername)     // admin
            .setClientPasscode(relayPassword)  // password
            .setSystemLogin(relayUsername)     // admin
            .setSystemPasscode(relayPassword)  // password
            // 여러 서버 인스턴스에서 user destination 공유
            .setUserDestinationBroadcast("/topic/unresolved-user-destination")
            .setUserRegistryBroadcast("/topic/simp-user-registry");

        // 유저별 메시지 전송 프리픽스
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 인바운드 채널 설정 (VoIP용 인증 인터셉터)
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
        registration.taskExecutor()
            .corePoolSize(10)       // VoIP 통화 동시 처리
            .maxPoolSize(20)
            .queueCapacity(100);
    }

    /**
     * 아웃바운드 채널 설정 (메시지 전송 성능 향상)
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
            .corePoolSize(10)       // ICE Candidate 동시 전송 처리
            .maxPoolSize(20)
            .queueCapacity(100);
    }

    // ========== 순수 WebSocket (GPS용) ==========

    /**
     * 순수 WebSocket 핸들러 등록 (GPS 추적용)
     * 환자가 길찾기 중일 때 실시간 GPS 전송
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gpsWebSocketHandler, "/ws/gps")
            .addInterceptors(webSocketAuthInterceptor)
            .setAllowedOrigins("*");  // CORS 설정 (운영에서는 구체적으로 지정)
    }
}