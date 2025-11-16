package ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.model;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.model.dtos.CrearDepositoDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String direccion;
    private String Latitud;
	private String Longitud;
	private Double distanciaCentral;

	public Deposito(CrearDepositoDTO depositoDTO) {
		this.nombre = depositoDTO.nombre();
		this.direccion = depositoDTO.direccion();
		this.Latitud = depositoDTO.Latitud();
		this.Longitud = depositoDTO.Longitud();
		this.distanciaCentral = depositoDTO.distanciaCentral();
	}
}