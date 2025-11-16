package ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.model.dtos;

public record CrearDepositoDTO (
	String nombre,
    String direccion,
	String Latitud,
	String Longitud,
	Double distanciaCentral){}
