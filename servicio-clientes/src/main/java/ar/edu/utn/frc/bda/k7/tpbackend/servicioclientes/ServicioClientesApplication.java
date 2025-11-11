package ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.repository")
public class ServicioClientesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioClientesApplication.class, args);
    }
}