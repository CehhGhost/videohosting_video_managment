package cehhghost.videohosting.video_managment.controllers;

import cehhghost.videohosting.video_managment.dtos.VideoPlaybackUrlResponseDTO;
import cehhghost.videohosting.video_managment.services.VideoStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/video-storage/videos")
public class InternalVideoStorageController {
    private final VideoStorageService videoStorageService;

    @GetMapping("/{videoId}/playback-url")
    public ResponseEntity<VideoPlaybackUrlResponseDTO> createPlaybackUrl(@PathVariable UUID videoId) {
        VideoPlaybackUrlResponseDTO responseDTO = videoStorageService.createPlaybackUrl(videoId);

        return ResponseEntity.ok(responseDTO);
    }
}
