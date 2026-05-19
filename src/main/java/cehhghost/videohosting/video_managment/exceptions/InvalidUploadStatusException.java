package cehhghost.videohosting.video_managment.exceptions;

import cehhghost.videohosting.video_managment.enums.UploadStatus;

public class InvalidUploadStatusException extends RuntimeException {
    public InvalidUploadStatusException(UploadStatus currentStatus, UploadStatus expectedStatus) {
        super("Invalid upload status. Current status: " + currentStatus + ", expected status: " + expectedStatus);
    }
}
