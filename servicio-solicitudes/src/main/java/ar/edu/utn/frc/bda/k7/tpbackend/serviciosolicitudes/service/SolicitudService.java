package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.service;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.osrm.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.repository.PersistenciaSolicitud;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException; // <-- Importar
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final PersistenciaSolicitud persistenciaSolicitud;
    private final ClienteRestAPIClient clienteClient;
    private final CamionRestAPIClient camionClient;
	private final DepositoRestAPIClient depositoClient;
	private final TarifaRestAPIClient tarifaClient; // Inyectamos el CLIENTE
    private final OsrmRestAPIClient osrmClient;

	private static final long HORAS_ESPERA_EN_DEPOSITO = 8; // 8 horas de espera
	private static final long MAX_DURACION_SEGUNDOS = HORAS_ESPERA_EN_DEPOSITO * 3600; // 8 horas en segundos


    public Solicitud crearSolicitud(SolicitudRequestDTO request, String token) {
        
        if (!clienteClient.existeClientePorKeycloakId(token)) {
             throw new RuntimeException("Cliente no registrado. Por favor, complete su perfil antes de crear una solicitud.");
             // (Aquí se cumple RF 1.2: el sistema detecta que no existe)
             // (El usuario debería ser redirigido al frontend de "registrarme")
        }

        ClienteDTO cliente = clienteClient.getClientePorKeycloakId(token);

        // RF 1.1: Creación del contenedor
        Contenedor contenedor = new Contenedor();
        contenedor.setPeso(request.pesoContenedor());
        contenedor.setVolumen(request.volumenContenedor());
        contenedor.setClienteId(cliente.getDNI()); // <-- Usamos el DNI del perfil
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
        
        OsrmResponse osrmResponse = osrmClient.obtenerRutasAlternativas(coordsOrigen, coordsDestino);
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

		Map<String, Double> tarifaMap = getTarifaMap(token);
        
        // RF 112: Calcular tarifa aproximada en base a camiones elegibles
        solicitud.setCostoEstimado(
            calcularCostoEstimado(camionesAptos, rutaDirecta.getDistance(), tarifaMap)
        );
        solicitud.setTiempoEstimado(rutaDirecta.getDuration() / 3600.0); // En horas
        solicitud.setEstado("BORRADOR");
        
        return persistenciaSolicitud.save(solicitud);
    }

	private Double calcularCostoEstimado(CamionDTO[] camionesAptos, Double distanciaMetros, Map<String, Double> tarifaMap) {
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
        double costoGestion = tarifaMap.get("CARGO_GESTION_TRAMO_SIMPLE");
        double costoCombustible = (distanciaKm * avgConsumoKm) * tarifaMap.get("PRECIO_LITRO_COMBUSTIBLE");
        
        double costoTraslado = distanciaKm * avgCostoKm;
        
        return costoGestion + costoTraslado + costoCombustible;
    }

	public List<TramoSugeridoDTO> obtenerTramosSugeridos(Long idDepositoOrigen, Long idDepositoDestino, String token) {
        
    // 1. Obtener Depósitos (Ahora son llamadas sincrónicas/bloqueantes)
    DepositoDTO origen = depositoClient.obtenerDepositoPorId(idDepositoOrigen, token);
    DepositoDTO destino = depositoClient.obtenerDepositoPorId(idDepositoDestino, token);

    // 2. Formatear coordenadas
    // Formato OSRM: lon,lat
    String coordsOrigen = String.format("%s,%s", origen.getLongitud(), origen.getLatitud());
    String coordsDestino = String.format("%s,%s", destino.getLongitud(), destino.getLatitud());
    
    // 3. Llamar a OSRM (Ahora es una llamada sincrónica/bloqueante)
    OsrmResponse osrmResponse = osrmClient.obtenerRutasAlternativas(coordsOrigen, coordsDestino);

    // 4. Lógica de negocio (Esta parte no cambia, ya era sincrónica)
    if (osrmResponse == null || osrmResponse.getRoutes() == null || osrmResponse.getRoutes().isEmpty()) {
        return Collections.emptyList();
    }

    List<OsrmRoute> todasLasRutas = osrmResponse.getRoutes();

    // 5. Aplicar REGLA DE 8 HORAS
    List<OsrmRoute> rutasValidas = todasLasRutas.stream()
        .filter(ruta -> ruta.getDuration() <= MAX_DURACION_SEGUNDOS)
        .sorted(Comparator.comparingDouble(OsrmRoute::getDuration))
        .collect(Collectors.toList());

    // 6. Decidir qué devolver
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

		LocalDateTime proximaFechaInicio = LocalDateTime.now().plusHours(24);

        for (Tramo tramo : ruta.getTramos()) {
            if (tramo.getDuracionEstimadaSegundos() == null || tramo.getDuracionEstimadaSegundos() <= 0) {
                throw new IllegalArgumentException("El tramo (ID: " + tramo.getId() + ") no tiene una duración estimada válida.");
            }

            // 1. Seteamos la fecha de inicio
            tramo.setFechaHoraInicioEstimada(proximaFechaInicio);

            // 2. Calculamos y seteamos la fecha de fin
            LocalDateTime fechaFin = proximaFechaInicio.plusSeconds(tramo.getDuracionEstimadaSegundos());
            tramo.setFechaHoraFinEstimada(fechaFin);

            // 3. Preparamos la fecha de inicio del *siguiente* tramo
            // (Asumimos 8 horas de espera/gestión en el depósito)
            proximaFechaInicio = fechaFin.plusHours(HORAS_ESPERA_EN_DEPOSITO);
        }

        solicitud.setRuta(ruta);
        solicitud.setEstado("PROGRAMADA"); // La solicitud pasa a estar programada
        return persistenciaSolicitud.save(solicitud);
    }

    /**
     * RF 6: Asignar camión a un tramo. (Admin) [cite: 57]
     */
    public Tramo asignarCamionATramo(Long solicitudId, Long tramoId, Long camionId, String token) {
        Solicitud solicitud = findSolicitudById(solicitudId);
		Contenedor contenedor = solicitud.getContenedor(); // <-- Obtenemos el contenedor
        Tramo tramo = solicitud.getRuta().getTramos().stream()
            .filter(t -> t.getId().equals(tramoId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Tramo no encontrado"));
            
        // Validar que el camión sea apto [cite: 69, 112]
        CamionDTO camion = camionClient.obtenerCamionPorId(camionId, token);
        if (camion == null) {
            throw new NoSuchElementException("Camión no encontrado con ID: " + camionId);
        }

        // Asumimos que contenedor.getPeso() y camion.costoTrasladoPorKm() están en Tns
        // y que contenedor.getVolumen() y camion.volumenM3() están en m3
        if (camion.costoTrasladoPorKm() < contenedor.getPeso() || 
            camion.volumenM3() < contenedor.getVolumen()) {
            
            throw new IllegalArgumentException("Validación fallida: El camión (ID: " + camionId + ") no es apto para el peso/volumen del contenedor.");
        }
            
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
        
        List<CamionDTO> misCamiones = camionClient.obtenerMisCamiones(token);
        
        boolean esMiCamion = misCamiones.stream()
            .map(camion -> (Long) ((Number) camion.id()).longValue())
            .anyMatch(idCamion -> idCamion.equals(tramo.getCamionId()));

        if (!esMiCamion) {
            throw new AccessDeniedException("Acceso denegado: El tramo no está asignado a este transportista.");
        }

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
                    Map<String, Double> tarifaMap = getTarifaMap(token);
                    calcularCostosYTiemposReales(solicitud, token, tarifaMap);
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

    private void calcularCostosYTiemposReales(Solicitud solicitud, String token, Map<String, Double> tarifaMap) {
        
        double costoTotal = 0.0;
        long tiempoTotalSegundos = 0;

        // "La tarifa final del envío se calcula como..." [cite: 111]
        // "Cargos de Gestión... + costo por kilómetro... + costo de combustible... + costo por estadía" [cite: 112]
        
        LocalDateTime fechaLlegadaAnterior = null;

		final Double PRECIO_COMBUSTIBLE = tarifaMap.get("PRECIO_LITRO_COMBUSTIBLE");
        final Double CARGO_GESTION = tarifaMap.get("CARGO_GESTION_TRAMO_SIMPLE");
        final Double COSTO_ESTADIA = tarifaMap.get("COSTO_ESTADIA_DEPOSITO_DIA");

        for (Tramo tramo : solicitud.getRuta().getTramos()) {
            if (tramo.getEstado().equals("FINALIZADO")) {
                CamionDTO camion = camionClient.obtenerCamionPorId(tramo.getCamionId(), token);
                double distanciaKm = tramo.getDistanciaMetros() / 1000.0;
                
                // 2. Sumar costos del tramo
                costoTotal += CARGO_GESTION;
                costoTotal += distanciaKm * camion.costoTrasladoPorKm();
                costoTotal += (distanciaKm * camion.consumoCombustibleKm()) * PRECIO_COMBUSTIBLE;
                
                if (fechaLlegadaAnterior != null) {
                    long diasEstadia = Duration.between(fechaLlegadaAnterior, tramo.getFechaHoraInicioReal()).toDays();
                    if (diasEstadia > 0) {
                        costoTotal += diasEstadia * COSTO_ESTADIA;
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

	public List<Solicitud> getContenedoresPendientes() {
        List<String> estadosPendientes = List.of(
            "PENDIENTE_RETIRO", 
            "EN_TRANSITO", 
            "EN_DEPOSITO"
        );
        return persistenciaSolicitud.findByContenedorEstadoIn(estadosPendientes);
    }

	private Map<String, Double> getTarifaMap(String token) {
        List<TarifaDTO> tarifas = tarifaClient.getTarifas(token);
        
        // Convierte la lista de DTOs en un Mapa<Clave, Valor>
        return tarifas.stream()
                .collect(Collectors.toMap(TarifaDTO::getClave, TarifaDTO::getValor));
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

	public List<Solicitud> getContenedoresPendientes(String estado) {
        // Si el estado es nulo, la query traerá todos los pendientes.
        // Si se provee un estado (ej. "EN_TRANSITO"), filtrará por ese.
        return persistenciaSolicitud.findContenedoresPendientesConFiltros(estado);
    }

	public List<Tramo> obtenerMisTramosAsignados(String token) {
        
        // 1. Llamar a servicio-camiones para saber qué camiones maneja este transportista
        List<CamionDTO> misCamiones = camionClient.obtenerMisCamiones(token);
        
        if (misCamiones == null || misCamiones.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Extraer los IDs de esos camiones
        List<Long> misCamionIds = misCamiones.stream()
            .map(camion -> (Long) ((Number) camion.id()).longValue()) 
            .collect(Collectors.toList());

        // 3. Definir estados relevantes
        List<String> estadosRelevantes = List.of("ASIGNADO", "INICIADO");
        
        // 4. Buscar en TODAS las solicitudes y filtrar los tramos (SIN TramoRepository)
        return persistenciaSolicitud.findAll().stream() // Carga todas las solicitudes
            .filter(solicitud -> solicitud.getRuta() != null && solicitud.getRuta().getTramos() != null) // Filtra solicitudes con ruta y tramos
            .flatMap(solicitud -> solicitud.getRuta().getTramos().stream()) // Obtiene un stream de todos los tramos
            .filter(tramo -> misCamionIds.contains(tramo.getCamionId())) // Filtra por ID de camión
            .filter(tramo -> estadosRelevantes.contains(tramo.getEstado())) // Filtra por estado
            .collect(Collectors.toList());
	}
}