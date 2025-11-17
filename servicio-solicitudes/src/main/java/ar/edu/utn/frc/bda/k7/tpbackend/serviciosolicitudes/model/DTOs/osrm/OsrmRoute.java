package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.osrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmRoute {
    @JsonProperty("duration")
    private double duration; 

    @JsonProperty("distance")
    private double distance;  

    @JsonProperty("geometry")
    private String geometry;  
}