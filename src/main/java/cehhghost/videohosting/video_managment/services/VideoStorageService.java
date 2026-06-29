package cehhghost.videohosting.video_managment.services;

import cehhghost.videohosting.video_managment.clients.VideoMetadataClient;
import cehhghost.videohosting.video_managment.dtos.*;
import cehhghost.videohosting.video_managment.enums.UploadStatus;
import cehhghost.videohosting.video_managment.events.VideoUploadedApplicationEvent;
import cehhghost.videohosting.video_managment.exceptions.InvalidUploadStatusException;
import cehhghost.videohosting.video_managment.exceptions.VideoStorageObjectNotFoundException;
import cehhghost.videohosting.video_managment.exceptions.VideoUploadNotCompletedException;
import cehhghost.videohosting.video_managment.models.VideoStorageObject;
import cehhghost.videohosting.video_managment.repositories.VideoStorageObjectRepository;
import cehhghost.videohosting.video_managment.storages.PresignedDownloadUrl;
import cehhghost.videohosting.video_managment.storages.PresignedUploadUrl;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoStorageService {

    private static final String ORIGINALS_PREFIX = "videos/originals/";

    private final VideoStorageObjectRepository videoStorageObjectRepository;
    private final ObjectStorageService objectStorageService;
    private final VideoMetadataClient videoMetadataClient;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public InitVideoUploadResponseDTO initUpload(UUID ownerId, InitVideoUploadRequestDTO requestDTO) {
        String title = requestDTO.getTitle().trim();

        String description = requestDTO.getDescription();

        if (description != null) {
            description = description.trim();

            if (description.isBlank()) {
                description = null;
            }
        }

        CreateVideoMetadataResponseDTO metadataResponseDTO =
                videoMetadataClient.createVideoMetadata(
                        CreateVideoMetadataRequestDTO.builder()
                                .title(title)
                                .description(description)
                                .ownerId(ownerId)
                                .visibility(requestDTO.getVisibility())
                                .build()
                );

        UUID videoId = metadataResponseDTO.getVideoId();

        String normalizedOriginalFilename = requestDTO.getOriginalFilename().trim();
        String normalizedContentType = requestDTO.getContentType().trim();

        String objectKey = this.buildObjectKey(videoId, normalizedOriginalFilename);

        VideoStorageObject videoStorageObject = VideoStorageObject.builder()
                .id(UUID.randomUUID())
                .videoId(videoId)
                .ownerId(ownerId)
                .originalFilename(normalizedOriginalFilename)
                .objectKey(objectKey)
                .originalSizeBytes(requestDTO.getSizeBytes())
                .contentType(normalizedContentType)
                .uploadStatus(UploadStatus.PENDING_UPLOAD)
                .build();

        videoStorageObjectRepository.save(videoStorageObject);

        PresignedUploadUrl presignedUploadUrl = objectStorageService.createPresignedUploadUrl(
                objectKey,
                normalizedContentType
        );

        return InitVideoUploadResponseDTO.builder()
                .videoId(videoStorageObject.getVideoId())
                .uploadStatus(videoStorageObject.getUploadStatus())
                .objectKey(videoStorageObject.getObjectKey())
                .uploadUrl(presignedUploadUrl.getUrl())
                .expiresAt(presignedUploadUrl.getExpiresAt())
                .build();
    }

    @Transactional
    public VideoStorageObjectResponseDTO completeUpload(UUID ownerId, UUID videoId) {
        VideoStorageObject videoStorageObject = this.getStorageObjectEntity(videoId);

        this.validateOwner(videoStorageObject, ownerId);

        if (videoStorageObject.getUploadStatus() != UploadStatus.PENDING_UPLOAD) {
            throw new InvalidUploadStatusException(videoStorageObject.getUploadStatus(), UploadStatus.PENDING_UPLOAD);
        }

        if (!objectStorageService.objectExists(videoStorageObject.getObjectKey())) {
            throw new VideoUploadNotCompletedException(videoId);
        }

        videoStorageObject.setUploadStatus(UploadStatus.UPLOADED);

        applicationEventPublisher.publishEvent(
                new VideoUploadedApplicationEvent(
                        videoStorageObject.getVideoId(),
                        videoStorageObject.getObjectKey(),
                        videoStorageObject.getContentType(),
                        videoStorageObject.getOriginalSizeBytes()
                )
        );

        return modelMapper.map(videoStorageObject, VideoStorageObjectResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public VideoStorageObjectResponseDTO getVideo(UUID ownerId, UUID videoId) {
        VideoStorageObject videoStorageObject = this.getStorageObjectEntity(videoId);

        this.validateOwner(videoStorageObject, ownerId);

        return modelMapper.map(videoStorageObject, VideoStorageObjectResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public VideoPlaybackUrlResponseDTO createPlaybackUrl(UUID videoId) {
        VideoStorageObject videoStorageObject = this.getStorageObjectEntity(videoId);

        if (videoStorageObject.getUploadStatus() != UploadStatus.UPLOADED) {
            throw new VideoUploadNotCompletedException(videoId);
        }

        PresignedDownloadUrl presignedDownloadUrl = objectStorageService.createPresignedDownloadUrl(
                videoStorageObject.getObjectKey()
        );

        return VideoPlaybackUrlResponseDTO.builder()
                .videoId(videoStorageObject.getVideoId())
                .objectKey(videoStorageObject.getObjectKey())
                .playbackUrl(presignedDownloadUrl.getUrl())
                .contentType(videoStorageObject.getContentType())
                .expiresAt(presignedDownloadUrl.getExpiresAt())
                .build();
    }

    private String buildObjectKey(UUID videoId, String originalFilename) {
        String extension = this.extractExtension(originalFilename);

        return ORIGINALS_PREFIX + videoId + extension;
    }

    private VideoStorageObject getStorageObjectEntity(UUID videoId) {
        return videoStorageObjectRepository.findByVideoId(videoId)
                .orElseThrow(() -> new VideoStorageObjectNotFoundException(videoId));
    }

    // TODO учесть, что нужно будет переделать с использованием ffprobe
    private String extractExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex).toLowerCase(Locale.ROOT);
    }

    public UUID parseUserId(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid token subject",
                    exception
            );
        }
    }

    private void validateOwner(VideoStorageObject videoStorageObject, UUID ownerId) {
        if (!videoStorageObject.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have access to this video storage object"
            );
        }
    }
}
