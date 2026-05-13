package cehhghost.videohosting.video_managment.controllers;

import cehhghost.videohosting.video_managment.dtos.InitVideoUploadRequestDTO;
import cehhghost.videohosting.video_managment.dtos.InitVideoUploadResponseDTO;
import cehhghost.videohosting.video_managment.dtos.VideoResponseDTO;
import cehhghost.videohosting.video_managment.services.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/videos/local")
public class VideoController {
    private final VideoService videoService;

    @PostMapping("/upload/init")
    public ResponseEntity<InitVideoUploadResponseDTO> initUpload(
            @RequestBody InitVideoUploadRequestDTO requestDTO
    ) {
        InitVideoUploadResponseDTO responseDTO = videoService.initUpload(requestDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/{videoId}/upload/complete")
    public ResponseEntity<VideoResponseDTO> completeUpload(
            @PathVariable UUID videoId
    ) {
        VideoResponseDTO responseDTO = videoService.completeUpload(videoId);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoResponseDTO> getVideo(
            @PathVariable UUID videoId
    ) {
        VideoResponseDTO responseDTO = videoService.getVideo(videoId);

        return ResponseEntity.ok(responseDTO);
    }
}
