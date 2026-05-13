package cehhghost.videohosting.video_managment.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage.s3")
public class S3StorageProperties {
    private String endpoint;
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private Integer presignedUrlExpirationMinutes;
}
