package kr.co.ongil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OngilApplication {

    public static void main(String[] args) {

        SpringApplication.run(OngilApplication.class, args);
    }

}
