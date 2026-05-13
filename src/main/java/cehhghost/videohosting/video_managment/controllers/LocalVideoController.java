package cehhghost.videohosting.video_managment.controllers;

import cehhghost.videohosting.video_managment.services.LocalVideoStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@Deprecated
@RequestMapping("/api/videos/local")
public class LocalVideoController {
    private final LocalVideoStorageService localVideoStorageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) {

        log.info("Upload request: title={}, file={}, size={}",
                title, file.getOriginalFilename(), file.getSize());

        String savedFilename = localVideoStorageService.save(file);

        Map<String, String> response = Map.of(
                "status", "UPLOADED",
                "filename", savedFilename,
                "title", title
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
