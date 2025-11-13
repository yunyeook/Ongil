package kr.co.ongil.global.sse.eventListener;

import kr.co.ongil.global.sse.event.NavigationEvent;
import kr.co.ongil.global.sse.publisher.NavigationRedisMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 길안내 이벤트 리스너
 *
 * 특정 서버가 직접 SSE 전송 -> Redis Pub/Sub으로 발행만 함
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NavigationEventListener {

    private final NavigationRedisMessagePublisher redisPublisher;

    @EventListener
    public void handleNavigationEvent(NavigationEvent event) {

        redisPublisher.publishNavigationEvent(event);

        log.info("길안내 이벤트 Redis 발행 완료: patientId={}, initiatorId={}, status={}",
            event.patientId(),
            event.initiatorId(),
            event.status()
        );
    }
}