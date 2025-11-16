package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.repository;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Solicitud;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface PersistenciaSolicitud extends JpaRepository<Solicitud, Long> {
	List<Solicitud> findByEstado(String estado);
	List<Solicitud> findByContenedorEstadoIn(List<String> estados);
	@Query("SELECT s FROM Solicitud s JOIN s.contenedor c WHERE " +
           "(:estado IS NULL OR c.estado = :estado) AND " +
           "c.estado IN ('PENDIENTE_RETIRO', 'EN_TRANSITO', 'EN_DEPOSITO')")
    List<Solicitud> findContenedoresPendientesConFiltros(String estado);
}