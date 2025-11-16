package ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.model.Cliente;
import ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.repository.PersistenciaCliente;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor 
@Slf4j
public class ClienteService {
    private final PersistenciaCliente persistenciaCliente;

    public Cliente guardarCliente(Cliente cliente) {
        return persistenciaCliente.save(cliente);
    }

    public Optional<Cliente> obtenerClientePorId(Long dNI) {
        return persistenciaCliente.findById(dNI);
    }
    
    public List<Cliente> obtenerTodos() {
		log.info("ClientesService: Obteniendo todos los clientes desde la base de datos");
        return persistenciaCliente.findAll();
    }
    
    public boolean existeCliente(Long dNI) {
        return persistenciaCliente.existsById(dNI);
    }
}