package ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.api;

import java.util.List;

import org.springframework.http.ResponseEntity;

import ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.model.Cliente;

public interface IClienteRestAPI {
	ResponseEntity<Cliente> crearCliente(Cliente cliente);
	
	ResponseEntity<List<Cliente>> obtenerTodosLosClientes();
	
	ResponseEntity<Cliente> obtenerClientePorId(Long dNI);
	
	ResponseEntity<Void> verificarExistenciaCliente(Long dNI);
}
