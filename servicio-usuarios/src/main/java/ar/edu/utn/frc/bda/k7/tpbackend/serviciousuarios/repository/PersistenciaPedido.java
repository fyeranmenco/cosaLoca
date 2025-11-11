package ar.edu.utn.frc.bda.k7.ejemploSpring.serviciopedidos.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frc.bda.k7.ejemploSpring.serviciopedidos.model.Pedido;

@Repository
public interface PersistenciaPedido extends JpaRepository<Pedido, Long> {
}