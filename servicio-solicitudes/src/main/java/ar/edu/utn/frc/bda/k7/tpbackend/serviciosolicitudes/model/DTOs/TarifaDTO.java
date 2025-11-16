package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos;

import lombok.Data;

@Data
public class TarifaDTO {
    private String clave;
    private Double valor;
    private String descripcion;
}