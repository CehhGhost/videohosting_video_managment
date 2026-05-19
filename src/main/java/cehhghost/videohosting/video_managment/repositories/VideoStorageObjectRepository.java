package cehhghost.videohosting.video_managment.repositories;

import cehhghost.videohosting.video_managment.models.VideoStorageObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VideoStorageObjectRepository extends JpaRepository<VideoStorageObject, UUID> {
    Optional<VideoStorageObject> findByVideoId(UUID videoId);
    boolean existsByVideoId(UUID videoId);
}
