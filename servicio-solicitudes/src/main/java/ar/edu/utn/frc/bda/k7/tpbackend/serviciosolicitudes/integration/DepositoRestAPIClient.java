package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.DepositoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class DepositoRestAPIClient {

    private final WebClient webClient;

    public DepositoRestAPIClient(@Value("${service.deposito.url}") String depositoServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(depositoServiceUrl).build();
    }

    public Mono<DepositoDTO> obtenerDepositoPorId(Long id, String token) {
        return webClient.get()
                .uri("/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DepositoDTO.class);
    }
}