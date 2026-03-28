package DevNectar.StaffBridge.devNectar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DevNectarApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevNectarApplication.class, args);
	}
}
