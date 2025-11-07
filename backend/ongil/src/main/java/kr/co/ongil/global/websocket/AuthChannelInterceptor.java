package kr.co.ongil.global.websocket;

import kr.co.ongil.global.security.jwt.JwtUtil;
import kr.co.ongil.global.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * WebSocket 인증 인터셉터
 * STOMP CONNECT 시 JWT 토큰 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Authorization 헤더에서 JWT 토큰 추출
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    // JWT 토큰 검증 및 사용자 정보 추출
                    if (jwtUtil.validateToken(token)) {
                        Integer userId = jwtUtil.getUserIdFromToken(token);
                        String username = jwtUtil.getUsernameFromToken(token);
                        String userType = jwtUtil.getUserTypeFromToken(token);

                        // CustomUserDetails 생성
                        CustomUserDetails userDetails = CustomUserDetails.fromToken(userId, username, userType);

                        // Principal 설정
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );

                        accessor.setUser(authentication);
                        log.info("WebSocket 인증 성공: userId={}", userId);
                    } else {
                        log.warn("WebSocket 인증 실패: 유효하지 않은 토큰");
                        throw new IllegalArgumentException("Invalid JWT token");
                    }
                } catch (Exception e) {
                    log.error("WebSocket 인증 오류: {}", e.getMessage());
                    throw new IllegalArgumentException("JWT authentication failed", e);
                }
            } else {
                log.warn("WebSocket 연결 시도: Authorization 헤더 없음");
                throw new IllegalArgumentException("Missing Authorization header");
            }
        }

        return message;
    }
}
