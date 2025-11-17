package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient; 
import org.springframework.core.ParameterizedTypeReference;  
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.CamionDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

@Component
public class CamionRestAPIClient {

    private final RestClient restClient;  
    
	public CamionRestAPIClient(@Value("${service.camion.url}") String camionServiceUrl) {
         this.restClient = RestClient.builder().baseUrl(camionServiceUrl).build();
    }

	public List<CamionDTO> getAllCamiones(String token) {
		return restClient.get() 
				.uri("/")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(new ParameterizedTypeReference<List<CamionDTO>>() {});
	}

    public CamionDTO[] obtenerCamionesDisponibles(Double peso, Double volumen, String token) {
        return restClient.get() 
                .uri(uriBuilder -> uriBuilder
                        .path("/disponibles/aptos")
                        .queryParam("peso", peso)
                        .queryParam("volumen", volumen)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CamionDTO[].class);  
    }

	public CamionDTO obtenerCamionPorId(Long camionId, String token) {
        return restClient.get()  
                .uri("/{id}", camionId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CamionDTO.class);  
    }

    public void actualizarDisponibilidad(Long camionId, boolean disponible, String token) {
        restClient.put() 
                .uri("/{id}/disponibilidad", camionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("disponible", disponible)) 
                .retrieve()
                .toBodilessEntity(); 
    }

	public List<CamionDTO> obtenerMisCamiones(Jwt principal) {
        return restClient.get()  
                .uri("/mis-camiones")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + principal.getTokenValue())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                 .body(new ParameterizedTypeReference<List<CamionDTO>>() {});
    }
}