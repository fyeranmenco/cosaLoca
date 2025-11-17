package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient; 
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.ClienteDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class ClienteRestAPIClient {

    private final RestClient restClient;  

    public ClienteRestAPIClient(@Value("${service.cliente.url}") String clienteServiceUrl) {
         this.restClient = RestClient.builder().baseUrl(clienteServiceUrl).build();
    }

 
	public boolean existeClientePorKeycloakId(String token) {
        try {
            restClient.get() 
                    .uri("/existe/keycloak")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity(); 
            return true;
        } catch (HttpClientErrorException e) {
             if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
             throw e;
        } catch (Exception e) {
            return false;
        }
    }

	public ClienteDTO getClientePorKeycloakId(String token) {
        return restClient.get()  
                .uri("/keycloak")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(ClienteDTO.class);  
    }
}