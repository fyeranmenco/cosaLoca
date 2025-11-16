package ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor  
public class Chofer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String nombre;
	private String licencia;
	@Column(unique = true, nullable = true) // Puede ser nulo si no está asignado
	private String idUsuarioKeyCloak;
}
