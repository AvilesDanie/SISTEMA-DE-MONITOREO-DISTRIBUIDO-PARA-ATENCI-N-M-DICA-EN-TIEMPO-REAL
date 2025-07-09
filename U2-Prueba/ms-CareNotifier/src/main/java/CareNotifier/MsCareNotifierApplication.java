package CareNotifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // <-- ESTO ES LO QUE FALTA
public class MsCareNotifierApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsCareNotifierApplication.class, args);
	}

}
