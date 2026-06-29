package cehhghost.videohosting.video_managment.dtos;

import cehhghost.videohosting.video_managment.enums.VideoVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVideoMetadataRequestDTO {
    private String title;
    private String description;
    private UUID ownerId;
    private VideoVisibility visibility;
}
