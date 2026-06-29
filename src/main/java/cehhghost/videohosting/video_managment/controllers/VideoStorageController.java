package cehhghost.videohosting.video_managment.controllers;

import cehhghost.videohosting.video_managment.dtos.InitVideoUploadRequestDTO;
import cehhghost.videohosting.video_managment.dtos.InitVideoUploadResponseDTO;
import cehhghost.videohosting.video_managment.dtos.VideoStorageObjectResponseDTO;
import cehhghost.videohosting.video_managment.services.VideoStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/video-storage/videos")
public class VideoStorageController {
    private final VideoStorageService videoStorageService;

    @PostMapping("/upload/init")
    public ResponseEntity<InitVideoUploadResponseDTO> initUpload(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody InitVideoUploadRequestDTO requestDTO) {
        UUID ownerId = videoStorageService.parseUserId(jwt.getSubject());

        InitVideoUploadResponseDTO responseDTO = videoStorageService.initUpload(ownerId, requestDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/{videoId}/upload/complete")
    public ResponseEntity<VideoStorageObjectResponseDTO> completeUpload(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID videoId) {
        UUID ownerId = videoStorageService.parseUserId(jwt.getSubject());

        VideoStorageObjectResponseDTO responseDTO = videoStorageService.completeUpload(ownerId, videoId);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{videoId}/storage-object")
    public ResponseEntity<VideoStorageObjectResponseDTO> getVideo(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID videoId) {
        UUID ownerId = videoStorageService.parseUserId(jwt.getSubject());

        VideoStorageObjectResponseDTO responseDTO = videoStorageService.getVideo(ownerId, videoId);

        return ResponseEntity.ok(responseDTO);
    }
}
