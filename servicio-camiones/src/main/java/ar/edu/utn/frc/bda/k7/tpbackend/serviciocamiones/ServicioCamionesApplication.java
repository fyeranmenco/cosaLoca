package ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.repository")
public class ServicioCamionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioCamionesApplication.class, args);
    }
}