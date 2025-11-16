package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class Tramo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String origen; // Puede ser "ORIGEN" o "DEPOSITO_ID_X"
    private String destino; // Puede ser "DESTINO" o "DEPOSITO_ID_Y"
    
    private String estado; // ESTIMADO, ASIGNADO, INICIADO, FINALIZADO [cite: 87]
    
    private Long camionId; 
    
    private Double costoEstimado;
    private Double costoReal;
    
    private LocalDateTime fechaHoraInicioEstimada;
    private LocalDateTime fechaHoraFinEstimada;
    
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;

	// --- NUEVOS CAMPOS ---
    private Double distanciaMetros;
    private Long duracionEstimadaSegundos;
}