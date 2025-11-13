package kr.co.ongil.global.sse.publisher;

import kr.co.ongil.global.sse.event.NavigationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

/**
 * Navigation Redis Pub/Sub 메시지 발행자
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NavigationRedisMessagePublisher {

    private final RedisTemplate<String, Object> pubSubRedisTemplate;
    private final ChannelTopic navigationUpdateTopic;

    /**
     * 길안내 이벤트를 Redis Pub/Sub으로 발행
     *
     * @param event 길안내 이벤트
     */
    public void publishNavigationEvent(NavigationEvent event) {
        try {
            pubSubRedisTemplate.convertAndSend(
                navigationUpdateTopic.getTopic(),
                event
            );

            log.debug("Navigation Redis Pub/Sub 발행 완료: patientId={}, status={}, channel={}",
                event.patientId(),
                event.status(),
                navigationUpdateTopic.getTopic()
            );

        } catch (Exception e) {
            log.error("Navigation Redis Pub/Sub 발행 실패: patientId={}, status={}",
                event.patientId(),
                event.status(),
                e
            );
        }
    }
}