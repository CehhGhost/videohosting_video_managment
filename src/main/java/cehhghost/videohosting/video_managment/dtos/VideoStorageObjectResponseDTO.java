package cehhghost.videohosting.video_managment.dtos;

import cehhghost.videohosting.video_managment.enums.UploadStatus;
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
public class VideoStorageObjectResponseDTO {
    private UUID videoId;
    private String originalFilename;
    private String objectKey;
    private Long originalSizeBytes;
    private String contentType;
    private UploadStatus uploadStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
