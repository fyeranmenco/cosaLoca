package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient; // <-- Importar
import org.springframework.core.ParameterizedTypeReference; // <-- Importar para Listas
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.CamionDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;

@Component
public class CamionRestAPIClient {

    private final RestClient restClient; // <-- Cambiado
    
	public CamionRestAPIClient(@Value("${service.camion.url}") String camionServiceUrl) {
        // <-- Cambiado
        this.restClient = RestClient.builder().baseUrl(camionServiceUrl).build();
    }

    public CamionDTO[] obtenerCamionesDisponibles(Double peso, Double volumen, String token) {
        return restClient.get() // <-- Cambiado
                .uri(uriBuilder -> uriBuilder
                        .path("/disponibles/aptos")
                        .queryParam("peso", peso)
                        .queryParam("volumen", volumen)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CamionDTO[].class); // <-- Sin .block()
    }

	public CamionDTO obtenerCamionPorId(Long camionId, String token) {
        return restClient.get() // <-- Cambiado
                .uri("/{id}", camionId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CamionDTO.class); // <-- Sin .block()
    }

    public void actualizarDisponibilidad(Long camionId, boolean disponible, String token) {
        restClient.put() // <-- Cambiado
                .uri("/{id}/disponibilidad", camionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("disponible", disponible)) // <-- .body() en lugar de .bodyValue()
                .retrieve()
                .toBodilessEntity(); // <-- Sin .block()
    }

	public List<CamionDTO> obtenerMisCamiones(String token) {
        return restClient.get() // <-- Cambiado
                .uri("/mis-camiones")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                // --- sintaxis de RestClient para listas genéricas ---
                .body(new ParameterizedTypeReference<List<CamionDTO>>() {});
    }
}