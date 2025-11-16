package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.osrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmResponse {
    @JsonProperty("routes")
    private List<OsrmRoute> routes;
}