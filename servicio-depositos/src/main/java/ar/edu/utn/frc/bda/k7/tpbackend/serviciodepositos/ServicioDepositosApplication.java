package ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.repository")
public class ServicioDepositosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioDepositosApplication.class, args);
    }
}