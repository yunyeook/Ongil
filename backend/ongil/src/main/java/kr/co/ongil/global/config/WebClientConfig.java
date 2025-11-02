package kr.co.ongil.global.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 공통 설정
 * 모든 외부 API 호출에 사용되는 공통 컴포넌트
 */
@Configuration
public class WebClientConfig {

    /**
     * ExchangeStrategies (메시지 크기 제한, 코덱 설정)
     */
    @Bean
    public ExchangeStrategies webClientExchangeStrategies(ObjectMapper objectMapper) {
        return ExchangeStrategies.builder()
            .codecs(configurer -> {
                configurer.defaultCodecs().jackson2JsonEncoder(
                    new Jackson2JsonEncoder(objectMapper)
                );
                configurer.defaultCodecs().jackson2JsonDecoder(
                    new Jackson2JsonDecoder(objectMapper)
                );
                configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10MB
            })
            .build();
    }

    /**
     * HttpClient (타임아웃 설정)
     */
    @Bean
    public HttpClient webClientHttpClient() {
        return HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
            .responseTimeout(Duration.ofSeconds(30))
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))
            );
    }

    /**
     * ReactorClientHttpConnector
     */
    @Bean
    public ReactorClientHttpConnector webClientConnector(HttpClient webClientHttpClient) {
        return new ReactorClientHttpConnector(webClientHttpClient);
    }
}