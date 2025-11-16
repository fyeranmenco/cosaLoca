package ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.api.IClienteRestAPI;
import ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.model.Cliente;
import ar.edu.utn.frc.bda.k7.tpbackend.servicioclientes.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
public class ClienteController implements IClienteRestAPI {
    private final ClienteService clienteService;

    @Override
    @PostMapping
	@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Cliente> crearCliente(@RequestBody Cliente cliente) {
        Cliente nuevoCliente = clienteService.guardarCliente(cliente);
        return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<Cliente>> obtenerTodosLosClientes() {
		try {
			log.info("Se accedio al endpoint para obtener todos los clientes");
			List<Cliente> clientes = clienteService.obtenerTodos();
			return ResponseEntity.ok(clientes);
		} catch (Exception e) {
			log.error("Error al obtener la lista de clientes", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} 
    }


    @Override
    @GetMapping("/{dNI}")
    public ResponseEntity<Cliente> obtenerClientePorId(@PathVariable Long dNI) {
        return clienteService.obtenerClientePorId(dNI)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build()); 
    }
    
    @Override
    @GetMapping("/{dNI}/existe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> verificarExistenciaCliente(@PathVariable Long dNI) {
        if (clienteService.existeCliente(dNI)) {
            return ResponseEntity.noContent().build(); 
        } else {
            return ResponseEntity.notFound().build(); 
        }
    }
}