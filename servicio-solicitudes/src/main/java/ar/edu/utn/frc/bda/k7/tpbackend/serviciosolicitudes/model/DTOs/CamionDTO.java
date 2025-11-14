package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.DTOs;

public record CamionDTO(
	Long id,
	String patente,
	String modelo,
	Double capacidadCargaToneladas,
	Double volumenM3,
	String estado
){

}
