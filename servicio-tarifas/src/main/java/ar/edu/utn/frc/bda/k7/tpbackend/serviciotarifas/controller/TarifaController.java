package ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.controller;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.Tarifa;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.model.dtos.ActualizarTarifaDTO;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciotarifas.service.TarifaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tarifas")
@RequiredArgsConstructor
public class TarifaController {

    private final TarifaService tarifaService;

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Abierto a todos los servicios autenticados
    public ResponseEntity<List<Tarifa>> getAllTarifas() {
        return ResponseEntity.ok(tarifaService.getAll());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tarifa> crearOActualizarTarifa(@RequestBody ActualizarTarifaDTO dto) {
        return ResponseEntity.ok(tarifaService.crearOActualizar(dto));
    }
}