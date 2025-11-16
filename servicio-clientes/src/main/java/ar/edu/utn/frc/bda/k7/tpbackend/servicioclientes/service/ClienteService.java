package ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.model.Cliente;
import ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.repository.PersistenciaCliente;

import java.util.List;
import java.util.NoSuchElementException;
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

	public Cliente registrarMiPerfil(Cliente cliente, String keycloakId) {
        if (persistenciaCliente.existsByKeycloakId(keycloakId)) {
            throw new IllegalStateException("El usuario ya tiene un perfil de cliente registrado.");
        }
        if (persistenciaCliente.existsById(cliente.getDNI())) {
            throw new IllegalStateException("El DNI ya está registrado por otro cliente.");
        }
        
        cliente.setIdUsuarioKeyCloak(keycloakId);
        return persistenciaCliente.save(cliente);
    }
    
    // --- NUEVO MÉTODO (para Clientes) ---
    public Cliente obtenerMiPerfil(String keycloakId) {
        return persistenciaCliente.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new NoSuchElementException("No se encontró un perfil de cliente para el usuario."));
    }
    
    // --- NUEVO MÉTODO (para Servicios Internos) ---
    public Cliente obtenerClientePorKeycloakId(String keycloakId) {
        return persistenciaCliente.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado por Keycloak ID"));
    }

    // --- NUEVO MÉTODO (para Servicios Internos) ---
    public boolean existeClientePorKeycloakId(String keycloakId) {
        return persistenciaCliente.existsByKeycloakId(keycloakId);
    }
}