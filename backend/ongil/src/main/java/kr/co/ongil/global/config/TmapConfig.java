package kr.co.ongil.global.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "tmap")
public class TmapConfig {

    private String appKey;
    private Api api;

    @Getter
    @Setter
    public static class Api {
        private String baseUrl;
    }

    @Bean(name = "tmapWebClient")
    public WebClient tmapWebClient() {
        return WebClient.builder()
            .baseUrl(api.getBaseUrl())
            .defaultHeader("appKey", appKey)
            .build();
    }
}