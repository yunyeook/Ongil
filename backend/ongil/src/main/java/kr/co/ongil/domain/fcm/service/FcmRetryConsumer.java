package kr.co.ongil.domain.fcm.service;

import kr.co.ongil.domain.fcm.dto.request.FcmRetryMessage;
import kr.co.ongil.global.config.FcmRabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * [RabbitMQ 소비자(Consumer)]
 * 큐에 쌓인 메시지를 실질적으로 꺼내서 처리하는 클래스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmRetryConsumer {

    private final FcmService fcmService;

    /**
     * @RabbitListener: 특정 큐를 계속 지켜보고 있다가 메시지가 들어오면 자동으로 실행되는 어노테이션.
     *                  queues: 어떤 우체통(Queue)을 지켜볼지 지정.
     * 
     *                  동작 방식:
     *                  1. RabbitMQ 큐에 '재시도 메시지'가 들어옴
     *                  2. 스프링이 이 어노테이션을 보고 "어! 메시지 왔다" 하고 이 메서드를 실행함
     *                  3. 파라미터(retryMessage)로 들어온 데이터를 들고 다시 발송을 시도함
     */
    @RabbitListener(queues = FcmRabbitConfig.FCM_RETRY_QUEUE)
    public void consumeRetryMessage(FcmRetryMessage retryMessage) {
        log.info("FCM 재시도 메시지 수신 - type: {}, retryCount: {}",
                retryMessage.messageType(), retryMessage.retryCount());

        // 큐에서 꺼낸 메시지를 들고 실제 발송 로직(fcmService)을 호출.
        fcmService.sendRetry(retryMessage);
    }
}
