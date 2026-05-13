package cehhghost.videohosting.video_managment.dtos;

import cehhghost.videohosting.video_managment.enums.VideoStatus;
import cehhghost.videohosting.video_managment.models.Video;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private String originalFilename;
    private String objectKey;
    private Long originalSizeBytes;
    private String contentType;
    private VideoStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
