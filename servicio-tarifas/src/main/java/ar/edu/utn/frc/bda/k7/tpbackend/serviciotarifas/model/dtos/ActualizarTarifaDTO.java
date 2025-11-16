package ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.dtos;

public record ActualizarTarifaDTO(
	Long id,
    Double valor,
    String descripcion
) {
}