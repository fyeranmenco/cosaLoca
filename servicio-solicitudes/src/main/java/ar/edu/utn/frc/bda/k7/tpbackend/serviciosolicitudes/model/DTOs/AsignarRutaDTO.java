package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos;

import java.util.List;

public record AsignarRutaDTO (
	List<TramoDTO> tramos
){
	
}

record TramoDTO (
	String direccionOrigen,
	String latitudOrigen,
	String longitudOrigen,
	String direccionDestino,
	String latitudDestino,
	String longitudDestino,
	Double distanciaKm,
	Double duracionHoras
){
}
