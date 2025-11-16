package ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.model.Camion;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.repository.PersistenciaCamion;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CamionService {

    private final PersistenciaCamion persistenciaCamion;

    public Camion guardarCamion(Camion camion) {
        camion.setDisponible(true); // Por defecto al crearlo
        return persistenciaCamion.save(camion);
    }

    public List<Camion> obtenerTodos() {
        return persistenciaCamion.findAll();
    }

    public Optional<Camion> obtenerCamionPorId(Long id) {
        return persistenciaCamion.findById(id);
    }
    
    public List<Camion> obtenerCamionesDisponiblesAptos(Double peso, Double volumen) {
        return persistenciaCamion.findAll().stream()
                .filter(Camion::getDisponible)
                .filter(camion -> camion.getCapacidadPeso() >= peso)
                .filter(camion -> camion.getCapacidadVolumen() >= volumen)
                .collect(Collectors.toList());
    }
    
    public Camion actualizarDisponibilidad(Long id, boolean disponible) {
        Camion camion = persistenciaCamion.findById(id)
                .orElseThrow(() -> new RuntimeException("Camión no encontrado"));
        camion.setDisponible(disponible);
        return persistenciaCamion.save(camion);
    }

	public Camion asignarTransportista(Long camionId, String keycloakId) {
        Camion camion = persistenciaCamion.findById(camionId)
                .orElseThrow(() -> new NoSuchElementException("Camión no encontrado"));
        
        camion.getChofer().setIdUsuarioKeyCloak(keycloakId);
        return persistenciaCamion.save(camion);
    }

    public List<Camion> obtenerMisCamiones(String keycloakId) {
        return persistenciaCamion.findByChoferIdUsuarioKeyCloak(keycloakId);
    }
}