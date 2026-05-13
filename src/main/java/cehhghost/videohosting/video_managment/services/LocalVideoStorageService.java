package cehhghost.videohosting.video_managment.services;

import cehhghost.videohosting.video_managment.exceptions.StorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@Deprecated
public class LocalVideoStorageService {
    @Value("${video.storage.originals-path}")
    private String originalsPath;

    @Value("${video.storage.temp-path}")
    private String tempPath;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(originalsPath));
            Files.createDirectories(Paths.get(tempPath));
            log.info("Storage folders ready: originals={}, temp={}", originalsPath, tempPath);
        } catch (IOException e) {
            throw new StorageException("Cannot create storage folders", e);
        }
    }

    public String save(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String uniqueName = UUID.randomUUID() + "_" + originalFilename;
        Path destination = Paths.get(originalsPath).resolve(uniqueName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("Video saved: {} -> {}", originalFilename, destination);
            return uniqueName;
        } catch (IOException e) {
            throw new StorageException("Failed to save file: " + originalFilename, e);
        }
    }
}
