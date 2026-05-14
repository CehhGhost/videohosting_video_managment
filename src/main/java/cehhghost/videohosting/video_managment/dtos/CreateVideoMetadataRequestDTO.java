package cehhghost.videohosting.video_managment.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVideoMetadataRequestDTO {
    private String title;
    private String description;
}
