package ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.controller;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.model.Deposito;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.service.DepositoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depositos")
@RequiredArgsConstructor
public class DepositoController {

    private final DepositoService depositoService;

    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Deposito> crearDeposito(@RequestBody Deposito deposito) {
        Deposito nuevoDeposito = depositoService.crearDeposito(deposito);
        return new ResponseEntity<>(nuevoDeposito, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Deposito> obtenerDeposito(@PathVariable Long id) {
        Deposito deposito = depositoService.obtenerDepositoPorId(id);
        return ResponseEntity.ok(deposito);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Deposito>> obtenerTodosLosDepositos() {
        List<Deposito> depositos = depositoService.obtenerTodosLosDepositos();
        return ResponseEntity.ok(depositos);
    }
}