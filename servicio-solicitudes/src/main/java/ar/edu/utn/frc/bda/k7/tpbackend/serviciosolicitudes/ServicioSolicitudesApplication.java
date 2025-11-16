package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.repository")
@Slf4j
public class ServicioSolicitudesApplication {

    public static void main(String[] args) {
		log.info("Iniciando Servicio de Solicitudes...");
        SpringApplication.run(ServicioSolicitudesApplication.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}