package kr.co.ongil.domain.navigation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.ongil.domain.map.dto.response.RouteResponse;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavigationRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String NAV_KEY_PREFIX = "navigation:";
    private static final Duration SESSION_TTL = Duration.ofHours(24);

    /**
     * 길안내 세션 저장
     */
    public void saveNavigationSession(Long patientId, String navigationId, RouteResponse route) {
        String key = NAV_KEY_PREFIX + patientId;

        try {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("navigation_id", navigationId);
            sessionData.put("route", objectMapper.writeValueAsString(route));
            sessionData.put("started_at", LocalDateTime.now().toString());

            redisTemplate.opsForHash().putAll(key, sessionData);
            redisTemplate.expire(key, SESSION_TTL);

            log.info("Redis 길안내 세션 저장: patientId={}, navigationId={}", patientId, navigationId);
        } catch (Exception e) {
            log.error("Redis 세션 저장 실패", e);
            throw new RuntimeException("Redis 세션 저장 실패", e);
        }
    }

    /**
     * 길안내 세션 전체 조회
     */
    public Map<Object, Object> getNavigationSession(Long patientId) {
        String key = NAV_KEY_PREFIX + patientId;
        Map<Object, Object> session = redisTemplate.opsForHash().entries(key);

        if (session.isEmpty()) {
            return null;
        }

        return session;
    }

    /**
     * 경로 조회
     */
    public RouteResponse getRoute(Long patientId) {
        String key = NAV_KEY_PREFIX + patientId;
        Object routeJson = redisTemplate.opsForHash().get(key, "route");

        if (routeJson == null) {
            return null;
        }

        try {
            return objectMapper.readValue(routeJson.toString(), RouteResponse.class);
        } catch (Exception e) {
            log.error("경로 역직렬화 실패", e);
            return null;
        }
    }

    /**
     * 길안내 중인지 여부
     */
    public boolean hasActiveSession(Long patientId) {
        String key = NAV_KEY_PREFIX + patientId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * navigationId 조회
     */
    public String getNavigationId(Long patientId) {
        String key = NAV_KEY_PREFIX + patientId;
        Object navigationId = redisTemplate.opsForHash().get(key, "navigation_id");
        return navigationId != null ? navigationId.toString() : null;
    }

    /**
     * 길안내 종료
     */
    public void endSession(Long patientId) {
        String key = NAV_KEY_PREFIX + patientId;
        redisTemplate.delete(key);
        log.info("Redis 길안내 세션 종료: patientId={}", patientId);
    }
}