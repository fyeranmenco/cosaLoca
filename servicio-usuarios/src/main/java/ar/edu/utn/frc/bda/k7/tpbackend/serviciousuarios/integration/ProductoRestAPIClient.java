package ar.edu.utn.frc.bda.k7.ejemploSpring.serviciopedidos.integration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductoRestAPIClient{

    private final RestTemplate restTemplate;

    @Value("${service.producto.url}")
    private String productoServiceUrl; 

    public ProductoRestAPIClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean existeProducto(Long productoId) {
        try {
            restTemplate.getForObject(productoServiceUrl + "/producto/{id}", Void.class, productoId);
            return true; 
        } catch (Exception e) {
            return false; 
        }
    }

	public boolean verificarYDescontarStock(Long productoId, int cantidad) {
        
        String url = productoServiceUrl + "/productos/{id}/descontar";
        
        Map<String, Integer> body = new HashMap<>();
        body.put("cantidad", cantidad);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Integer>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                requestEntity, 
                Void.class, 
                productoId
            );

            return response.getStatusCode().is2xxSuccessful(); 

        } catch (HttpClientErrorException.Conflict e) {
            System.err.println("Error de stock: Stock insuficiente o producto no disponible.");
            return false;
            
        } catch (HttpClientErrorException.NotFound e) {
            System.err.println("Error de producto: Producto no encontrado.");
            return false;
            
        } catch (Exception e) {
            System.err.println("Error de comunicación con ServicioProductos: " + e.getMessage());
            return false;
        }
    }
}
