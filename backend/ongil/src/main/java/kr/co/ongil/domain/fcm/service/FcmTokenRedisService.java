package kr.co.ongil.domain.fcm.service;

import java.util.Set;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmTokenRedisService {

    private final StringRedisTemplate redisTemplate;
    private static final String FCM_KEY_PREFIX = "fcm:user:";

    public void saveToken(Integer userId, String token) {
        String key = FCM_KEY_PREFIX + userId;
        try {
            // 기존 토큰들 모두 삭제
            deleteAllTokens(userId);
            // 새 토큰 저장
            redisTemplate.opsForSet().add(key, token);

            // 30일 TTL 설정 (TODO: 고려사항)
            // redisTemplate.expire(key, 30, TimeUnit.DAYS);

        } catch (Exception e) {
            log.error("토큰 저장 실패", e);
            // DB에 저장하면 되니까 예외발생시키지 않음.
        }
    }

    /**
     * FCM 토큰은 사용자당 여러 개일 수 있다..
     */

    //해당 키의 특정 토큰 단일 조회.
    public String getToken(Integer userId) {
        String key = FCM_KEY_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(key);
        if (tokens == null || tokens.isEmpty()) return null;
        return tokens.iterator().next(); // 첫 번째 토큰 반환
    }

    //해당 키의 모든 토큰 조회
    public Set<String> getTokens(Integer userId) {
        String key = FCM_KEY_PREFIX + userId;
        return redisTemplate.opsForSet().members(key);
    }
    // 해당 키의 특정 토큰 삭제
    public void deleteToken(Integer userId, String token) {
        redisTemplate.opsForSet().remove(FCM_KEY_PREFIX + userId, token);
    }

    //해당 키의 모든 토큰 삭제
    public void deleteAllTokens(Integer userId) {
        redisTemplate.delete(FCM_KEY_PREFIX + userId);
    }
}
