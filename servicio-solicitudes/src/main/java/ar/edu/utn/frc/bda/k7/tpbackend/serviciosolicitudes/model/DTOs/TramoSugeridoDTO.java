package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoSugeridoDTO {
    private String duracionFormateada;
    private double distanciaKm;
    private String geometria;  
    private long duracionSegundos;
    private double distanciaMetros;
}