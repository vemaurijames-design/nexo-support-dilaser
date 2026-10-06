package co.dilaser.nexo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NexoSupportApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexoSupportApplication.class, args);
    }
}
