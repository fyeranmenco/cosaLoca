package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.CamionDTO;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
// import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class CamionRestAPIClient {

    private final WebClient webClient;
    
	public CamionRestAPIClient(@Value("${service.camion.url}") String camionServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(camionServiceUrl).build();
    }

    public CamionDTO[] obtenerCamionesDisponibles(Double peso, Double volumen, String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/disponibles/aptos")
                        .queryParam("peso", peso)
                        .queryParam("volumen", volumen)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CamionDTO[].class)
                .block(); 
    }

	public CamionDTO obtenerCamionPorId(Long camionId, String token) {
        return webClient.get()
                .uri("/{id}", camionId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CamionDTO.class)
                .block(); // Asumimos que el admin/transportista tiene permiso
    }

    public void actualizarDisponibilidad(Long camionId, boolean disponible, String token) {
        webClient.put()
                .uri("/{id}/disponibilidad", camionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("disponible", disponible))
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}