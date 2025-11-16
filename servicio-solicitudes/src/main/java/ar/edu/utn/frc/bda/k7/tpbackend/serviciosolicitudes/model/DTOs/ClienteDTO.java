package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos;

import lombok.Data;

@Data
public class ClienteDTO {
    private Long dNI;
    private String nombre;
    private String apellido;
    private String keycloakId;
}