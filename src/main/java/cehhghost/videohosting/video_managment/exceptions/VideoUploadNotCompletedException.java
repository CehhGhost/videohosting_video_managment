package cehhghost.videohosting.video_managment.exceptions;

import java.util.UUID;

public class VideoUploadNotCompletedException extends RuntimeException {
    public VideoUploadNotCompletedException(UUID videoId) {
        super("Video file was not found in object storage for video: " + videoId);
    }
}
