package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.osrm.OsrmResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;  

@Component 
public class OsrmRestAPIClient {
	private final RestClient restClient;  

    public OsrmRestAPIClient(@Value("${service.osrm.url}") String osrmBaseUrl) {
         this.restClient = RestClient.builder().baseUrl(osrmBaseUrl).build();
    }

    public OsrmResponse obtenerRutasAlternativas(String coordsOrigen, String coordsDestino) {
        
        String path = String.format("/route/v1/driving/%s;%s", coordsOrigen, coordsDestino);

        return this.restClient.get()  
            .uri(uriBuilder -> uriBuilder
                .path(path)
                .queryParam("alternatives", "true")
                .queryParam("steps", "false")
                .queryParam("overview", "full")
                .build())
            .retrieve()
            .body(OsrmResponse.class); 
    }
}