package cehhghost.videohosting.video_managment.dtos;

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
public class VideoPlaybackUrlResponseDTO {
    private UUID videoId;
    private String objectKey;
    private String playbackUrl;
    private String contentType;
    private Instant expiresAt;
}
