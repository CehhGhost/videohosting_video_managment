package cehhghost.videohosting.video_managment.storages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUploadUrl {
    private String url;
    private Instant expiresAt;
}
