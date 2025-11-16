package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.controller;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Ruta;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Solicitud;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Tramo;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.SolicitudRequestDTO;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.TramoSugeridoDTO;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Slf4j
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<Solicitud> crearSolicitud(@RequestBody SolicitudRequestDTO request, @AuthenticationPrincipal Jwt principal) {
        String token = principal.getTokenValue();
        // El DTO ya no tiene DNI, el servicio lo obtiene del token
        return ResponseEntity.ok(solicitudService.crearSolicitud(request, token)); 
    }

	@GetMapping("/tramos/sugeridos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TramoSugeridoDTO>> obtenerTramosSugeridos(
            @RequestParam Long idDepositoOrigen,
            @RequestParam Long idDepositoDestino,
            @AuthenticationPrincipal Jwt principal) {
        
        String token = principal.getTokenValue();
        List<TramoSugeridoDTO> sugerencias = solicitudService.obtenerTramosSugeridos(idDepositoOrigen, idDepositoDestino, token);
        return ResponseEntity.ok(sugerencias);
    }

	@GetMapping("/borradores")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Solicitud>> getSolicitudesBorrador() {
        return ResponseEntity.ok(solicitudService.obtenerSolicitudesBorrador());
    }

    @GetMapping("/{id}/estado")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<String> consultarEstado(@PathVariable Long id, @AuthenticationPrincipal Jwt principal) {
        // TODO: Validar que el 'principal' (Cliente) sea el dueño de la solicitud 'id'
        return ResponseEntity.ok(solicitudService.consultarEstadoSolicitud(id));
    }

    @PostMapping("/{id}/ruta")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Solicitud> asignarRuta(@PathVariable Long id, @RequestBody Ruta ruta) {
        return ResponseEntity.ok(solicitudService.asignarRuta(id, ruta));
    }
    
    @PatchMapping("/{solicitudId}/tramos/{tramoId}/asignar-camion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tramo> asignarCamion(
            @PathVariable Long solicitudId, 
            @PathVariable Long tramoId, 
            @RequestBody Long camionId,
			@AuthenticationPrincipal Jwt principal) {
        return ResponseEntity.ok(solicitudService.asignarCamionATramo(solicitudId, tramoId, camionId, principal.getTokenValue()));
    }

    @PatchMapping("/{solicitudId}/tramos/{tramoId}/estado")
    @PreAuthorize("hasRole('TRANSPORTISTA') or hasRole('ADMIN')")
    public ResponseEntity<Tramo> actualizarEstadoTramo(
            @PathVariable Long solicitudId, 
            @PathVariable Long tramoId, 
            @RequestBody String estado, // "INICIAR" o "FINALIZAR"
            @AuthenticationPrincipal Jwt principal) {
        String transportistaKeycloakId = principal.getClaimAsString("sub");
		try {
        return ResponseEntity.ok(solicitudService.actualizarEstadoTramo(solicitudId, tramoId, estado, transportistaKeycloakId, principal.getTokenValue()));
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
    }

	@GetMapping("/{id}/costo_estimado")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<Double> getCostoEstimado(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(solicitudService.getCostoEstimado(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{id}/costo_real")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<Double> getCostoReal(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(solicitudService.getCostoReal(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // O un DTO de error
        }
    }

	@GetMapping("/contenedores/pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Solicitud>> getContenedoresPendientes(
            @RequestParam(required = false) String estado) { // <-- Filtro añadido
        return ResponseEntity.ok(solicitudService.getContenedoresPendientes(estado));
    }

    @GetMapping("/{id}/tiempo_estimado") // 'image.png' dice /tiempo, pero estimad/real es más claro
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<Double> getTiempoEstimado(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(solicitudService.getTiempoEstimado(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{id}/tiempo_real")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<Double> getTiempoReal(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(solicitudService.getTiempoReal(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); 
        }
    }

	@GetMapping("/tramos/mis-asignaciones")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<List<Tramo>> getMisTramosAsignados(@AuthenticationPrincipal Jwt principal) {
        String token = principal.getTokenValue();
        return ResponseEntity.ok(solicitudService.obtenerMisTramosAsignados(token));
    }
}