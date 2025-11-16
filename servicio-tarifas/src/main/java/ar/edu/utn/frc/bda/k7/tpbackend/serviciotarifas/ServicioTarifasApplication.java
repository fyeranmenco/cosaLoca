package ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.repository")
public class ServicioTarifasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioTarifasApplication.class, args);
    }
}