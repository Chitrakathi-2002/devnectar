package DevNectar.StaffBridge.CloudNex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudNexApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudNexApplication.class, args);
	}
}
