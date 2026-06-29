package cehhghost.videohosting.video_managment.models;

import cehhghost.videohosting.video_managment.enums.UploadStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="_video_storage_objects")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoStorageObject {
    @Id
    private UUID id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "video_id", nullable = false, unique = true)
    private UUID videoId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "object_key", nullable = false, unique = true, length = 1024)
    private String objectKey;

    @Column(name = "original_size_bytes", nullable = false)
    private Long originalSizeBytes;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false)
    private UploadStatus uploadStatus;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
