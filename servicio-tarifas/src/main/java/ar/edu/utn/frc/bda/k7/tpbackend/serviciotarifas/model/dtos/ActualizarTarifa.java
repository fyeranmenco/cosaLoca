package ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.dtos;

public record ActualizarTarifaDTO(
    String clave,
    Double valor,
    String descripcion
) {
}