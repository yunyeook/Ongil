package kr.co.ongil.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "tmap")
public class TmapConfig {

    @Value("${tmap.api.base-url}")
    private String baseUrl;

    @Value("${tmap.app-key}")
    private String appKey;

    @Bean(name = "tmapWebClient")
    public WebClient tmapWebClient(
        ExchangeStrategies webClientExchangeStrategies,
        ReactorClientHttpConnector webClientConnector
    ) {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("appKey", appKey)
            .clientConnector(webClientConnector)
            .exchangeStrategies(webClientExchangeStrategies)
            .build();
    }
}