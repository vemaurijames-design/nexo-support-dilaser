package co.dilaser.nexo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "nexo")
public class NexoProperties {
    private Jwt jwt = new Jwt();
    private String frontendUrl;
    private Storage storage = new Storage();
    private Mail mail = new Mail();
    private Whatsapp whatsapp = new Whatsapp();

    @Data public static class Jwt {
        private String secret;
        private long accessMinutes = 15;
        private long refreshDays = 7;
    }
    @Data public static class Storage { private String path = "./data/storage"; }
    @Data public static class Mail { private String from; }
    @Data public static class Whatsapp {
        private boolean enabled;
        private String phoneId;
        private String token;
        private String templatePreventivo;
    }
}