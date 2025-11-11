package ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ar.edu.utn.frc.bda.k7.tpbackend.servicioDeposito.repository")
public class ServicioDepositoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioDepositoApplication.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}