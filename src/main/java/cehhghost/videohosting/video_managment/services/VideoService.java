package cehhghost.videohosting.video_managment.services;

import cehhghost.videohosting.video_managment.dtos.InitVideoUploadRequestDTO;
import cehhghost.videohosting.video_managment.dtos.InitVideoUploadResponseDTO;
import cehhghost.videohosting.video_managment.dtos.VideoResponseDTO;
import cehhghost.videohosting.video_managment.enums.VideoStatus;
import cehhghost.videohosting.video_managment.exceptions.InvalidVideoStatusException;
import cehhghost.videohosting.video_managment.exceptions.VideoNotFoundException;
import cehhghost.videohosting.video_managment.exceptions.VideoUploadNotCompletedException;
import cehhghost.videohosting.video_managment.models.Video;
import cehhghost.videohosting.video_managment.repositories.VideoRepository;
import cehhghost.videohosting.video_managment.storages.PresignedUploadUrl;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private static final String ORIGINALS_PREFIX = "videos/originals/";

    private final VideoRepository videoRepository;

    private final ObjectStorageService objectStorageService;

    private final ModelMapper modelMapper;

    @Transactional
    public InitVideoUploadResponseDTO initUpload(InitVideoUploadRequestDTO requestDTO) {
        UUID videoId = UUID.randomUUID();

        String normalizedOriginalFilename = requestDTO.getOriginalFilename().trim();
        String normalizedContentType = requestDTO.getContentType().trim();

        String objectKey = buildObjectKey(videoId, normalizedOriginalFilename);

        String description = requestDTO.getDescription();

        if (description != null) {
            description = description.trim();
            if (description.isBlank()) {
                description = null;
            }
        }

        Video video = Video.builder()
                .id(videoId)
                .title(requestDTO.getTitle().trim())
                .description(description)
                .originalFilename(normalizedOriginalFilename)
                .objectKey(objectKey)
                .originalSizeBytes(requestDTO.getSizeBytes())
                .contentType(normalizedContentType)
                .status(VideoStatus.PENDING_UPLOAD)
                .build();

        videoRepository.save(video);

        PresignedUploadUrl presignedUploadUrl = objectStorageService.createPresignedUploadUrl(
                objectKey,
                normalizedContentType
        );

        return InitVideoUploadResponseDTO.builder()
                .videoId(video.getId())
                .status(video.getStatus())
                .objectKey(video.getObjectKey())
                .uploadUrl(presignedUploadUrl.getUrl())
                .expiresAt(presignedUploadUrl.getExpiresAt())
                .build();
    }

    @Transactional
    public VideoResponseDTO completeUpload(UUID videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId));

        if (video.getStatus() != VideoStatus.PENDING_UPLOAD) {
            throw new InvalidVideoStatusException(video.getStatus(), VideoStatus.PENDING_UPLOAD);
        }

        if (!objectStorageService.objectExists(video.getObjectKey())) {
            throw new VideoUploadNotCompletedException(videoId);
        }

        video.setStatus(VideoStatus.UPLOADED);

        return modelMapper.map(video, VideoResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public VideoResponseDTO getVideo(UUID videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId));

        return modelMapper.map(video, VideoResponseDTO.class);
    }

    private String buildObjectKey(UUID videoId, String originalFilename) {
        String extension = extractExtension(originalFilename);

        return ORIGINALS_PREFIX + videoId + extension;
    }

    // TODO учесть, что нужно будет переделать с использованием ffprobe
    private String extractExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex).toLowerCase(Locale.ROOT);
    }
}
