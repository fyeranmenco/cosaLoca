package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.service;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration.CamionRestAPIClient;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration.ClienteRestAPIClient;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Solicitud;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Ruta;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Tramo;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.Contenedor;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.DTOs.CamionDTO;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.DTOs.DistanciaDuracionDTO;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.DTOs.SolicitudRequestDTO;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.repository.PersistenciaSolicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final PersistenciaSolicitud persistenciaSolicitud;
    private final ClienteRestAPIClient clienteClient;
    private final CamionRestAPIClient camionClient;
    // NOTA: Faltaría el DepositoRestAPIClient si se usan depósitos

    /**
     * RF 1: Registrar una nueva solicitud de transporte. (Cliente) [cite: 47]
     */
    public Solicitud crearSolicitud(SolicitudRequestDTO request, String token) {
        
        // RF 1.2: Validar que el cliente existe [cite: 49]
        if (!clienteClient.existeCliente(request.clienteDNI(), token)) {
             throw new RuntimeException("El Cliente con DNI " + request.clienteDNI() + " no existe.");
        }

        // RF 1.1: Creación del contenedor [cite: 48]
        Contenedor contenedor = new Contenedor();
        contenedor.setPeso(request.pesoContenedor());
        contenedor.setVolumen(request.volumenContenedor());
        contenedor.setClienteId(request.clienteDNI()); // Asocia al cliente
        contenedor.setEstado("PENDIENTE_RETIRO");

        Solicitud solicitud = new Solicitud();
        solicitud.setContenedor(contenedor);
        solicitud.setDireccionOrigen(request.direccionOrigen());
        solicitud.setLatitudOrigen(request.latitudOrigen());
        solicitud.setLongitudOrigen(request.longitudOrigen());
        solicitud.setDireccionDestino(request.direccionDestino());
        solicitud.setLatitudDestino(request.latitudDestino());
        solicitud.setLongitudDestino(request.longitudDestino());
        


		DistanciaDuracionDTO calculoRuta = new DistanciaDuracionDTO(10000L, 1800L); // Valores de ejemplo
        // DistanciaDuracionDTO calculoRuta = integracionGMClient.calcularDistanciaYDuracion(
        //     solicitud.getLatitudOrigen(), solicitud.getLongitudOrigen(),
        //     solicitud.getLatitudDestino(), solicitud.getLongitudDestino()
        // );
        
        CamionDTO[] camionesAptos = camionClient.obtenerCamionesDisponibles(
            contenedor.getPeso(), contenedor.getVolumen(), token
        );

        if (camionesAptos.length == 0) {
            throw new RuntimeException("No hay camiones disponibles para las características de este contenedor.");
        }
        
        // RF 112: Calcular tarifa aproximada en base a camiones elegibles
        Double costoPromedioPorKm = calcularCostoPromedio(camionesAptos);
        
        solicitud.setCostoEstimado(calculoRuta.distanciaMetros() / 1000.0 * costoPromedioPorKm);
        solicitud.setTiempoEstimado(calculoRuta.duracionSegundos() / 3600.0); // En horas
        solicitud.setEstado("BORRADOR"); // RF 1.3 [cite: 51]
        
        return persistenciaSolicitud.save(solicitud);
    }
    
    /**
     * RF 2: Consultar el estado del transporte. (Cliente) [cite: 52]
     */
    public String consultarEstadoSolicitud(Long id) {
        Solicitud solicitud = findSolicitudById(id);
        // Lógica de seguimiento [cite: 28, 114]
        return "Solicitud: " + solicitud.getEstado() + ". Contenedor: " + solicitud.getContenedor().getEstado();
    }
    
    /**
     * RF 4: Asignar una ruta a la solicitud. (Admin) [cite: 54]
     * (Asumimos que la ruta ya viene calculada, en una implementación real
     * este método calcularía los tramos usando los depósitos)
     */
    public Solicitud asignarRuta(Long id, Ruta ruta) {
        Solicitud solicitud = findSolicitudById(id);
        
        // Lógica de validación de la ruta...
        
        solicitud.setRuta(ruta);
        solicitud.setEstado("PROGRAMADA"); // RF 1.3 [cite: 51]
        return persistenciaSolicitud.save(solicitud);
    }

    /**
     * RF 6: Asignar camión a un tramo. (Admin) [cite: 57]
     */
    public Tramo asignarCamionATramo(Long solicitudId, Long tramoId, Long camionId) {
        Solicitud solicitud = findSolicitudById(solicitudId);
        Tramo tramo = solicitud.getRuta().getTramos().stream()
            .filter(t -> t.getId().equals(tramoId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Tramo no encontrado"));
            
        // Validar que el camión sea apto (RF 11)
        // (Se omite por brevedad, se debería llamar a camionClient.obtenerCamionPorId(camionId) 
        // y comparar con solicitud.getContenedor().getPeso/Volumen)
            
        tramo.setCamionId(camionId);
        tramo.setEstado("ASIGNADO");
        
        // Marcar camión como no disponible
        camionClient.actualizarDisponibilidad(camionId, false);
        
        persistenciaSolicitud.save(solicitud);
        return tramo;
    }

    /**
     * RF 7: Determinar inicio o fin de tramo. (Transportista) [cite: 58]
     */
    public Tramo actualizarEstadoTramo(Long solicitudId, Long tramoId, String estado, String transportistaKeycloakId) {
        Solicitud solicitud = findSolicitudById(solicitudId);
        Tramo tramo = solicitud.getRuta().getTramos().stream()
            .filter(t -> t.getId().equals(tramoId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Tramo no encontrado"));
        
        // Aquí iría la lógica de negocio para validar que el 'transportistaKeycloakId'
        // corresponde al 'camionId' asignado al tramo.

        switch (estado.toUpperCase()) {
            case "INICIAR":
                tramo.setEstado("INICIADO");
                tramo.setFechaHoraInicioReal(LocalDateTime.now());
                solicitud.setEstado("EN_TRANSITO");
                solicitud.getContenedor().setEstado("EN_TRANSITO");
                break;
            case "FINALIZAR":
                tramo.setEstado("FINALIZADO");
                tramo.setFechaHoraFinReal(LocalDateTime.now());
                
                // Marcar camión como disponible
                camionClient.actualizarDisponibilidad(tramo.getCamionId(), true);
                
                // Lógica para ver si es el último tramo
                boolean esUltimoTramo = esUltimoTramo(solicitud.getRuta(), tramo);
                if (esUltimoTramo) {
                    solicitud.setEstado("ENTREGADA");
                    solicitud.getContenedor().setEstado("ENTREGADO");
                    // RF 9: Calcular costos y tiempos reales [cite: 63]
                    calcularCostosYTiemposReales(solicitud);
                } else {
                    solicitud.getContenedor().setEstado("EN_DEPOSITO");
                }
                break;
            default:
                throw new IllegalArgumentException("Estado no válido");
        }
        
        persistenciaSolicitud.save(solicitud);
        return tramo;
    }

    // --- Métodos de Ayuda ---

    private Solicitud findSolicitudById(Long id) {
        return persistenciaSolicitud.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada con ID: " + id));
    }

    private Double calcularCostoPromedio(CamionDTO[] camionesAptos) {
        // Lógica de cálculo de tarifa [cite: 112]
        return 150.0; // Valor de ejemplo
    }

    private boolean esUltimoTramo(Ruta ruta, Tramo tramoActual) {
        // Lógica de ejemplo
        return ruta.getTramos().get(ruta.getTramos().size() - 1).getId().equals(tramoActual.getId());
    }

    private void calcularCostosYTiemposReales(Solicitud solicitud) {
        // RF 8: Lógica de cálculo de costos reales (recorrido, estadía, combustible) [cite: 59-63, 110]
        solicitud.setCostoFinal(1000.0); // Valor de ejemplo
        solicitud.setTiempoReal(10.0); // Valor de ejemplo
    }
}