package ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.model.Camion;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.repository.PersistenciaCamion;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CamionService {

    private final PersistenciaCamion persistenciaCamion;

    public Camion guardarProducto(Camion camion) {
        return persistenciaCamion.save(camion);
    }

	public List<Camion> obtenerTodos() {
		return persistenciaCamion.findAll();
	}

    public Optional<Camion> obtenerCamionPorId(Long id) {
        return persistenciaCamion.findById(id);
    }
}