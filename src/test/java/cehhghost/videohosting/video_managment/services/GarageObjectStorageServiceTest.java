package cehhghost.videohosting.video_managment.services;

import cehhghost.videohosting.video_managment.configs.S3StorageProperties;
import cehhghost.videohosting.video_managment.storages.PresignedDownloadUrl;
import cehhghost.videohosting.video_managment.storages.PresignedUploadUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarageObjectStorageServiceTest {
    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private S3StorageProperties properties;

    private GarageObjectStorageService garageObjectStorageService;

    @BeforeEach
    void setUp() {
        properties = new S3StorageProperties();
        properties.setBucket("videos");
        properties.setPresignedUrlExpirationMinutes(15);
        garageObjectStorageService = new GarageObjectStorageService(s3Client, s3Presigner, properties);
    }

    @Test
    void createPresignedUploadUrlBuildsPutRequest() throws Exception {
        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        when(presignedRequest.url()).thenReturn(URI.create("https://storage.example.com/upload").toURL());
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedRequest);
        Instant before = Instant.now();

        PresignedUploadUrl result = garageObjectStorageService.createPresignedUploadUrl(
                "videos/originals/video.mp4",
                "video/mp4"
        );

        assertThat(result.getUrl()).isEqualTo("https://storage.example.com/upload");
        assertThat(result.getExpiresAt()).isBetween(
                before.plus(Duration.ofMinutes(15)).minusSeconds(1),
                Instant.now().plus(Duration.ofMinutes(15)).plusSeconds(1)
        );

        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        PutObjectPresignRequest request = captor.getValue();
        assertThat(request.signatureDuration()).isEqualTo(Duration.ofMinutes(15));
        PutObjectRequest putObjectRequest = request.putObjectRequest();
        assertThat(putObjectRequest.bucket()).isEqualTo("videos");
        assertThat(putObjectRequest.key()).isEqualTo("videos/originals/video.mp4");
        assertThat(putObjectRequest.contentType()).isEqualTo("video/mp4");
    }

    @Test
    void createPresignedDownloadUrlBuildsGetRequest() throws Exception {
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(URI.create("https://storage.example.com/download").toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        PresignedDownloadUrl result = garageObjectStorageService.createPresignedDownloadUrl("videos/originals/video.mp4");

        assertThat(result.getUrl()).isEqualTo("https://storage.example.com/download");

        ArgumentCaptor<GetObjectPresignRequest> captor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(captor.capture());
        GetObjectPresignRequest request = captor.getValue();
        assertThat(request.signatureDuration()).isEqualTo(Duration.ofMinutes(15));
        GetObjectRequest getObjectRequest = request.getObjectRequest();
        assertThat(getObjectRequest.bucket()).isEqualTo("videos");
        assertThat(getObjectRequest.key()).isEqualTo("videos/originals/video.mp4");
    }

    @Test
    void objectExistsReturnsTrueWhenHeadObjectSucceeds() {
        boolean result = garageObjectStorageService.objectExists("videos/originals/video.mp4");

        assertThat(result).isTrue();

        ArgumentCaptor<HeadObjectRequest> captor = ArgumentCaptor.forClass(HeadObjectRequest.class);
        verify(s3Client).headObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("videos");
        assertThat(captor.getValue().key()).isEqualTo("videos/originals/video.mp4");
    }

    @Test
    void objectExistsReturnsFalseForNoSuchKey() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());

        boolean result = garageObjectStorageService.objectExists("missing.mp4");

        assertThat(result).isFalse();
    }

    @Test
    void objectExistsReturnsFalseForS3NotFound() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(S3Exception.builder()
                .statusCode(404)
                .build());

        boolean result = garageObjectStorageService.objectExists("missing.mp4");

        assertThat(result).isFalse();
    }

    @Test
    void objectExistsRethrowsUnexpectedS3Exception() {
        S3Exception exception = (S3Exception) S3Exception.builder()
                .statusCode(500)
                .message("storage failed")
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(exception);

        assertThatThrownBy(() -> garageObjectStorageService.objectExists("video.mp4"))
                .isSameAs(exception);
    }
}
