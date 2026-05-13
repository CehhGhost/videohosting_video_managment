package cehhghost.videohosting.video_managment.exceptions;

import java.util.UUID;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(UUID videoId) {
        super("Video not found: " + videoId);
    }
}
