package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos;

public record SolicitudRequestDTO(
		String longitudDestino,
		Double pesoContenedor,
		Double volumenContenedor,
		String direccionDestino,
		String latitudDestino
) {

}
