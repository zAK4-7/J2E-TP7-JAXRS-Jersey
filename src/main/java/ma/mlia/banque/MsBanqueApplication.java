package ma.mlia.banque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the bank microservice exposing a JAX-RS (Jersey) API.
 */
@SpringBootApplication
public class MsBanqueApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsBanqueApplication.class, args);
    }
}
