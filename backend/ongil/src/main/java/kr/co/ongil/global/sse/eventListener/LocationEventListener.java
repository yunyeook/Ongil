package kr.co.ongil.global.sse.eventListener;

import kr.co.ongil.global.sse.event.LocationUpdatedEvent;
import kr.co.ongil.global.sse.publisher.LocationRedisMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 위치 업데이트 이벤트 리스너
 *
 * 특정 서버가 직접 SSE 전송 -> Redis Pub/Sub으로 발행만 함
 */
@Component
@RequiredArgsConstructor

@Slf4j
public class LocationEventListener {

    private final LocationRedisMessagePublisher redisPublisher;

    @EventListener
    public void handleLocationUpdate(LocationUpdatedEvent event) {

        redisPublisher.publishLocationUpdate(event);

        log.debug("위치 업데이트 Redis 발행 완료: patientId={}",
            event.patientId()
        );
    }
}