package kr.co.ongil.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [RabbitMQ 설정 클래스]
 * 메시지가 지나다닐 '길'과 '우체통'을 시스템에 등록하는 역할.
 */
@Configuration
public class FcmRabbitConfig {

    // 사용할 고유 이름들을 상수로 정의.(강제 문법은 아닌데 일반적으로 계층 구조처럼 쓰는 약속된 관례(Convention).)
    public static final String FCM_RETRY_EXCHANGE = "fcm.retry.exchange";
    public static final String FCM_RETRY_QUEUE = "fcm.retry.queue";
    public static final String FCM_RETRY_ROUTING_KEY = "fcm.retry.key";

    /**
     * DirectExchange: 메시지를 어디로 보낼지 결정하는 '중앙 우체국' 같은 역할.
     * 라우팅 키(Routing Key)가 정확히 일치하는 곳으로 배달해줌.
     */
    @Bean
    public DirectExchange fcmRetryExchange() {
        return new DirectExchange(FCM_RETRY_EXCHANGE);
    }

    /**
     * Queue: 메시지가 실제로 줄 서서 기다리는 '우체통'역할.
     */
    @Bean
    public Queue fcmRetryQueue() {
        return new Queue(FCM_RETRY_QUEUE);
    }

    /**
     * Binding: 우체국(Exchange)과 우체통(Queue)을 연결해주는 역할.
     * "이 우체국으로 오고, 키가 'fcm.retry.key'인 메시지는 이 우체통에 넣어줘"라고 명령하는 설정.
     */
    @Bean
    public Binding fcmRetryBinding(Queue fcmRetryQueue, DirectExchange fcmRetryExchange) {
        return BindingBuilder.bind(fcmRetryQueue).to(fcmRetryExchange).with(FCM_RETRY_ROUTING_KEY);
    }

    /**
     * MessageConverter: 자바 객체를 JSON으로, JSON을 다시 자바 객체로 변환해주는 역할.
     * 이게 없으면 래빗엠큐가 자바 객체를 이해하지 못함.
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
