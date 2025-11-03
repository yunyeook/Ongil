package kr.co.ongil.global.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;
import java.util.stream.Collectors;

public class DotenvApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // .env 파일을 찾아서 로드합니다. .env 파일이 프로젝트 루트에 있어야 합니다.
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // .env 파일의 모든 항목을 Map<String, Object> 객체로 변환합니다.
        Map<String, Object> envMap = dotenv.entries().stream()
                .collect(Collectors.toMap(DotenvEntry::getKey, e -> (Object) e.getValue()));

        // Spring 환경에 "dotenv"라는 이름으로 프로퍼티 소스를 추가합니다.
        applicationContext.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("dotenv", envMap));
    }
}