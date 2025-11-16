package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos;

import lombok.Data;
// Este DTO debe coincidir con la entidad Deposito de tu otro servicio
@Data
public class DepositoDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private String Latitud;
    private String Longitud;
}