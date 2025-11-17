package ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class Cliente {

    @Id
    private Long dNI;
    
	private String nombre;
    private String apellido;
    private String telefono;
    private String email;
	private String idUsuarioKeyCloak;
}