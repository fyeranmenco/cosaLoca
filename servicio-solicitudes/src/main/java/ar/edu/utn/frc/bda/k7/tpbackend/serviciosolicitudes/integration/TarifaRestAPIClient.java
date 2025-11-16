package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.TarifaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient; // <-- Importar
import org.springframework.core.ParameterizedTypeReference; // <-- Importar para Listas
import java.util.List;

@Component
public class TarifaRestAPIClient {

    private final RestClient restClient; // <-- Cambiado

    public TarifaRestAPIClient(@Value("${service.tarifa.url}") String tarifaServiceUrl) {
        // <-- Cambiado
        this.restClient = RestClient.builder().baseUrl(tarifaServiceUrl).build();
    }

	public List<TarifaDTO> getTarifas(String token) {
        return restClient.get() // <-- Cambiado
                .uri("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                // --- sintaxis de RestClient para listas genéricas ---
                .body(new ParameterizedTypeReference<List<TarifaDTO>>() {});
    }
}