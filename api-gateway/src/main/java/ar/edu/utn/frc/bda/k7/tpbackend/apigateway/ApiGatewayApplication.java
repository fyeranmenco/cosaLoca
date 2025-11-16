package ar.edu.utn.frc.bda.k7.tpbackend.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
		// logger.info("Iniciando ApiGatewayApplication...");
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}