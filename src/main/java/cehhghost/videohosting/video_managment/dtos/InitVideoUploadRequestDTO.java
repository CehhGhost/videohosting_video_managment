package cehhghost.videohosting.video_managment.dtos;

import cehhghost.videohosting.video_managment.enums.VideoCategory;
import cehhghost.videohosting.video_managment.enums.VideoVisibility;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitVideoUploadRequestDTO {
    @NotBlank(message = "Video title is required")
    private String title;

    private String description;

    @NotBlank(message = "Original filename is required")
    private String originalFilename;

    @NotBlank(message = "Content type is required")
    @Pattern(regexp = "^video/.+", message = "Only video files are allowed")
    private String contentType;

    @NotNull(message = "Video size is required")
    @Positive(message = "Video size must be positive")
    private Long sizeBytes;

    @NotNull(message = "Video visibility is required")
    private VideoVisibility visibility;

    @Builder.Default
    private VideoCategory category = VideoCategory.OTHER;

    @Size(max = 20, message = "Video must not have more than 20 tags")
    @Builder.Default
    private Set<@Size(max = 64, message = "Tag must not exceed 64 characters") String> tags = new LinkedHashSet<>();
}
