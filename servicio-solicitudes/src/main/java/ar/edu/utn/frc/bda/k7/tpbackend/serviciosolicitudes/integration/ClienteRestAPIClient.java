package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ClienteRestAPIClient {

    private final RestTemplate restTemplate;

    @Value("${service.cliente.url}")
    private String clienteServiceUrl; 

    public ClienteRestAPIClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Requerido por RF 1.2
    public boolean existeCliente(Long clienteDNI, String token) {
        try {
			System.out.println("Token recibido en ClienteRestAPIClient: " + token);
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(token);
            restTemplate.exchange(clienteServiceUrl + "/{dNI}/existe", HttpMethod.GET, new HttpEntity<String>(headers), Void.class, clienteDNI);
            return true; 
        } catch (Exception e) {
            return false;
        }
    }
}