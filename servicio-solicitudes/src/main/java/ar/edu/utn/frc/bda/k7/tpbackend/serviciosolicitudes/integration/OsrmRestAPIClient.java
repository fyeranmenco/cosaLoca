package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class OsrmRestAPIClient {
	private final WebClient webClient;

    public OsrmRestAPIClient(@Value("${osrm.base-url}") String osrmBaseUrl) {
        this.webClient = WebClient.builder().baseUrl(osrmBaseUrl).build();
    }

    public Mono<String> obtenerRutasAlternativas(String coordsOrigen, String coordsDestino) {
        
        // Formato: /route/v1/driving/lon1,lat1;lon2,lat2
        String path = String.format("/route/v1/driving/%s;%s", coordsOrigen, coordsDestino);

        return this.webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(path)
                .queryParam("alternatives", "true") // ¡La clave para 3-4 opciones!
                .queryParam("steps", "false")       // No necesitamos instrucciones paso-a-paso
                .queryParam("overview", "full")     // Geometría para dibujar el mapa
                .build())
            .retrieve()
            .bodyToMono(String.class); // Por ahora, solo obtenemos el JSON como String
    }
}

