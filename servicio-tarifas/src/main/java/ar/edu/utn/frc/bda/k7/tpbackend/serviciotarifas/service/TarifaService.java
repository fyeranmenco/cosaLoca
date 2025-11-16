package ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.service;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.Tarifa;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.dtos.ActualizarTarifaDTO;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    public List<Tarifa> getAll() {
        return tarifaRepository.findAll();
    }

    public Tarifa crearOActualizar(ActualizarTarifaDTO dto) {
        Tarifa tarifa = new Tarifa(dto.clave(), dto.valor(), dto.descripcion());
        return tarifaRepository.save(tarifa);
    }
}