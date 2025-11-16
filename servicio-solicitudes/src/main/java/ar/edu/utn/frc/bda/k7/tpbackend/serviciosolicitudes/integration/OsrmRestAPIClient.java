package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.osrm.OsrmResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component; // Asegúrate de que esté anotado como Component
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component // <-- ¡Añade esto!
public class OsrmRestAPIClient {
	private final WebClient webClient;

    // ¡CORREGIDO! Usa el valor de tu application.yml
    public OsrmRestAPIClient(@Value("${service.osrm.url}") String osrmBaseUrl) {
        this.webClient = WebClient.builder().baseUrl(osrmBaseUrl).build();
    }

    // Devuelve el DTO parseado, no un String
    public Mono<OsrmResponse> obtenerRutasAlternativas(String coordsOrigen, String coordsDestino) {
        
        // Formato: /route/v1/driving/lon1,lat1;lon2,lat2
        String path = String.format("/route/v1/driving/%s;%s", coordsOrigen, coordsDestino);

        return this.webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(path)
                .queryParam("alternatives", "true") // ¡La clave para 3-4 opciones!
                .queryParam("steps", "false")
                .queryParam("overview", "full")
                .build())
            .retrieve()
            .bodyToMono(OsrmResponse.class); // <-- Modificado
    }
}