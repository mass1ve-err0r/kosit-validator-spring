package software.baig.oss.kositspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class KoSITValidatorAsAServiceApplication {


    static void main(String[] args) {
        SpringApplication service = new SpringApplication(KoSITValidatorAsAServiceApplication.class);
        service.run(args);
    }

}
