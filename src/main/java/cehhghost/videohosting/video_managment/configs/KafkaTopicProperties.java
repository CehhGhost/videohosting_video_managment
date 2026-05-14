package cehhghost.videohosting.video_managment.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.kafka.topics")
public class KafkaTopicProperties {
    private String videoUploaded;
    private Integer videoUploadedPartitions = 1;
    private Integer videoUploadedReplicas = 1;
}
