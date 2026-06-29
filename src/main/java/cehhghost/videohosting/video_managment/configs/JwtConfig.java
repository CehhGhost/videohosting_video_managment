package cehhghost.videohosting.video_managment.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Bean
    public JwtDecoder jwtDecoder(AuthJwtProperties authJwtProperties) {
        SecretKey secretKey = this.createSecretKey(authJwtProperties);

        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private SecretKey createSecretKey(AuthJwtProperties authJwtProperties) {
        return new SecretKeySpec(
                authJwtProperties.getSecret().getBytes(StandardCharsets.UTF_8),
                HMAC_ALGORITHM
        );
    }
}
