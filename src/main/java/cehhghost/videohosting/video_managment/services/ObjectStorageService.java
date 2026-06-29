package cehhghost.videohosting.video_managment.services;

import cehhghost.videohosting.video_managment.storages.PresignedDownloadUrl;
import cehhghost.videohosting.video_managment.storages.PresignedUploadUrl;

public interface ObjectStorageService {
    PresignedUploadUrl createPresignedUploadUrl(
            String objectKey,
            String contentType
    );

    PresignedDownloadUrl createPresignedDownloadUrl(String objectKey);

    boolean objectExists(String objectKey);
}
