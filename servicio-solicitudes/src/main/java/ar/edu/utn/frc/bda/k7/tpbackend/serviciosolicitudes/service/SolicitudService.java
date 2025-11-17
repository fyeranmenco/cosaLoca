package ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.service;

import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.integration.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.model.dtos.osrm.*;
import ar.edu.utn.frc.bda.k7.tpbackend.serviciosolicitudes.repository.PersistenciaSolicitud;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException; 
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
@Slf4j
public class SolicitudService {

    private final PersistenciaSolicitud persistenciaSolicitud;
    private final ClienteRestAPIClient clienteClient;
    private final CamionRestAPIClient camionClient;
	private final DepositoRestAPIClient depositoClient;
	private final TarifaRestAPIClient tarifaClient;  
    private final OsrmRestAPIClient osrmClient;

	private static final long HORAS_ESPERA_EN_DEPOSITO = 8;  
	private static final long MAX_DURACION_SEGUNDOS = HORAS_ESPERA_EN_DEPOSITO * 3600;  

	@Value("${service.central.latitud}")
	private String CENTRAL_LATITUD;

	@Value("${service.central.longitud}")
	private String CENTRAL_LONGITUD;

	public List<Solicitud> obtenerTodasLasSolicitudes() {
		return persistenciaSolicitud.findAll();
	}

    public Solicitud crearSolicitud(SolicitudRequestDTO request, Jwt jwt) {
        
		log.info("Creando solicitud ");

		log.info("intentando obtener cliente por Keycloak ID: ");
		ClienteDTO cliente = clienteClient.getClientePorKeycloakId(
			jwt.getTokenValue()
		);

		if (cliente == null) {
			log.error("No se encontró un cliente asociado al usuario autenticado.");
			throw new NoSuchElementException("No se encontró un cliente asociado al usuario autenticado.");
		}

		log.info("Cliente encontrado: " + cliente.getNombre() + " " + cliente.getApellido());

         Contenedor contenedor = new Contenedor();
        contenedor.setPeso(request.pesoContenedor());
        contenedor.setVolumen(request.volumenContenedor());
        contenedor.setClienteId(cliente.getDNI());  
        contenedor.setEstado("PENDIENTE_RETIRO");

		log.info("Contenedor creado: Peso=" + contenedor.getPeso() + ", Volumen=" + contenedor.getVolumen());

        Solicitud solicitud = new Solicitud();
        solicitud.setContenedor(contenedor);
        solicitud.setDireccionOrigen("CENTRAL");
        solicitud.setLatitudOrigen(CENTRAL_LATITUD);
        solicitud.setLongitudOrigen(CENTRAL_LONGITUD);
        solicitud.setDireccionDestino(request.direccionDestino());
        solicitud.setLatitudDestino(request.latitudDestino());
        solicitud.setLongitudDestino(request.longitudDestino());
        
		log.info("Calculando ruta entre origen y destino...");

		String coordsOrigen = String.format("%s,%s", CENTRAL_LONGITUD, CENTRAL_LATITUD);
        String coordsDestino = String.format("%s,%s", request.longitudDestino(), request.latitudDestino());
        
        OsrmResponse osrmResponse = osrmClient.obtenerRutasAlternativas(coordsOrigen, coordsDestino);
        if (osrmResponse == null || osrmResponse.getRoutes() == null || osrmResponse.getRoutes().isEmpty()) {
			log.error("No se pudo calcular una ruta entre el origen y el destino.");
            throw new RuntimeException("No se pudo calcular una ruta entre el origen y el destino.");
        }
        
		log.info("Ruta calculada exitosamente.");

        OsrmRoute rutaDirecta = osrmResponse.getRoutes().get(0);

		
        
        CamionDTO[] camionesAptos = camionClient.obtenerCamionesDisponibles(
            contenedor.getPeso(), contenedor.getVolumen(), jwt.getTokenValue()
        );

        if (camionesAptos.length == 0) {
			log.error("No hay camiones disponibles para las características del contenedor.");
            throw new RuntimeException("No hay camiones disponibles para las características de este contenedor.");
        }

		Map<String, Double> tarifaMap = getTarifaMap(jwt.getTokenValue());
        
         solicitud.setCostoEstimado(
            calcularCostoEstimado(camionesAptos, rutaDirecta.getDistance(), tarifaMap)
        );
        solicitud.setTiempoEstimado(rutaDirecta.getDuration() / 3600.0); // En horas
        solicitud.setEstado("BORRADOR");

		log.info("Solicitud creada con costo estimado: " + solicitud.getCostoEstimado() + " y tiempo estimado: " + solicitud.getTiempoEstimado() + " horas.");
        
        return persistenciaSolicitud.save(solicitud);
    }

