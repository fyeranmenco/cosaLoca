package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos;

public record CamionDTO(
	Long id,
	String patente,
	String modelo,
	Double capacidadCargaToneladas, 
	Double volumenM3, 
	String estado,
    Double consumoCombustibleKm,  
    Double costoTrasladoPorKm
){

}
