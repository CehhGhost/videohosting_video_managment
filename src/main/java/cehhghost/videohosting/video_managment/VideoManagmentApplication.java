package cehhghost.videohosting.video_managment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VideoManagmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoManagmentApplication.class, args);
	}

}
