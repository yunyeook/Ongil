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
    public void saveNavigationSession(Integer patientId, String navigationId, RouteResponse route) {
        String key = NAV_KEY_PREFIX + patientId;

        try {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("navigation_id", navigationId);
            sessionData.put("route", route);
            sessionData.put("started_at", LocalDateTime.now().toString());

            redisTemplate.opsForHash().putAll(key, sessionData);
            redisTemplate.expire(key, SESSION_TTL);

            log.info("Redis 길안내 세션 저장: patientId={}, navigationId={}", patientId, navigationId);
        } catch (Exception e) {
            log.error("Redis 세션 저장 실패", e);
            throw new BusinessException(ErrorCode.REDIS_SESSION_SAVE_FAILED);
        }
    }

    /**
     * 길안내 세션 전체 조회
     */
    public Map<Object, Object> getNavigationSession(Integer patientId) {
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
    public RouteResponse getRoute(Integer patientId) {
        String key = NAV_KEY_PREFIX + patientId;
        Object routeObject = redisTemplate.opsForHash().get(key, "route");

        if (routeObject == null) {
            return null;
        }

        try {
            //  convertValue 사용으로 변경
            return objectMapper.convertValue(routeObject, RouteResponse.class);
        } catch (Exception e) {
            log.error("경로 역직렬화 실패", e);
            throw new BusinessException(ErrorCode.REDIS_DESERIALIZATION_FAILED);
        }
    }


    /**
     * navigationId 조회
     */
    public String getNavigationId(Integer patientId) {
        String key = NAV_KEY_PREFIX + patientId;
        Object navigationId = redisTemplate.opsForHash().get(key, "navigation_id");
        return navigationId != null ? navigationId.toString() : null;
    }

    /**
     * 길안내 종료
     */
    public void endSession(Integer patientId) {
        String key = NAV_KEY_PREFIX + patientId;
        redisTemplate.delete(key);
        log.info("Redis 길안내 세션 종료: patientId={}", patientId);
    }
}