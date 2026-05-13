package cehhghost.videohosting.video_managment.services;

import cehhghost.videohosting.video_managment.storages.PresignedUploadUrl;

public interface ObjectStorageService {
    PresignedUploadUrl createPresignedUploadUrl(
            String objectKey,
            String contentType
    );

    boolean objectExists(String objectKey);
}
