package kr.co.ongil.global.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 기본 직렬화 설정 (공통)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))  // 기본 10분
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );

        // users 캐시: 1시간
        RedisCacheConfiguration usersConfig = defaultConfig
                .entryTtl(Duration.ofHours(1));

        // hotDeals 캐시: 5분 (자주 변경되는 데이터)
        RedisCacheConfiguration hotDealsConfig = defaultConfig
                .entryTtl(Duration.ofMinutes(5));

        // statistics 캐시: 24시간 (통계 데이터)
        RedisCacheConfiguration statsConfig = defaultConfig
                .entryTtl(Duration.ofHours(24));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("users", usersConfig)
                .withCacheConfiguration("hotDeals", hotDealsConfig)
                .withCacheConfiguration("statistics", statsConfig)
                .build();
    }
}