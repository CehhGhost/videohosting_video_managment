package cehhghost.videohosting.video_managment.controllers;

import cehhghost.videohosting.video_managment.dtos.InitVideoUploadRequestDTO;
import cehhghost.videohosting.video_managment.dtos.InitVideoUploadResponseDTO;
import cehhghost.videohosting.video_managment.dtos.VideoStorageObjectResponseDTO;
import cehhghost.videohosting.video_managment.services.VideoStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/video-storage/videos")
public class VideoStorageController {
    private final VideoStorageService videoStorageService;

    @PostMapping("/upload/init")
    public ResponseEntity<InitVideoUploadResponseDTO> initUpload(
            @Valid @RequestBody InitVideoUploadRequestDTO requestDTO
    ) {
        InitVideoUploadResponseDTO responseDTO = videoStorageService.initUpload(requestDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/{videoId}/upload/complete")
    public ResponseEntity<VideoStorageObjectResponseDTO> completeUpload(
            @PathVariable UUID videoId
    ) {
        VideoStorageObjectResponseDTO responseDTO = videoStorageService.completeUpload(videoId);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{videoId}/storage-object")
    public ResponseEntity<VideoStorageObjectResponseDTO> getVideo(
            @PathVariable UUID videoId
    ) {
        VideoStorageObjectResponseDTO responseDTO = videoStorageService.getVideo(videoId);

        return ResponseEntity.ok(responseDTO);
    }
}
