package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.api.ISolicitudRestAPI;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Solicitud;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.service.SolicitudService;


@RestController
@RequestMapping("/api/v1/solicitud")
public class SolicitudController implements ISolicitudRestAPI {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudservice) {
        this.solicitudService = solicitudservice;
    }

    @PostMapping
    public ResponseEntity<Solicitud> registrarSolicitud(@RequestBody Solicitud pedido) {
        Solicitud nuevoPedido = solicitudService.crearPedido(pedido);
        return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> consultarSolicitud(@PathVariable Long id) {
        Solicitud pedido = solicitudService.obtenerPedidoPorId(id);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping
    public ResponseEntity<java.util.List<Solicitud>> obtenerTodosLossolicitud() {
        java.util.List<Solicitud> solicitud = solicitudService.obtenerTodosLosPedidos();
        return ResponseEntity.ok(solicitud);
    }
}