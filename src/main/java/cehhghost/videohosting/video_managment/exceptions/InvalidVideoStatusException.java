package cehhghost.videohosting.video_managment.exceptions;

import cehhghost.videohosting.video_managment.enums.VideoStatus;

public class InvalidVideoStatusException extends RuntimeException {
    public InvalidVideoStatusException(VideoStatus currentStatus, VideoStatus expectedStatus) {
        super("Invalid video status. Current status: " + currentStatus + ", expected status: " + expectedStatus);
    }
}
