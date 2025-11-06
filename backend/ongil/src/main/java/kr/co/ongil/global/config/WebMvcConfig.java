package kr.co.ongil.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정
 *
 * 모든 비즈니스 API 컨트롤러에 /api/v1 prefix를 자동으로 추가합니다.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/v1",
            // kr.co.ongil.domain 패키지의 모든 컨트롤러에 /api/v1 prefix 추가
            c -> c.getPackageName().startsWith("kr.co.ongil.domain")
        );
    }
}
