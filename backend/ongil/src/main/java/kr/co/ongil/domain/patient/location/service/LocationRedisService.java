package kr.co.ongil.domain.patient.location.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import kr.co.ongil.domain.map.dto.response.CoordinateInfo;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * GPS 위치 정보 Redis 캐싱 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    private static final String GPS_KEY_PREFIX = "gps:";

    /**
     * 최근 GPS 위치 저장
     */
    public void saveLocation(Integer patientId, CoordinateInfo coordinate) {
        String key = GPS_KEY_PREFIX + patientId;
        try {
            // 환자의 기존 위치정보 삭제
            deleteLocation(patientId);
            // 환자의 기존 위치정보 저장
            redisTemplate.opsForValue().set(key, coordinate);

           // TODO :  운영시 TTL 설정 적용
//             redisTemplate.expire(key, 10, TimeUnit.MINUTES);

            log.debug("GPS 위치 저장: patientId={}", patientId);
        } catch (Exception e) {
            log.error("GPS 위치 저장 실패", e);
            throw new BusinessException(ErrorCode.REDIS_SESSION_SAVE_FAILED);
        }
    }

    /**
     * 환자 GPS 위치 조회
     */
    public CoordinateInfo getLocation(Integer patientId) {
        String key = GPS_KEY_PREFIX + patientId;
        try {
            Object data = redisTemplate.opsForValue().get(key);
            if (data == null) {
                return null;
            }
            return objectMapper.convertValue(data, CoordinateInfo.class);
        } catch (Exception e) {
            log.error("GPS 위치 조회 실패", e);
            return null;
        }
    }

    /**
     * 최근 위치 삭제
     */
    public void deleteLocation(Integer patientId) {
        String key = GPS_KEY_PREFIX + patientId;
        redisTemplate.delete(key);
        log.debug("GPS 위치 삭제: patientId={}", patientId);
    }
}