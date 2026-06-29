package cehhghost.videohosting.video_managment.services;

import cehhghost.videohosting.video_managment.configs.S3StorageProperties;
import cehhghost.videohosting.video_managment.storages.PresignedDownloadUrl;
import cehhghost.videohosting.video_managment.storages.PresignedUploadUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GarageObjectStorageService implements ObjectStorageService {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    private final S3StorageProperties properties;

    @Override
    public PresignedUploadUrl createPresignedUploadUrl(String objectKey, String contentType) {
        Duration expiration = Duration.ofMinutes(properties.getPresignedUrlExpirationMinutes());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return PresignedUploadUrl.builder()
                .url(presignedRequest.url().toString())
                .expiresAt(Instant.now().plus(expiration))
                .build();
    }

    @Override
    public PresignedDownloadUrl createPresignedDownloadUrl(String objectKey) {
        Duration expiration = Duration.ofMinutes(properties.getPresignedUrlExpirationMinutes());

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return PresignedDownloadUrl.builder()
                .url(presignedRequest.url().toString())
                .expiresAt(Instant.now().plus(expiration))
                .build();
    }

    @Override
    public boolean objectExists(String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();

            s3Client.headObject(request);

            return true;
        } catch (NoSuchKeyException exception) {
            return false;
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return false;
            }

            throw exception;
        }
    }
}
