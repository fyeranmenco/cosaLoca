package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoSugeridoDTO {
    private String duracionFormateada; // ej. "7h 30m"
    private double distanciaKm;
    private String geometria; // Para dibujar en el mapa
    private long duracionSegundos;
    private double distanciaMetros;
}