package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
// import reactor.core.publisher.Mono;

@Component
public class ClienteRestAPIClient {

    private final WebClient webClient;

    public ClienteRestAPIClient(@Value("${service.cliente.url}") String clienteServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(clienteServiceUrl).build();
    }

    public boolean existeCliente(Long clienteDNI, String token) {
        try {
            System.out.println("Token recibido en ClienteRestAPIClient: " + token);

            webClient.get()
                    .uri("/{dNI}/existe", clienteDNI)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}