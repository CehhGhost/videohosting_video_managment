package cehhghost.videohosting.video_managment.dtos;

import cehhghost.videohosting.video_managment.enums.VideoStatus;
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
public class InitVideoUploadResponseDTO {
    private UUID videoId;
    private VideoStatus status;
    private String objectKey;
    private String uploadUrl;
    private Instant expiresAt;
}
