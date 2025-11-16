package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.DepositoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient; // <-- Importar
// import reactor.core.publisher.Mono; // <-- Ya no se usa

@Component
public class DepositoRestAPIClient {

    private final RestClient restClient; // <-- Cambiado

    public DepositoRestAPIClient(@Value("${service.deposito.url}") String depositoServiceUrl) {
        // <-- Cambiado
        this.restClient = RestClient.builder().baseUrl(depositoServiceUrl).build();
    }

    public DepositoDTO obtenerDepositoPorId(Long id, String token) {
        return restClient.get() // <-- Cambiado
                .uri("/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DepositoDTO.class); // <-- Sin Mono ni .block()
    }
}