package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

// DTO para recibir la info del camión
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.DTOs.CamionDTO;

@Component
public class CamionRestAPIClient {

    private final RestTemplate restTemplate;

    @Value("${service.camion.url}")
    private String camionServiceUrl;

    public CamionRestAPIClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Llama al servicio de camiones para validar la capacidad [cite: 65]
    public CamionDTO[] obtenerCamionesDisponibles(Double peso, Double volumen) {
        String url = UriComponentsBuilder.fromHttpUrl(camionServiceUrl)
                .path("/disponibles/aptos")
                .queryParam("peso", peso)
                .queryParam("volumen", volumen)
                .toUriString();

        return restTemplate.getForObject(url, CamionDTO[].class);
    }
    
    // Marca un camión como no disponible
    public void actualizarDisponibilidad(Long camionId, boolean disponible) {
        String url = camionServiceUrl + "/" + camionId + "/disponibilidad";
        restTemplate.put(url, Map.of("disponible", disponible));
    }
}