package ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
	@OneToOne
    @JoinColumn(name = "chofer_id", referencedColumnName = "id")
	private Chofer chofer;
    
    private Double capacidadPeso; 
    private Double capacidadVolumen; 
    
    private Double consumoCombustibleKm; 
    private Double costoTrasladoPorKm; 
    
    private Boolean disponible; 
}