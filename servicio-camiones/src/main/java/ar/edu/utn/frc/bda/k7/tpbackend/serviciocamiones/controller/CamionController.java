package ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.api.ICamionRestAPI;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.model.Camion;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.service.CamionService;

@RestController
@RequestMapping("/api/camiones")
@RequiredArgsConstructor
public class CamionController implements ICamionRestAPI {

    private final CamionService camionService;

    @PostMapping
    public ResponseEntity<Camion> registrarCamion(@RequestBody Camion camion) {
        Camion nuevoCamion = camionService.guardarProducto(camion);
        return new ResponseEntity<>(nuevoCamion, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Camion> obtenerCamionPorId(@PathVariable Long id) {
        return camionService.obtenerCamionPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build()); 
    }
    
    @GetMapping
    public ResponseEntity<List<Camion>> obtenerTodosLosCamiones() {
        return ResponseEntity.ok(camionService.obtenerTodos());
    }

}