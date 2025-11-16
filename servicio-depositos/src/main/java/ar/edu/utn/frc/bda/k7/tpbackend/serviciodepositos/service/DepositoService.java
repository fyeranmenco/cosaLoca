package ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.model.Deposito;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.model.dtos.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.repository.PersistenciaDeposito;

@Service
public class DepositoService {

    private final PersistenciaDeposito persistenciaDeposito;

    public DepositoService(PersistenciaDeposito persistenciaDeposito) {
        this.persistenciaDeposito = persistenciaDeposito;
    }

    public Deposito crearDeposito(CrearDepositoDTO depositoDTO) {
		Deposito nuevDeposito = new Deposito(depositoDTO);
        return persistenciaDeposito.save(nuevDeposito);
    }

    public Deposito obtenerDepositoPorId(Long id) {
        return persistenciaDeposito.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    public List<Deposito> obtenerTodosLosDepositos() {
        return persistenciaDeposito.findAll();
    }
}