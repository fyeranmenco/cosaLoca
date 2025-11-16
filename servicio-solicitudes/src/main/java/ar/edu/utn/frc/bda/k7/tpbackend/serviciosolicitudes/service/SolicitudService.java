package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.service;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.osrm.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.repository.PersistenciaSolicitud;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final PersistenciaSolicitud persistenciaSolicitud;
    private final ClienteRestAPIClient clienteClient;
    private final CamionRestAPIClient camionClient;
	private final DepositoRestAPIClient depositoClient;
    private final OsrmRestAPIClient osrmClient;

    private static final double MAX_DURACION_SEGUNDOS = 8 * 60 * 60; // 8 horas'
	private static final Double PRECIO_LITRO_COMBUSTIBLE = 1000.0; // Valor de ejemplo
    private static final Double CARGO_GESTION_TRAMO_SIMPLE = 50000.0; 
    private static final Double COSTO_ESTADIA_DEPOSITO_DIA = 20000.0; 


    public Solicitud crearSolicitud(SolicitudRequestDTO request, String token) {
        
        if (!clienteClient.existeCliente(request.clienteDNI(), token)) {
             throw new RuntimeException("El Cliente con DNI " + request.clienteDNI() + " no existe.");
        }

        Contenedor contenedor = new Contenedor();
        contenedor.setPeso(request.pesoContenedor());
        contenedor.setVolumen(request.volumenContenedor());
        contenedor.setClienteId(request.clienteDNI()); 
        contenedor.setEstado("PENDIENTE_RETIRO");

        Solicitud solicitud = new Solicitud();
        solicitud.setContenedor(contenedor);
        solicitud.setDireccionOrigen(request.direccionOrigen());
        solicitud.setLatitudOrigen(request.latitudOrigen());
        solicitud.setLongitudOrigen(request.longitudOrigen());
        solicitud.setDireccionDestino(request.direccionDestino());
        solicitud.setLatitudDestino(request.latitudDestino());
        solicitud.setLongitudDestino(request.longitudDestino());
        
		
		String coordsOrigen = String.format("%s,%s", request.longitudOrigen(), request.latitudOrigen());
        String coordsDestino = String.format("%s,%s", request.longitudDestino(), request.latitudDestino());
        
        OsrmResponse osrmResponse = osrmClient.obtenerRutasAlternativas(coordsOrigen, coordsDestino).block();
        if (osrmResponse == null || osrmResponse.getRoutes() == null || osrmResponse.getRoutes().isEmpty()) {
            throw new RuntimeException("No se pudo calcular una ruta entre el origen y el destino.");
        }
        
        // Tomamos la primera ruta (la más rápida) como referencia
        OsrmRoute rutaDirecta = osrmResponse.getRoutes().get(0);

		// DistanciaDuracionDTO calculoRuta = new DistanciaDuracionDTO(10000L, 1800L); // Valores de ejemplo
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
        solicitud.setCostoEstimado(
            calcularCostoEstimado(camionesAptos, rutaDirecta.getDistance())
        );
        solicitud.setTiempoEstimado(rutaDirecta.getDuration() / 3600.0); // En horas
        solicitud.setEstado("BORRADOR");
        
        return persistenciaSolicitud.save(solicitud);
    }

	private Double calcularCostoEstimado(CamionDTO[] camionesAptos, Double distanciaMetros) {
        if (camionesAptos.length == 0) return 0.0;

        // "valores promedio entre los camiones elegibles" [cite: 114]
        double avgCostoKm = Arrays.stream(camionesAptos)
            .mapToDouble(CamionDTO::costoTrasladoPorKm)
            .average().orElse(0.0);
        
        double avgConsumoKm = Arrays.stream(camionesAptos)
            .mapToDouble(CamionDTO::consumoCombustibleKm)
            .average().orElse(0.0);

        double distanciaKm = distanciaMetros / 1000.0;
        
        // "Cargos de Gestión... + costo por kilómetro... + costo de combustible..." [cite: 112]
        // (Asumimos que el estimado no tiene estadía en depósito)
        double costoGestion = CARGO_GESTION_TRAMO_SIMPLE;
        double costoTraslado = distanciaKm * avgCostoKm;
        double costoCombustible = (distanciaKm * avgConsumoKm) * PRECIO_LITRO_COMBUSTIBLE;
        
        return costoGestion + costoTraslado + costoCombustible;
    }

	public List<TramoSugeridoDTO> obtenerTramosSugeridos(Long idDepositoOrigen, Long idDepositoDestino, String token) {
        
        // 1. Obtener Depósitos (llamada asíncrona a otro microservicio)
        Mono<DepositoDTO> monoOrigen = depositoClient.obtenerDepositoPorId(idDepositoOrigen, token);
        Mono<DepositoDTO> monoDestino = depositoClient.obtenerDepositoPorId(idDepositoDestino, token);

        // 2. Combinar las respuestas
        OsrmResponse osrmResponse = Mono.zip(monoOrigen, monoDestino)
            .flatMap(tupla -> {
                DepositoDTO origen = tupla.getT1();
                DepositoDTO destino = tupla.getT2();

                // Formato OSRM: lon,lat
                String coordsOrigen = String.format("%s,%s", origen.getLongitud(), origen.getLatitud());
                String coordsDestino = String.format("%s,%s", destino.getLongitud(), destino.getLatitud());
                
                // 3. Llamar a OSRM
                return osrmClient.obtenerRutasAlternativas(coordsOrigen, coordsDestino);
            })
            .block(); // .block() para simplificar (en un entorno real, manejarías reactivamente)

        if (osrmResponse == null || osrmResponse.getRoutes() == null || osrmResponse.getRoutes().isEmpty()) {
            return Collections.emptyList();
        }

        List<OsrmRoute> todasLasRutas = osrmResponse.getRoutes();

        // 4. Aplicar REGLA DE 8 HORAS
        List<OsrmRoute> rutasValidas = todasLasRutas.stream()
            .filter(ruta -> ruta.getDuration() <= MAX_DURACION_SEGUNDOS)
            .sorted(Comparator.comparingDouble(OsrmRoute::getDuration))
            .collect(Collectors.toList());

        // 5. Decidir qué devolver
        if (!rutasValidas.isEmpty()) {
            // Caso A: Hay rutas < 8h. Devolver las 3 más rápidas.
            return rutasValidas.stream()
                .limit(3) // "mostrar 2 o 3"
                .map(this::convertirARutaSugerida)
                .collect(Collectors.toList());
        } else {
            // Caso B: NO hay rutas < 8h. Devolver la "menos mala".
            return todasLasRutas.stream()
                .min(Comparator.comparingDouble(OsrmRoute::getDuration))
                .map(this::convertirARutaSugerida)
                .map(List::of) // Lo convierte en una lista de 1 elemento
                .orElse(Collections.emptyList());
        }
    }

	private TramoSugeridoDTO convertirARutaSugerida(OsrmRoute ruta) {
        long duracion = (long) ruta.getDuration();
        long horas = duracion / 3600;
        long minutos = (duracion % 3600) / 60;
        
        return new TramoSugeridoDTO(
            String.format("%dh %02dm", horas, minutos),
            ruta.getDistance() / 1000.0,
            ruta.getGeometry(),
            duracion,
            ruta.getDistance()
        );
    }
    
    /**
     * RF 2: Consultar el estado del transporte. (Cliente) [cite: 52]
     */
    public String consultarEstadoSolicitud(Long id) {
        Solicitud solicitud = findSolicitudById(id);
        // Lógica de seguimiento [cite: 28, 114]
        return "Solicitud: " + solicitud.getEstado() + ". Contenedor: " + solicitud.getContenedor().getEstado();
    }

	public List<Solicitud> obtenerSolicitudesBorrador() {
        return persistenciaSolicitud.findByEstado("BORRADOR");
    }
    
    /**
     * RF 4: Asignar una ruta a la solicitud. (Admin) [cite: 54]
     * (Asumimos que la ruta ya viene calculada, en una implementación real
     * este método calcularía los tramos usando los depósitos)
     */
    public Solicitud asignarRuta(Long id, Ruta ruta) {
        Solicitud solicitud = findSolicitudById(id);

        // --- VALIDACIÓN MEJORADA ---
        if (!"BORRADOR".equalsIgnoreCase(solicitud.getEstado())) {
            throw new IllegalStateException("Solo se puede asignar una ruta a solicitudes en estado 'BORRADOR'.");
        }
        
        if (ruta == null || ruta.getTramos() == null || ruta.getTramos().isEmpty()) {
            throw new IllegalArgumentException("La ruta asignada no puede ser nula ni contener tramos vacíos.");
        }
        // --- FIN VALIDACIÓN ---

        solicitud.setRuta(ruta);
        solicitud.setEstado("PROGRAMADA"); // La solicitud pasa a estar programada
        return persistenciaSolicitud.save(solicitud);
    }

    /**
     * RF 6: Asignar camión a un tramo. (Admin) [cite: 57]
     */
    public Tramo asignarCamionATramo(Long solicitudId, Long tramoId, Long camionId, String token) {
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
        camionClient.actualizarDisponibilidad(camionId, false, token);
        
        persistenciaSolicitud.save(solicitud);
        return tramo;
    }

    /**
     * RF 7: Determinar inicio o fin de tramo. (Transportista) [cite: 58]
     */
    public Tramo actualizarEstadoTramo(Long solicitudId, Long tramoId, String estado, String transportistaKeycloakId, String token) {
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
                camionClient.actualizarDisponibilidad(tramo.getCamionId(), true, token);
                
                // Lógica para ver si es el último tramo
                boolean esUltimoTramo = esUltimoTramo(solicitud.getRuta(), tramo);
                if (esUltimoTramo) {
                    solicitud.setEstado("ENTREGADA");
                    solicitud.getContenedor().setEstado("ENTREGADO");
                    // RF 9: Calcular costos y tiempos reales [cite: 63]
                    calcularCostosYTiemposReales(solicitud, token);
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

    // private Double calcularCostoPromedio(CamionDTO[] camionesAptos) {
    //     // Lógica de cálculo de tarifa [cite: 112]
    //     return 150.0; // Valor de ejemplo
    // }

    private boolean esUltimoTramo(Ruta ruta, Tramo tramoActual) {
        // Lógica de ejemplo
        return ruta.getTramos().get(ruta.getTramos().size() - 1).getId().equals(tramoActual.getId());
    }

    private void calcularCostosYTiemposReales(Solicitud solicitud, String token) {
        
        double costoTotal = 0.0;
        long tiempoTotalSegundos = 0;

        // "La tarifa final del envío se calcula como..." [cite: 111]
        // "Cargos de Gestión... + costo por kilómetro... + costo de combustible... + costo por estadía" [cite: 112]
        
        LocalDateTime fechaLlegadaAnterior = null;

        for (Tramo tramo : solicitud.getRuta().getTramos()) {
            if (tramo.getEstado().equals("FINALIZADO")) {
                CamionDTO camion = camionClient.obtenerCamionPorId(tramo.getCamionId(), token);
                double distanciaKm = tramo.getDistanciaMetros() / 1000.0;
                
                // 2. Sumar costos del tramo
                costoTotal += CARGO_GESTION_TRAMO_SIMPLE; // Cargo de gestión por tramo [cite: 112]
                costoTotal += distanciaKm * camion.costoTrasladoPorKm(); // Costo por km [cite: 112]
                costoTotal += (distanciaKm * camion.consumoCombustibleKm()) * PRECIO_LITRO_COMBUSTIBLE; 
                
                if (fechaLlegadaAnterior != null) {
                    long diasEstadia = Duration.between(fechaLlegadaAnterior, tramo.getFechaHoraInicioReal()).toDays();
                    if (diasEstadia > 0) {
                        costoTotal += diasEstadia * COSTO_ESTADIA_DEPOSITO_DIA;
                    }
                }
                
                // 4. Acumular tiempo real
                tiempoTotalSegundos += Duration.between(tramo.getFechaHoraInicioReal(), tramo.getFechaHoraFinReal()).toSeconds();
                fechaLlegadaAnterior = tramo.getFechaHoraFinReal();
            }
        }
        
        solicitud.setCostoFinal(costoTotal);
        solicitud.setTiempoReal(tiempoTotalSegundos / 3600.0); // En horas
    }

	public Double getCostoEstimado(Long id) {
        return findSolicitudById(id).getCostoEstimado();
    }
    
    public Double getCostoReal(Long id) {
        Solicitud s = findSolicitudById(id);
        if (!"ENTREGADA".equals(s.getEstado())) {
            throw new IllegalStateException("El costo real solo está disponible para solicitudes 'ENTREGADAS'.");
        }
        return s.getCostoFinal();
    }
    
    public Double getTiempoEstimado(Long id) {
        return findSolicitudById(id).getTiempoEstimado();
    }
    
    public Double getTiempoReal(Long id) {
        Solicitud s = findSolicitudById(id);
        if (!"ENTREGADA".equals(s.getEstado())) {
            throw new IllegalStateException("El tiempo real solo está disponible para solicitudes 'ENTREGADAS'.");
        }
        return s.getTiempoReal();
    }
}