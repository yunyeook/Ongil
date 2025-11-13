package kr.co.ongil.global.sse.publisher;

import kr.co.ongil.global.sse.event.LocationUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationRedisMessagePublisher {

    private final RedisTemplate<String, Object> pubSubRedisTemplate;
    private final ChannelTopic locationUpdateTopic;

    public void publishLocationUpdate(LocationUpdatedEvent event) {
        try {
            pubSubRedisTemplate.convertAndSend(
                locationUpdateTopic.getTopic(),
                event
            );

            log.info("Location Redis Pub/Sub 발행 완료: patientId={}, channel={}",
                event.patientId(),
                locationUpdateTopic.getTopic()
            );

        } catch (Exception e) {
            log.error("Location Redis Pub/Sub 발행 실패: patientId={}",
                event.patientId(), e
            );
        }
    }
}