package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos;

public record CamionDTO(
	Long id,
	String patente,
	String modelo,
	Double capacidadCargaToneladas, // <- Renombrado (asumo que en el servicio de camiones se llama capacidadPeso)
	Double volumenM3, // <- Renombrado (asumo que en el servicio de camiones se llama capacidadVolumen)
	String estado,
    Double consumoCombustibleKm, // <-- NUEVO
    Double costoTrasladoPorKm
){

}
