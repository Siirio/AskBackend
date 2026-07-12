package kz.ask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AskBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AskBackendApplication.class, args);
    }
}
