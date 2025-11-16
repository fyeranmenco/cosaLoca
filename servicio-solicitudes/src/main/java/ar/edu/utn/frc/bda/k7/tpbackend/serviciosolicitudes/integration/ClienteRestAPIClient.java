package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient; // <-- Importar
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.ClienteDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class ClienteRestAPIClient {

    private final RestClient restClient; // <-- Cambiado

    public ClienteRestAPIClient(@Value("${service.cliente.url}") String clienteServiceUrl) {
        // <-- Cambiado
        this.restClient = RestClient.builder().baseUrl(clienteServiceUrl).build();
    }

    // Método obsoleto eliminado (existeCliente por DNI)

	public boolean existeClientePorKeycloakId(String token) {
        try {
            restClient.get() // <-- Cambiado
                    .uri("/existe/keycloak")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity(); // <-- Sin .block()
            return true;
        } catch (HttpClientErrorException e) {
            // Si da 404 (Not Found), el cliente no existe
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            // Si es otro error (401, 500), lanza la excepción
            throw e;
        } catch (Exception e) {
            return false;
        }
    }

	public ClienteDTO getClientePorKeycloakId(String token) {
        return restClient.get() // <-- Cambiado
                .uri("/keycloak")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(ClienteDTO.class); // <-- Sin .block()
    }
}