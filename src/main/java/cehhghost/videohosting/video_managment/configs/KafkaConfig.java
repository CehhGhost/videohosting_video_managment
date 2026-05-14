package cehhghost.videohosting.video_managment.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaConfig {
    @Bean
    public NewTopic videoUploadedTopic(KafkaTopicProperties kafkaTopicProperties) {
        return TopicBuilder.name(kafkaTopicProperties.getVideoUploaded())
                .partitions(kafkaTopicProperties.getVideoUploadedPartitions())
                .replicas(kafkaTopicProperties.getVideoUploadedReplicas())
                .build();
    }
}
