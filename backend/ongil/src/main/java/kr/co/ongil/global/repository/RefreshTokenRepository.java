package kr.co.ongil.global.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public void storeRefreshToken(Integer userId, String refreshToken, long expiration) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);
        log.info("리프레시 토큰 저장 완료: userId={}", userId);
    }

    /**
     * 리프레시 토큰을 확인하고 즉시 삭제 (소비)합니다.
     * 이 메서드는 원자적으로 동작하여 경쟁 상태를 방지합니다.
     *
     * @return 토큰이 유효하여 성공적으로 소비되었으면 true, 그렇지 않으면 (토큰이 없거나 일치하지 않으면) false
     */
    public boolean consumeRefreshToken(Integer userId, String refreshTokenToConsume) {
        String key = createKey(userId);

        // Redis의 WATCH, MULTI, EXEC를 사용하여 트랜잭션 내에서 원자적 연산 수행
        List<Object> txResults = redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                // (1) key에 대한 감시를 시작합니다.
                operations.watch(key);

                String storedToken = (String) operations.opsForValue().get(key);

                // (2) 저장된 토큰이 없거나, 소비하려는 토큰과 일치하지 않으면 트랜잭션을 중단합니다.
                if (storedToken == null || !storedToken.equals(refreshTokenToConsume)) {
                    operations.unwatch();
                    return null; // null을 반환하면 트랜잭션이 실행되지 않음
                }

                // (3) 트랜잭션을 시작합니다.
                operations.multi();
                operations.delete(key); // 토큰을 삭제합니다.

                // (4) 트랜잭션을 실행합니다.
                // 만약 (1)번에서 watch한 이후 다른 클라이언트가 key를 변경했다면, exec는 실패하고 null을 반환합니다.
                return operations.exec();
            }
        });

        // txResults가 null이 아니면 트랜잭션이 성공적으로 실행된 것입니다.
        // (delete 명령의 결과는 보통 리스트에 담겨 반환되지만, 성공 여부만 중요)
        if (txResults != null) {
            log.info("리프레시 토큰 소비 성공: userId={}", userId);
            return true;
        } else {
            log.warn("리프레시 토큰 소비 실패 (토큰 불일치 또는 경쟁 상태): userId={}", userId);
            return false;
        }
    }

    public void deleteAllTokensForUser(Integer userId) {
        String key = createKey(userId);
        redisTemplate.delete(key);
        log.info("리프레시 토큰 삭제 완료: userId={}", userId);
    }

    private String createKey(Integer userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }
}