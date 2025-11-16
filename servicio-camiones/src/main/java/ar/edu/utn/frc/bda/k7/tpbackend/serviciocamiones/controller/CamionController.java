package ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.controller;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.api.ICamionRestAPI;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.model.Camion;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciocamiones.service.CamionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/camiones")
@RequiredArgsConstructor
@Slf4j
public class CamionController implements ICamionRestAPI {

    private final CamionService camionService;

    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Camion> registrarCamion(@RequestBody Camion camion) {
        Camion nuevoCamion = camionService.guardarCamion(camion);
        return new ResponseEntity<>(nuevoCamion, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Camion> obtenerCamionPorId(@PathVariable Long id) {
		log.info("Buscando camion con id: {}", id);
        return camionService.obtenerCamionPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build()); 
    }

   
    @GetMapping("/disponibles/aptos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Camion>> obtenerCamionesDisponiblesAptos(
            @RequestParam Double peso, 
            @RequestParam Double volumen) {
        return ResponseEntity.ok(camionService.obtenerCamionesDisponiblesAptos(peso, volumen));
    }
    
    
    @PutMapping("/{id}/disponibilidad")
    @PreAuthorize("isAuthenticated()") // Solo accesible internamente o por un admin
    public ResponseEntity<Camion> actualizarDisponibilidad(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(camionService.actualizarDisponibilidad(id, body.get("disponible")));
    }
}