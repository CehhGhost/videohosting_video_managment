package cehhghost.videohosting.video_managment.exceptions;

import java.io.IOException;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
