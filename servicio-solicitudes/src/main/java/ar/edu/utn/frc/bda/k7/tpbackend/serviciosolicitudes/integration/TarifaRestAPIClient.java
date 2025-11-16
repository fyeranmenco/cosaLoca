package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.TarifaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Component
public class TarifaRestAPIClient {

    private final WebClient webClient;

    public TarifaRestAPIClient(@Value("${service.tarifa.url}") String tarifaServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(tarifaServiceUrl).build();
    }

	@SuppressWarnings("unchecked")
	public List<TarifaDTO> getTarifas(String token) {
        return webClient.get()
                .uri("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(List.class) // Obtiene la lista genérica
                .block(); // Bloquea para obtener el resultado
    }
}