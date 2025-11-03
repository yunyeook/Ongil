package kr.co.ongil.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // JWT 인증 스키마 정의
        SecurityScheme securityScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList("bearerAuth");

        return new OpenAPI()
            .info(new Info()
                .title("Ongil API")
                .description("Ongil 프로젝트 API 문서")
                .version("v1.0.0"))
            .addServersItem(new Server()
                .url("http://localhost:8080")
                .description("Local Server"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", securityScheme))
            .addSecurityItem(securityRequirement);
    }
}