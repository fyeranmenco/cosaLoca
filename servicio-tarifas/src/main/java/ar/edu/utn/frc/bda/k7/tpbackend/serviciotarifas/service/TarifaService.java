package ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.service;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.Tarifa;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.dtos.ActualizarTarifaDTO;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.dtos.CrearTarifaDTO;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.repository.PersistenciaTarifa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarifaService {

    private final PersistenciaTarifa tarifaRepository;

    public List<Tarifa> getAll() {
        return tarifaRepository.findAll();
    }

    public Tarifa actualizarTarifa(ActualizarTarifaDTO dto) {
        Tarifa tarifa = new Tarifa(dto);
        return tarifaRepository.save(tarifa);
    }

	public Tarifa crearTarifa(CrearTarifaDTO dto) {
		Tarifa tarifa = new Tarifa(dto);
		return tarifaRepository.save(tarifa);
	}
}