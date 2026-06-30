package cehhghost.videohosting.video_managment.services;

import cehhghost.videohosting.video_managment.clients.VideoMetadataClient;
import cehhghost.videohosting.video_managment.dtos.CreateVideoMetadataRequestDTO;
import cehhghost.videohosting.video_managment.dtos.CreateVideoMetadataResponseDTO;
import cehhghost.videohosting.video_managment.dtos.InitVideoUploadRequestDTO;
import cehhghost.videohosting.video_managment.dtos.InitVideoUploadResponseDTO;
import cehhghost.videohosting.video_managment.dtos.VideoPlaybackUrlResponseDTO;
import cehhghost.videohosting.video_managment.dtos.VideoStorageObjectResponseDTO;
import cehhghost.videohosting.video_managment.enums.UploadStatus;
import cehhghost.videohosting.video_managment.enums.VideoCategory;
import cehhghost.videohosting.video_managment.enums.VideoVisibility;
import cehhghost.videohosting.video_managment.events.VideoUploadedApplicationEvent;
import cehhghost.videohosting.video_managment.exceptions.InvalidUploadStatusException;
import cehhghost.videohosting.video_managment.exceptions.VideoStorageObjectNotFoundException;
import cehhghost.videohosting.video_managment.exceptions.VideoUploadNotCompletedException;
import cehhghost.videohosting.video_managment.models.VideoStorageObject;
import cehhghost.videohosting.video_managment.repositories.VideoStorageObjectRepository;
import cehhghost.videohosting.video_managment.storages.PresignedDownloadUrl;
import cehhghost.videohosting.video_managment.storages.PresignedUploadUrl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoStorageServiceTest {
    @Mock
    private VideoStorageObjectRepository videoStorageObjectRepository;

    @Mock
    private ObjectStorageService objectStorageService;

    @Mock
    private VideoMetadataClient videoMetadataClient;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private VideoStorageService videoStorageService;

    @Test
    void initUploadCreatesMetadataStorageObjectAndPresignedUploadUrl() {
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        Instant expiresAt = Instant.parse("2026-06-30T01:00:00Z");
        Set<String> tags = new LinkedHashSet<>(Set.of("java", "spring"));
        InitVideoUploadRequestDTO requestDTO = InitVideoUploadRequestDTO.builder()
                .title("  My Video  ")
                .description("   ")
                .originalFilename("  Clip.MP4  ")
                .contentType("  video/mp4  ")
                .sizeBytes(1024L)
                .visibility(VideoVisibility.PRIVATE)
                .category(VideoCategory.TUTORIALS)
                .tags(tags)
                .build();

        when(videoMetadataClient.createVideoMetadata(any(CreateVideoMetadataRequestDTO.class)))
                .thenReturn(CreateVideoMetadataResponseDTO.builder()
                        .videoId(videoId)
                        .status("PENDING_UPLOAD")
                        .ownerId(ownerId)
                        .visibility(VideoVisibility.PRIVATE)
                        .build());
        when(videoStorageObjectRepository.save(any(VideoStorageObject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(objectStorageService.createPresignedUploadUrl("videos/originals/" + videoId + ".mp4", "video/mp4"))
                .thenReturn(PresignedUploadUrl.builder()
                        .url("https://storage.example.com/upload")
                        .expiresAt(expiresAt)
                        .build());

        InitVideoUploadResponseDTO result = videoStorageService.initUpload(ownerId, requestDTO);

        ArgumentCaptor<CreateVideoMetadataRequestDTO> metadataCaptor =
                ArgumentCaptor.forClass(CreateVideoMetadataRequestDTO.class);
        verify(videoMetadataClient).createVideoMetadata(metadataCaptor.capture());
        assertThat(metadataCaptor.getValue().getTitle()).isEqualTo("My Video");
        assertThat(metadataCaptor.getValue().getDescription()).isNull();
        assertThat(metadataCaptor.getValue().getOwnerId()).isEqualTo(ownerId);
        assertThat(metadataCaptor.getValue().getVisibility()).isEqualTo(VideoVisibility.PRIVATE);
        assertThat(metadataCaptor.getValue().getCategory()).isEqualTo(VideoCategory.TUTORIALS);
        assertThat(metadataCaptor.getValue().getTags()).isEqualTo(tags);

        ArgumentCaptor<VideoStorageObject> storageObjectCaptor = ArgumentCaptor.forClass(VideoStorageObject.class);
        verify(videoStorageObjectRepository).save(storageObjectCaptor.capture());
        VideoStorageObject savedStorageObject = storageObjectCaptor.getValue();
        assertThat(savedStorageObject.getId()).isNotNull();
        assertThat(savedStorageObject.getVideoId()).isEqualTo(videoId);
        assertThat(savedStorageObject.getOwnerId()).isEqualTo(ownerId);
        assertThat(savedStorageObject.getOriginalFilename()).isEqualTo("Clip.MP4");
        assertThat(savedStorageObject.getObjectKey()).isEqualTo("videos/originals/" + videoId + ".mp4");
        assertThat(savedStorageObject.getOriginalSizeBytes()).isEqualTo(1024L);
        assertThat(savedStorageObject.getContentType()).isEqualTo("video/mp4");
        assertThat(savedStorageObject.getUploadStatus()).isEqualTo(UploadStatus.PENDING_UPLOAD);

        assertThat(result.getVideoId()).isEqualTo(videoId);
        assertThat(result.getUploadStatus()).isEqualTo(UploadStatus.PENDING_UPLOAD);
        assertThat(result.getObjectKey()).isEqualTo("videos/originals/" + videoId + ".mp4");
        assertThat(result.getUploadUrl()).isEqualTo("https://storage.example.com/upload");
        assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void initUploadUsesObjectKeyWithoutExtensionWhenFilenameHasNoExtension() {
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        InitVideoUploadRequestDTO requestDTO = InitVideoUploadRequestDTO.builder()
                .title("Title")
                .originalFilename("clip")
                .contentType("video/mp4")
                .sizeBytes(1024L)
                .visibility(VideoVisibility.PUBLIC)
                .build();
        when(videoMetadataClient.createVideoMetadata(any(CreateVideoMetadataRequestDTO.class)))
                .thenReturn(CreateVideoMetadataResponseDTO.builder().videoId(videoId).build());
        when(objectStorageService.createPresignedUploadUrl("videos/originals/" + videoId, "video/mp4"))
                .thenReturn(PresignedUploadUrl.builder().url("url").expiresAt(Instant.now()).build());

        videoStorageService.initUpload(ownerId, requestDTO);

        verify(objectStorageService).createPresignedUploadUrl("videos/originals/" + videoId, "video/mp4");
    }

    @Test
    void completeUploadMarksUploadedPublishesEventAndReturnsResponse() {
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        VideoStorageObject storageObject = storageObject(videoId, ownerId, UploadStatus.PENDING_UPLOAD);
        VideoStorageObjectResponseDTO responseDTO = VideoStorageObjectResponseDTO.builder()
                .videoId(videoId)
                .uploadStatus(UploadStatus.UPLOADED)
                .build();
        when(videoStorageObjectRepository.findByVideoId(videoId)).thenReturn(Optional.of(storageObject));
        when(objectStorageService.objectExists(storageObject.getObjectKey())).thenReturn(true);
        when(modelMapper.map(storageObject, VideoStorageObjectResponseDTO.class)).thenReturn(responseDTO);

        VideoStorageObjectResponseDTO result = videoStorageService.completeUpload(ownerId, videoId);

        assertThat(result).isSameAs(responseDTO);
        assertThat(storageObject.getUploadStatus()).isEqualTo(UploadStatus.UPLOADED);

        ArgumentCaptor<VideoUploadedApplicationEvent> eventCaptor =
                ArgumentCaptor.forClass(VideoUploadedApplicationEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getVideoId()).isEqualTo(videoId);
        assertThat(eventCaptor.getValue().getObjectKey()).isEqualTo(storageObject.getObjectKey());
        assertThat(eventCaptor.getValue().getContentType()).isEqualTo(storageObject.getContentType());
        assertThat(eventCaptor.getValue().getSizeBytes()).isEqualTo(storageObject.getOriginalSizeBytes());
    }

    @Test
    void completeUploadRejectsWrongOwner() {
        UUID videoId = UUID.randomUUID();
        VideoStorageObject storageObject = storageObject(videoId, UUID.randomUUID(), UploadStatus.PENDING_UPLOAD);
        when(videoStorageObjectRepository.findByVideoId(videoId)).thenReturn(Optional.of(storageObject));

        assertThatThrownBy(() -> videoStorageService.completeUpload(UUID.randomUUID(), videoId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verifyNoInteractions(objectStorageService, applicationEventPublisher);
    }

    @Test
    void completeUploadRejectsUnexpectedStatus() {
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        VideoStorageObject storageObject = storageObject(videoId, ownerId, UploadStatus.UPLOADED);
        when(videoStorageObjectRepository.findByVideoId(videoId)).thenReturn(Optional.of(storageObject));

        assertThatThrownBy(() -> videoStorageService.completeUpload(ownerId, videoId))
                .isInstanceOf(InvalidUploadStatusException.class);

        verifyNoInteractions(objectStorageService, applicationEventPublisher);
    }

    @Test
    void completeUploadRejectsWhenObjectDoesNotExistInStorage() {
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        VideoStorageObject storageObject = storageObject(videoId, ownerId, UploadStatus.PENDING_UPLOAD);
        when(videoStorageObjectRepository.findByVideoId(videoId)).thenReturn(Optional.of(storageObject));
        when(objectStorageService.objectExists(storageObject.getObjectKey())).thenReturn(false);

        assertThatThrownBy(() -> videoStorageService.completeUpload(ownerId, videoId))
                .isInstanceOf(VideoUploadNotCompletedException.class);

        assertThat(storageObject.getUploadStatus()).isEqualTo(UploadStatus.PENDING_UPLOAD);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void completeUploadRejectsMissingStorageObject() {
        UUID videoId = UUID.randomUUID();
        when(videoStorageObjectRepository.findByVideoId(videoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoStorageService.completeUpload(UUID.randomUUID(), videoId))
                .isInstanceOf(VideoStorageObjectNotFoundException.class);
    }

    @Test
    void getVideoReturnsStorageObjectForOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        VideoStorageObject storageObject = storageObject(videoId, ownerId, UploadStatus.PENDING_UPLOAD);
        VideoStorageObjectResponseDTO responseDTO = VideoStorageObjectResponseDTO.builder().videoId(videoId).build();
        when(videoStorageObjectRepository.findByVideoId(videoId)).thenReturn(Optional.of(storageObject));
        when(modelMapper.map(storageObject, VideoStorageObjectResponseDTO.class)).thenReturn(responseDTO);

        VideoStorageObjectResponseDTO result = videoStorageService.getVideo(ownerId, videoId);

        assertThat(result).isSameAs(responseDTO);
    }

    @Test
    void getVideoRejectsWrongOwner() {
        UUID videoId = UUID.randomUUID();
        VideoStorageObject storageObject = storageObject(videoId, UUID.randomUUID(), UploadStatus.PENDING_UPLOAD);
        when(videoStorageObjectRepository.findByVideoId(videoId)).thenReturn(Optional.of(storageObject));

        assertThatThrownBy(() -> videoStorageService.getVideo(UUID.randomUUID(), videoId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verifyNoInteractions(modelMapper);
    }

    @Test
    void createPlaybackUrlReturnsPresignedDownloadUrlForUploadedObject() {
        UUID videoId = UUID.randomUUID();
        VideoStorageObject storageObject = storageObject(videoId, UUID.randomUUID(), UploadStatus.UPLOADED);
        Instant expiresAt = Instant.parse("2026-06-30T01:00:00Z");
        when(videoStorageObjectRepository.findByVideoId(videoId)).thenReturn(Optional.of(storageObject));
        when(objectStorageService.createPresignedDownloadUrl(storageObject.getObjectKey()))
                .thenReturn(PresignedDownloadUrl.builder()
                        .url("https://storage.example.com/playback")
                        .expiresAt(expiresAt)
                        .build());

        VideoPlaybackUrlResponseDTO result = videoStorageService.createPlaybackUrl(videoId);

        assertThat(result.getVideoId()).isEqualTo(videoId);
        assertThat(result.getObjectKey()).isEqualTo(storageObject.getObjectKey());
        assertThat(result.getPlaybackUrl()).isEqualTo("https://storage.example.com/playback");
        assertThat(result.getContentType()).isEqualTo(storageObject.getContentType());
        assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void createPlaybackUrlRejectsPendingUpload() {
        UUID videoId = UUID.randomUUID();
        when(videoStorageObjectRepository.findByVideoId(videoId))
                .thenReturn(Optional.of(storageObject(videoId, UUID.randomUUID(), UploadStatus.PENDING_UPLOAD)));

        assertThatThrownBy(() -> videoStorageService.createPlaybackUrl(videoId))
                .isInstanceOf(VideoUploadNotCompletedException.class);

        verify(objectStorageService, never()).createPresignedDownloadUrl(any());
    }

    @Test
    void parseUserIdReturnsUuid() {
        UUID userId = UUID.randomUUID();

        UUID result = videoStorageService.parseUserId(userId.toString());

        assertThat(result).isEqualTo(userId);
    }

    @Test
    void parseUserIdRejectsInvalidSubject() {
        assertThatThrownBy(() -> videoStorageService.parseUserId("not-a-uuid"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    private VideoStorageObject storageObject(UUID videoId, UUID ownerId, UploadStatus uploadStatus) {
        return VideoStorageObject.builder()
                .id(UUID.randomUUID())
                .videoId(videoId)
                .ownerId(ownerId)
                .originalFilename("clip.mp4")
                .objectKey("videos/originals/" + videoId + ".mp4")
                .originalSizeBytes(1024L)
                .contentType("video/mp4")
                .uploadStatus(uploadStatus)
                .build();
    }
}
