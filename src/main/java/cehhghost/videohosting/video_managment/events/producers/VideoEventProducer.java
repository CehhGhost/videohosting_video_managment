package cehhghost.videohosting.video_managment.events.producers;

import cehhghost.videohosting.video_managment.configs.KafkaTopicProperties;
import cehhghost.videohosting.video_managment.dtos.VideoUploadedEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoEventProducer {
    private final KafkaTemplate<String, VideoUploadedEventDTO> kafkaTemplate;

    private final KafkaTopicProperties kafkaTopicProperties;

    public void publishVideoUploaded(VideoUploadedEventDTO eventDTO) {
        String topic = kafkaTopicProperties.getVideoUploaded();
        String key = eventDTO.getVideoId().toString();

        kafkaTemplate.send(topic, key, eventDTO)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error(
                                "Failed to publish video uploaded event. videoId={}, topic={}",
                                eventDTO.getVideoId(),
                                topic,
                                exception
                        );
                        return;
                    }

                    log.info(
                            "Published video uploaded event. videoId={}, topic={}, partition={}, offset={}",
                            eventDTO.getVideoId(),
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }
}
