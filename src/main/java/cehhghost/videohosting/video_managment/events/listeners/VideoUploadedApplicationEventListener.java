package cehhghost.videohosting.video_managment.events.listeners;

import cehhghost.videohosting.video_managment.dtos.events.VideoUploadedEventDTO;
import cehhghost.videohosting.video_managment.events.VideoUploadedApplicationEvent;
import cehhghost.videohosting.video_managment.events.producers.VideoEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class VideoUploadedApplicationEventListener {
    private final VideoEventProducer videoEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVideoUploaded(VideoUploadedApplicationEvent event) {
        VideoUploadedEventDTO eventDTO = VideoUploadedEventDTO.builder()
                .videoId(event.getVideoId())
                .objectKey(event.getObjectKey())
                .contentType(event.getContentType())
                .sizeBytes(event.getSizeBytes())
                .build();

        videoEventProducer.publishVideoUploaded(eventDTO);
    }
}
