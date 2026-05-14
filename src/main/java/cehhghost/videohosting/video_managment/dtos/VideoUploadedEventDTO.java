package cehhghost.videohosting.video_managment.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadedEventDTO {
    private UUID videoId;
    private String objectKey;
    private String contentType;
    private Long sizeBytes;
}
