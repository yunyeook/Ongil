package kr.co.ongil.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 날짜를 timestamp(숫자) 대신 ISO 문자열로 표현
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // LocalDateTime 등 Java8 날짜 지원
        mapper.registerModule(new JavaTimeModule());

        // 카멜케이스 ↔ 스네이크케이스 자동 변환
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }
}
