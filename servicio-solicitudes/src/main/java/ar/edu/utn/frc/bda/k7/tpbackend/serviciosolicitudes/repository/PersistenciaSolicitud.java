package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.repository;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Solicitud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PersistenciaSolicitud extends JpaRepository<Solicitud, Long> {
}