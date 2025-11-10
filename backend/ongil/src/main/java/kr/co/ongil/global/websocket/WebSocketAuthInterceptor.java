package kr.co.ongil.global.websocket;

import java.util.List;
import java.util.Map;
import kr.co.ongil.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Map<String, Object> attributes) {

        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            log.warn("WebSocket 인증 실패: Authorization 헤더 없음");
            return false;
        }

        String token = authHeaders.get(0).replace("Bearer ", "");
        Integer userId = jwtUtil.getUserIdFromToken(token);
        String userType = jwtUtil.getUserTypeFromToken(token);

        attributes.put("userId", userId);
        attributes.put("userType", userType);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Exception ex) {}
}
