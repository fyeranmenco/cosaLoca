package ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.model.Camion;

@Repository
public interface PersistenciaCamion extends JpaRepository<Camion, Long> {
	List<Camion> findByKeycloakId(String keycloakId);
}