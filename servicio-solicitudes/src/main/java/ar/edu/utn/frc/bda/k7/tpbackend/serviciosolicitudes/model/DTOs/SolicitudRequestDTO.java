package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos;

public record SolicitudRequestDTO(
		String longitudDestino,
		Double pesoContenedor,
		Double volumenContenedor,
		String direccionOrigen,
		String latitudOrigen,
		String longitudOrigen,
		String direccionDestino,
		String latitudDestino
) {

}
