package kr.co.ongil.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 구독(Subscribe) 주소 설정
        // 클라이언트가 "/topic/xxx"로 시작하는 주소를 구독할 수 있음
        config.setApplicationDestinationPrefixes("/app"); // 발행(Publish) 주소 설정
        // → 클라이언트가 "/app/xxx"로 메시지를 보냄
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/ws").setAllowedOrigins("*").withSockJS();
    }
}