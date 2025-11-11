package ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.model.Deposito;
@Repository
public interface PersistenciaDeposito extends JpaRepository<Deposito, Long> {
}