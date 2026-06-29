package cehhghost.videohosting.video_managment.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "auth.jwt")
public class AuthJwtProperties {
    private String secret;
}
