package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class Contenedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Double peso;
    private Double volumen;
    private Long clienteId; 
    
    //PENDIENTE_RETIRO, EN_TRANSITO, EN_DEPOSITO, ENTREGADO
    private String estado; 
}