	private Double calcularCostoEstimado(CamionDTO[] camionesAptos, Double distanciaMetros, Map<String, Double> tarifaMap) {
        if (camionesAptos.length == 0) return 0.0;

         double avgCostoKm = Arrays.stream(camionesAptos)
            .mapToDouble(CamionDTO::costoTrasladoPorKm)
            .average().orElse(0.0);
        
        double avgConsumoKm = Arrays.stream(camionesAptos)
            .mapToDouble(CamionDTO::consumoCombustibleKm)
            .average().orElse(0.0);

        double distanciaKm = distanciaMetros / 1000.0;
        
         
        double costoGestion = tarifaMap.get("CARGO_GESTION_TRAMO_SIMPLE");
        double costoCombustible = (distanciaKm * avgConsumoKm) * tarifaMap.get("PRECIO_LITRO_COMBUSTIBLE");
        
        double costoTraslado = distanciaKm * avgCostoKm;
        
        return costoGestion + costoTraslado + costoCombustible;
    }

	public List<TramoSugeridoDTO> obtenerTramosSugeridos(Long idDepositoOrigen, Long idDepositoDestino, String token) {
        
     DepositoDTO origen = depositoClient.obtenerDepositoPorId(idDepositoOrigen, token);
    DepositoDTO destino = depositoClient.obtenerDepositoPorId(idDepositoDestino, token);

     
    String coordsOrigen = String.format("%s,%s", origen.getLongitud(), origen.getLatitud());
    String coordsDestino = String.format("%s,%s", destino.getLongitud(), destino.getLatitud());
    
     OsrmResponse osrmResponse = osrmClient.obtenerRutasAlternativas(coordsOrigen, coordsDestino);

     if (osrmResponse == null || osrmResponse.getRoutes() == null || osrmResponse.getRoutes().isEmpty()) {
        return Collections.emptyList();
    }

    List<OsrmRoute> todasLasRutas = osrmResponse.getRoutes();

     List<OsrmRoute> rutasValidas = todasLasRutas.stream()
        .filter(ruta -> ruta.getDuration() <= MAX_DURACION_SEGUNDOS)
        .sorted(Comparator.comparingDouble(OsrmRoute::getDuration))
        .collect(Collectors.toList());

     if (!rutasValidas.isEmpty()) {
         return rutasValidas.stream()
            .limit(3)  
            .map(this::convertirARutaSugerida)
            .collect(Collectors.toList());
    } else {
         return todasLasRutas.stream()
            .min(Comparator.comparingDouble(OsrmRoute::getDuration))
            .map(this::convertirARutaSugerida)
            .map(List::of)  
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
    
    
    public String consultarEstadoSolicitud(Long id, Jwt principal) {
		log.info("Consultando estado de la solicitud con ID: " + id);
		
		ClienteDTO cliente = clienteClient.getClientePorKeycloakId(principal.getTokenValue());
		Solicitud solicitud = findSolicitudById(id);
		if (!solicitud.getContenedor().getClienteId().equals(cliente.getDNI())) {
			throw new AccessDeniedException("No tienes permiso para consultar esta solicitud.");
		}
        return "Solicitud: " + solicitud.getEstado() + ". Contenedor: " + solicitud.getContenedor().getEstado();
    }

	public List<Solicitud> obtenerSolicitudesBorrador() {
        return persistenciaSolicitud.findByEstado("BORRADOR");
    }
    
    public Solicitud asignarRuta(Long id, Ruta ruta) {
        Solicitud solicitud = findSolicitudById(id);

        if (!"BORRADOR".equalsIgnoreCase(solicitud.getEstado())) {
            throw new IllegalStateException("Solo se puede asignar una ruta a solicitudes en estado 'BORRADOR'.");
        }
        
        if (ruta == null || ruta.getTramos() == null || ruta.getTramos().isEmpty()) {
            throw new IllegalArgumentException("La ruta asignada no puede ser nula ni contener tramos vacíos.");
        }

		LocalDateTime proximaFechaInicio = LocalDateTime.now().plusHours(24);

        for (Tramo tramo : ruta.getTramos()) {
            if (tramo.getDuracionEstimadaSegundos() == null || tramo.getDuracionEstimadaSegundos() <= 0) {
                throw new IllegalArgumentException("El tramo (ID: " + tramo.getId() + ") no tiene una duración estimada válida.");
            }

            tramo.setFechaHoraInicioEstimada(proximaFechaInicio);

            LocalDateTime fechaFin = proximaFechaInicio.plusSeconds(tramo.getDuracionEstimadaSegundos());
            tramo.setFechaHoraFinEstimada(fechaFin);

            proximaFechaInicio = fechaFin.plusHours(HORAS_ESPERA_EN_DEPOSITO);
        }

        solicitud.setRuta(ruta);
        solicitud.setEstado("PROGRAMADA"); 
        return persistenciaSolicitud.save(solicitud);
    }

    public Tramo asignarCamionATramo(Long solicitudId, Long tramoId, Long camionId, String token) {
        Solicitud solicitud = findSolicitudById(solicitudId);
		Contenedor contenedor = solicitud.getContenedor(); 
        Tramo tramo = solicitud.getRuta().getTramos().stream()
            .filter(t -> t.getId().equals(tramoId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Tramo no encontrado"));
            
        CamionDTO camion = camionClient.obtenerCamionPorId(camionId, token);
        if (camion == null) {
            throw new NoSuchElementException("Camión no encontrado con ID: " + camionId);
        }

        if (camion.costoTrasladoPorKm() < contenedor.getPeso() || 
            camion.volumenM3() < contenedor.getVolumen()) {
            throw new IllegalArgumentException("Validación fallida: El camión (ID: " + camionId + ") no es apto para el peso/volumen del contenedor.");
        }
            
        tramo.setCamionId(camionId);
        tramo.setEstado("ASIGNADO");
        
        camionClient.actualizarDisponibilidad(camionId, false, token);
        
        persistenciaSolicitud.save(solicitud);
        return tramo;
    }

    public Tramo actualizarEstadoTramo(Long solicitudId, Long tramoId, String estado, String transportistaKeycloakId, Jwt principal) {
        Solicitud solicitud = findSolicitudById(solicitudId);
        Tramo tramo = solicitud.getRuta().getTramos().stream()
            .filter(t -> t.getId().equals(tramoId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Tramo no encontrado"));
        
		String token = principal.getTokenValue();


        List<CamionDTO> misCamiones = camionClient.getAllCamiones(token);
        
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
                
                 camionClient.actualizarDisponibilidad(tramo.getCamionId(), true, token);
                
                 boolean esUltimoTramo = esUltimoTramo(solicitud.getRuta(), tramo);
                if (esUltimoTramo) {
                    solicitud.setEstado("ENTREGADA");
                    solicitud.getContenedor().setEstado("ENTREGADO");
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

 
    private Solicitud findSolicitudById(Long id) {
        return persistenciaSolicitud.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada con ID: " + id));
    }

  

    private boolean esUltimoTramo(Ruta ruta, Tramo tramoActual) {
         return ruta.getTramos().get(ruta.getTramos().size() - 1).getId().equals(tramoActual.getId());
    }

    private void calcularCostosYTiemposReales(Solicitud solicitud, String token, Map<String, Double> tarifaMap) {
        
        double costoTotal = 0.0;
        long tiempoTotalSegundos = 0;

        
        
        LocalDateTime fechaLlegadaAnterior = null;

		final Double PRECIO_COMBUSTIBLE = tarifaMap.get("PRECIO_LITRO_COMBUSTIBLE");
        final Double CARGO_GESTION = tarifaMap.get("CARGO_GESTION_TRAMO_SIMPLE");
        final Double COSTO_ESTADIA = tarifaMap.get("COSTO_ESTADIA_DEPOSITO_DIA");

        for (Tramo tramo : solicitud.getRuta().getTramos()) {
            if (tramo.getEstado().equals("FINALIZADO")) {
                CamionDTO camion = camionClient.obtenerCamionPorId(tramo.getCamionId(), token);
                double distanciaKm = tramo.getDistanciaMetros() / 1000.0;
                
                 costoTotal += CARGO_GESTION;
                costoTotal += distanciaKm * camion.costoTrasladoPorKm();
                costoTotal += (distanciaKm * camion.consumoCombustibleKm()) * PRECIO_COMBUSTIBLE;
                
                if (fechaLlegadaAnterior != null) {
                    long diasEstadia = Duration.between(fechaLlegadaAnterior, tramo.getFechaHoraInicioReal()).toDays();
                    if (diasEstadia > 0) {
                        costoTotal += diasEstadia * COSTO_ESTADIA;
                    }
                }
                
                 tiempoTotalSegundos += Duration.between(tramo.getFechaHoraInicioReal(), tramo.getFechaHoraFinReal()).toSeconds();
                fechaLlegadaAnterior = tramo.getFechaHoraFinReal();
            }
        }
        
        solicitud.setCostoFinal(costoTotal);
        solicitud.setTiempoReal(tiempoTotalSegundos / 3600.0); 
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
        
         return tarifas.stream()
                .collect(Collectors.toMap(TarifaDTO::getDescripcion, TarifaDTO::getValor));
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
       
        return persistenciaSolicitud.findContenedoresPendientesConFiltros(estado);
    }

	public List<Tramo> obtenerMisTramosAsignados(Jwt principal) {
        
         List<CamionDTO> misCamiones = camionClient.obtenerMisCamiones(principal);
        
        if (misCamiones == null || misCamiones.isEmpty()) {
            return Collections.emptyList();
        }

         List<Long> misCamionIds = misCamiones.stream()
            .map(camion -> (Long) ((Number) camion.id()).longValue()) 
            .collect(Collectors.toList());

         List<String> estadosRelevantes = List.of("ASIGNADO", "INICIADO");
        
         return persistenciaSolicitud.findAll().stream()  
            .filter(solicitud -> solicitud.getRuta() != null && solicitud.getRuta().getTramos() != null) 
            .flatMap(solicitud -> solicitud.getRuta().getTramos().stream())  
            .filter(tramo -> misCamionIds.contains(tramo.getCamionId()))  
            .filter(tramo -> estadosRelevantes.contains(tramo.getEstado()))  
            .collect(Collectors.toList());
	}
}