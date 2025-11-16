package ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.model.Cliente;

@Repository
public interface PersistenciaCliente extends JpaRepository<Cliente, Long> {
	Optional<Cliente> findByKeycloakId(String keycloakId);
    
    boolean existsByKeycloakId(String keycloakId);
}
