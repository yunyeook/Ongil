package kr.co.ongil.global.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class JacksonConfig  {
    @Bean @Primary
    public ObjectMapper objectMapper() {

        return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)

            //알 수 없는 필드 무시
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            //제어 문자 허용 (외부 API가 JSON 표준을 안 지킬 때 대비)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)

            // LocalDateTime 등 Java8 날짜 지원
            .registerModule(new JavaTimeModule())

            // 날짜를 timestamp(숫자) 대신 ISO 문자열로 표현
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    }


}
