package cehhghost.videohosting.video_managment.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class VideoMetadataClientConfig {
    @Bean
    public RestClient videoMetadataRestClient(
            @Value("${services.video-metadata.base-url}") String baseUrl
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
