package cehhghost.videohosting.video_managment.clients;

import cehhghost.videohosting.video_managment.dtos.CreateVideoMetadataRequestDTO;
import cehhghost.videohosting.video_managment.dtos.CreateVideoMetadataResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class VideoMetadataClient {
    private final RestClient videoMetadataRestClient;

    public CreateVideoMetadataResponseDTO createVideoMetadata(
            CreateVideoMetadataRequestDTO requestDTO
    ) {
        return videoMetadataRestClient.post()
                .uri("/api/internal/videos")
                .body(requestDTO)
                .retrieve()
                .body(CreateVideoMetadataResponseDTO.class);
    }
}
