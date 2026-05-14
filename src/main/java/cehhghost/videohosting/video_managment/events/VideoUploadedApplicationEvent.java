package cehhghost.videohosting.video_managment.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class VideoUploadedApplicationEvent {
    private final UUID videoId;
    private final String objectKey;
    private final String contentType;
    private final Long sizeBytes;
}
