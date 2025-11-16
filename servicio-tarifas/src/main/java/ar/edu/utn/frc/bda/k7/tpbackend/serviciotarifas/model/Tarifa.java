package ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.dtos.ActualizarTarifaDTO;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.dtos.CrearTarifaDTO;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarifa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    private Double valor;
    private String descripcion;

	public Tarifa(ActualizarTarifaDTO dto) {
		this.id = dto.id();
		this.valor = dto.valor();
		this.descripcion = dto.descripcion();
	}

	public Tarifa(CrearTarifaDTO dto) {
		this.valor = dto.valor();
		this.descripcion = dto.descripcion();
	}
}