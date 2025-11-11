package ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.api.IDepositoRestAPI;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.model.Deposito;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciodepositos.service.DepositoService;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/depositos")
@RequiredArgsConstructor
public class DepositoController implements IDepositoRestAPI {

    private final DepositoService depositoService;

    @Override
    @PostMapping
    public ResponseEntity<Deposito> crearDeposito(@RequestBody Deposito deposito) {
        Deposito nuevoDeposito = depositoService.crearDeposito(deposito);
        return new ResponseEntity<>(nuevoDeposito, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Deposito> obtenerDeposito(@PathVariable Long id) {
        Deposito deposito = depositoService.obtenerDepositoPorId(id);
        return ResponseEntity.ok(deposito);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<Deposito>> obtenerTodosLosDepositos() {
        java.util.List<Deposito> depositos = depositoService.obtenerTodosLosDepositos();
        return ResponseEntity.ok(depositos);
    }
}