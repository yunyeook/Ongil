package kr.co.ongil.domain.fcm.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmTokenRedisService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "fcm:user:";

    public void saveToken(Integer userId, String token) {
        String key = KEY_PREFIX + userId;
        redisTemplate.opsForSet().add(key, token);
//        redisTemplate.expire(key, 30, TimeUnit.DAYS);  // TTL 설정
    }

    public String getToken(Integer userId) {
        String key = KEY_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(key);
        if (tokens == null || tokens.isEmpty()) return null;
        return tokens.iterator().next(); // 첫 번째 토큰 반환
    }
    public Set<String> getTokens(Integer userId) {
        String key = KEY_PREFIX + userId;
        return redisTemplate.opsForSet().members(key);
    }
    public void deleteToken(Integer userId, String token) {
        redisTemplate.opsForSet().remove(KEY_PREFIX + userId, token);
    }

    public void deleteAllTokens(Integer userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}
