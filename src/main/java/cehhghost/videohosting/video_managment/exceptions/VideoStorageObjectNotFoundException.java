package cehhghost.videohosting.video_managment.exceptions;

import java.util.UUID;

public class VideoStorageObjectNotFoundException extends RuntimeException {
    public VideoStorageObjectNotFoundException(UUID videoId) {
        super("Video storage object not found for video with id: " + videoId);
    }
}
