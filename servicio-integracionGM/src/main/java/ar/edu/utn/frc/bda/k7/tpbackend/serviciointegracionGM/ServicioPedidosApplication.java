package ar.edu.utn.frc.bda.k7.ejemploSpring.serviciopedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ar.edu.utn.frc.bda.k7.ejemploSpring.serviciopedidos.repository")
public class ServicioPedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioPedidosApplication.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}