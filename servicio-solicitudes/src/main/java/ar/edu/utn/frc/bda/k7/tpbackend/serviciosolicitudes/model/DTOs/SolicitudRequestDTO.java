package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos;

public record SolicitudRequestDTO(
		Long clienteDNI,
		Double pesoContenedor,
		Double volumenContenedor,
		String direccionOrigen,
		String latitudOrigen,
		String longitudOrigen,
		String direccionDestino,
		String latitudDestino,
		String longitudDestino
) {

}
