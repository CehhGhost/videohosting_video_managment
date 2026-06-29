package cehhghost.videohosting.video_managment.dtos;

import cehhghost.videohosting.video_managment.enums.VideoVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
