package cehhghost.videohosting.video_managment.repositories;

import cehhghost.videohosting.video_managment.models.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {
}
