package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.controller;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Ruta;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Solicitud;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Tramo;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.DTOs.SolicitudRequestDTO;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    /**
     * RF 1: Registrar una nueva solicitud. (Rol: CLIENTE) [cite: 47]
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<Solicitud> crearSolicitud(@RequestBody SolicitudRequestDTO request, @AuthenticationPrincipal Jwt principal) {
        // Obtenemos el ID de Keycloak del Cliente
        String clienteKeycloakId = principal.getClaimAsString("sub");
        return ResponseEntity.ok(solicitudService.crearSolicitud(request, clienteKeycloakId));
    }

    /**
     * RF 2: Consultar el estado del transporte. (Rol: CLIENTE) [cite: 52]
     */
    @GetMapping("/{id}/estado")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<String> consultarEstado(@PathVariable Long id, @AuthenticationPrincipal Jwt principal) {
        // TODO: Validar que el 'principal' (Cliente) sea el dueño de la solicitud 'id'
        return ResponseEntity.ok(solicitudService.consultarEstadoSolicitud(id));
    }
    
    /**
     * RF 4: Asignar una ruta a la solicitud. (Rol: OPERADOR_ADMINISTRADOR) [cite: 54]
     */
    @PostMapping("/{id}/ruta")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Solicitud> asignarRuta(@PathVariable Long id, @RequestBody Ruta ruta) {
        return ResponseEntity.ok(solicitudService.asignarRuta(id, ruta));
    }
    
    /**
     * RF 6: Asignar camión a un tramo. (Rol: OPERADOR_ADMINISTRADOR) [cite: 57]
     */
    @PatchMapping("/{solicitudId}/tramos/{tramoId}/asignar-camion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tramo> asignarCamion(
            @PathVariable Long solicitudId, 
            @PathVariable Long tramoId, 
            @RequestBody Long camionId) {
        return ResponseEntity.ok(solicitudService.asignarCamionATramo(solicitudId, tramoId, camionId));
    }

    /**
     * RF 7: Iniciar/Finalizar tramo. (Rol: TRANSPORTISTA) [cite: 58]
     */
    @PatchMapping("/{solicitudId}/tramos/{tramoId}/estado")
    @PreAuthorize("hasRole('TRANSPORTISTA') or hasRole('ADMIN')")
    public ResponseEntity<Tramo> actualizarEstadoTramo(
            @PathVariable Long solicitudId, 
            @PathVariable Long tramoId, 
            @RequestBody String estado, // "INICIAR" o "FINALIZAR"
            @AuthenticationPrincipal Jwt principal) {
        String transportistaKeycloakId = principal.getClaimAsString("sub");
        return ResponseEntity.ok(solicitudService.actualizarEstadoTramo(solicitudId, tramoId, estado, transportistaKeycloakId));
    }
}