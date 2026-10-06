package co.dilaser.nexo;

import co.dilaser.nexo.config.NexoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(NexoProperties.class)
public class NexoSupportApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexoSupportApplication.class, args);
    }
}