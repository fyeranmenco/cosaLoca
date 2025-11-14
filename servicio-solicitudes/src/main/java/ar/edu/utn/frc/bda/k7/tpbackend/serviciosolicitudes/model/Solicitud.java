package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class Solicitud {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contenedor_id", referencedColumnName = "id")
    private Contenedor contenedor;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ruta_id", referencedColumnName = "id")
    private Ruta ruta;

    private String direccionOrigen;
    private String latitudOrigen;
    private String longitudOrigen;
    
    private String direccionDestino;
    private String latitudDestino;
    private String longitudDestino;

    private String estado; // BORRADOR, PROGRAMADA, EN_BUSQUEDA_CONTENEDOR, EN_TRANSITO, ENTREGADA

    private Double costoEstimado;
    private Double tiempoEstimado; 
    private Double costoFinal;
    private Double tiempoReal;
}