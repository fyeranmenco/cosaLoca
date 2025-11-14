package ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data @AllArgsConstructor @NoArgsConstructor 
public class Camion {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    
    private String dominio; 
    private String nombreTransportista;
    private String telefonoTransportista;
    
    private Double capacidadPeso; 
    private Double capacidadVolumen; 
    
    private Double consumoCombustibleKm; 
    private Double costoTrasladoPorKm; 
    
    private Boolean disponible; 
}