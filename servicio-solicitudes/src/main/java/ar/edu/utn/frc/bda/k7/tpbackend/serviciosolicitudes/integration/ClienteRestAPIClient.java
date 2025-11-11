package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration;

import org.springframework.beans.factory.annotation.Value;
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

    public boolean existeCliente(Long clienteId) {
        try {
            restTemplate.getForObject(clienteServiceUrl + "/clientes/{dNI}", Void.class, clienteId);
            return true; 
        } catch (Exception e) {
            return false;
        }
    }
}
