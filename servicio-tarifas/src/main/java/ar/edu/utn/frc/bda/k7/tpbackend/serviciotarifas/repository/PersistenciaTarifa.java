package ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.repository;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.Tarifa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PersistenciaTarifa extends JpaRepository<Tarifa, Long> {
}