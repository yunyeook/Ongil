package kr.co.ongil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.builder.SpringApplicationBuilder;
import kr.co.ongil.global.config.DotenvApplicationInitializer;

@SpringBootApplication
@EnableJpaAuditing
public class OngilApplication {

    public static void main(String[] args) {

        new SpringApplicationBuilder(OngilApplication.class)
                .initializers(new DotenvApplicationInitializer())
                .run(args);
    }

}
