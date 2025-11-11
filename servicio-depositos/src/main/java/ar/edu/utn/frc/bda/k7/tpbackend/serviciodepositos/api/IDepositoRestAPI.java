package ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.api;

import org.springframework.http.ResponseEntity;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.model.Deposito;

public interface IDepositoRestAPI {

    ResponseEntity<Deposito> crearDeposito(Deposito deposito);

    ResponseEntity<Deposito> obtenerDeposito(Long id);
    
    ResponseEntity<java.util.List<Deposito>> obtenerTodosLosDepositos();
	
}
