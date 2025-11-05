using complejoDeportivo.DTOs.Dashboard;
using complejoDeportivo.Models;
using Microsoft.EntityFrameworkCore;

namespace complejoDeportivo.Repositories.Dashboard
{
    public class DashboardRepository : IDashboardRepository
    {
        private readonly ComplejoDeportivoContext _context;

        public DashboardRepository(ComplejoDeportivoContext context)
        {
            _context = context;
        }

        // Método auxiliar para aplicar filtros básicos
        private IQueryable<Reserva> AplicarFiltros(FiltrosDashboardDto filtros)
        {
            var (inicio, fin) = filtros.ObtenerRangoFechas();

            var query = _context.Reservas
                .Include(r => r.DetalleReservas)
                    .ThenInclude(d => d.Cancha)
                .Where(r => r.DetalleReservas.Any(d => d.Cancha.ComplejoId == filtros.ComplejoId))
                .Where(r => r.Fecha >= DateOnly.FromDateTime(inicio) && r.Fecha <= DateOnly.FromDateTime(fin));

            if (filtros.CanchaId.HasValue)
            {
                query = query.Where(r => r.DetalleReservas.Any(d => d.CanchaId == filtros.CanchaId.Value));
            }
            if (filtros.EstadoReservaId.HasValue)
            {
                query = query.Where(r => r.EstadoReservaId == filtros.EstadoReservaId.Value);
            }
            if (filtros.ClienteId.HasValue)
            {
                query = query.Where(r => r.ClienteId == filtros.ClienteId.Value);
            }
            
            return query;
        }

        public async Task<DashboardResumenDto> GetResumenAsync(FiltrosDashboardDto filtros)
        {
            var (inicio, fin) = filtros.ObtenerRangoFechas();
            var hoy = DateOnly.FromDateTime(DateTime.Today);
            var mesActualInicio = new DateOnly(hoy.Year, hoy.Month, 1);
            var mesAnteriorInicio = mesActualInicio.AddMonths(-1);
            var mesAnteriorFin = mesActualInicio.AddDays(-1);

            var queryBase = _context.Reservas
                .Include(r => r.DetalleReservas)
                    .ThenInclude(d => d.Cancha)
                .Where(r => r.DetalleReservas.Any(d => d.Cancha.ComplejoId == filtros.ComplejoId));

            var resumen = new DashboardResumenDto
            {
                // Métricas de Hoy
                ReservasHoy = await queryBase.CountAsync(r => r.Fecha == hoy),
                IngresosHoy = await queryBase.Where(r => r.Fecha == hoy).SumAsync(r => r.Total),
                ClientesNuevosHoy = await _context.Clientes.CountAsync(c => c.FechaRegistro.Date == DateTime.Today),

                // Métricas de Estado (basadas en filtros)
                ReservasPendientes = await AplicarFiltros(filtros).CountAsync(r => r.EstadoReserva.Nombre == "Pendiente"),
                ReservasConfirmadas = await AplicarFiltros(filtros).CountAsync(r => r.EstadoReserva.Nombre == "Confirmada"),
                ReservasCanceladas = await AplicarFiltros(filtros).CountAsync(r => r.EstadoReserva.Nombre == "Cancelada"),

                // Infraestructura
                CanchasTotales = await _context.Canchas.CountAsync(c => c.ComplejoId == filtros.ComplejoId),
                CanchasActivas = await _context.Canchas.CountAsync(c => c.ComplejoId == filtros.ComplejoId && c.Activa),

                // Comparativas
                IngresosMesActual = await queryBase.Where(r => r.Fecha >= mesActualInicio && r.Fecha <= hoy).SumAsync(r => r.Total),
                IngresosMesAnterior = await queryBase.Where(r => r.Fecha >= mesAnteriorInicio && r.Fecha <= mesAnteriorFin).SumAsync(r => r.Total),
                ReservasMesActual = await queryBase.CountAsync(r => r.Fecha >= mesActualInicio && r.Fecha <= hoy),
                ReservasMesAnterior = await queryBase.CountAsync(r => r.Fecha >= mesAnteriorInicio && r.Fecha <= mesAnteriorFin),

                // Clientes
                TotalClientesRegistrados = await _context.Clientes.CountAsync()
            };

            return resumen;
        }

        public async Task<List<IngresoPeriodoDto>> GetIngresosPorPeriodoAsync(FiltrosDashboardDto filtros, string tipoAgrupacion = "diario")
        {
            var query = AplicarFiltros(filtros);
            
            // Lógica de agrupación (simplificada a mensual por defecto si no es diario)
            // Una implementación real necesitaría más lógica para `tipoAgrupacion`
            
            if (tipoAgrupacion == "diario")
            {
                 return await query
                    .GroupBy(r => r.Fecha)
                    .Select(g => new IngresoPeriodoDto
                    {
                        Fecha = g.Key.ToDateTime(TimeOnly.MinValue),
                        Total = g.Sum(r => r.Total),
                        CantidadReservas = g.Count(),
                        CantidadClientes = g.Select(r => r.ClienteId).Distinct().Count()
                    })
                    .OrderBy(dto => dto.Fecha)
                    .ToListAsync();
            }
            
            // Agrupación mensual
            return await query
                .GroupBy(r => new { r.Fecha.Year, r.Fecha.Month })
                .Select(g => new IngresoPeriodoDto
                {
                    Fecha = new DateTime(g.Key.Year, g.Key.Month, 1),
                    Total = g.Sum(r => r.Total),
                    CantidadReservas = g.Count(),
                    CantidadClientes = g.Select(r => r.ClienteId).Distinct().Count()
                })
                .OrderBy(dto => dto.Fecha)
                .ToListAsync();
        }

        public async Task<List<ReservaEstadoDto>> GetEstadosReservasAsync(FiltrosDashboardDto filtros)
        {
            var query = AplicarFiltros(filtros);

            var datos = await query
                .Include(r => r.EstadoReserva)
                .GroupBy(r => r.EstadoReserva.Nombre)
                .Select(g => new ReservaEstadoDto
                {
                    Estado = g.Key,
                    Cantidad = g.Count()
                })
                .ToListAsync();
            
            ReservaEstadoDto.CalcularPorcentajes(datos);
            return datos;
        }

        public async Task<List<CanchaPopularDto>> GetCanchasPopularesAsync(FiltrosDashboardDto filtros)
        {
            var (inicio, fin) = filtros.ObtenerRangoFechas();
            var query = AplicarFiltros(filtros);

            var canchas = await query
                .SelectMany(r => r.DetalleReservas)
                .Include(d => d.Cancha.TipoCancha)
                .GroupBy(d => new { d.CanchaId, d.Cancha.Nombre, TipoCancha = d.Cancha.TipoCancha.Nombre })
                .Select(g => new CanchaPopularDto
                {
                    CanchaId = g.Key.CanchaId,
                    Nombre = g.Key.Nombre,
                    TipoCancha = g.Key.TipoCancha,
                    ReservasCount = g.Count(),
                    IngresosTotales = g.Sum(d => d.Subtotal),
                    HorasTotales = g.Sum(d => d.CantidadHoras)
                    // OcupacionPorcentaje requeriría un cálculo más complejo
                })
                .OrderByDescending(dto => dto.ReservasCount)
                .Take(10)
                .ToListAsync();

            return CanchaPopularDto.AplicarRanking(canchas);
        }

        public async Task<List<ClienteFrecuenteDto>> GetClientesFrecuentesAsync(FiltrosDashboardDto filtros)
        {
            var (inicio, fin) = filtros.ObtenerRangoFechas();
            var query = AplicarFiltros(filtros);

            var clientes = await query
                .Include(r => r.Cliente)
                .GroupBy(r => r.Cliente)
                .Select(g => new ClienteFrecuenteDto
                {
                    ClienteId = g.Key.ClienteId,
                    NombreCompleto = g.Key.Nombre + " " + g.Key.Apellido,
                    Email = g.Key.Email,
                    Telefono = g.Key.Telefono,
                    TotalReservas = g.Count(),
                    TotalGastado = g.Sum(r => r.Total),
                    UltimaReserva = g.Max(r => r.Fecha).ToDateTime(TimeOnly.MinValue),
                    FechaRegistro = g.Key.FechaRegistro
                })
                .OrderByDescending(dto => dto.TotalReservas)
                .Take(10)
                .ToListAsync();
            
            return ClienteFrecuenteDto.AplicarRanking(clientes);
        }

        public Task<List<ReservaRecienteDto>> GetReservasRecientesAsync(FiltrosDashboardDto filtros)
        {
             // Implementación de ejemplo (los 10 más recientes)
            var query = AplicarFiltros(filtros);

            return query
                .Include(r => r.Cliente)
                .Include(r => r.EstadoReserva)
                .Include(r => r.DetalleReservas)
                    .ThenInclude(d => d.Cancha)
                .OrderByDescending(r => r.FechaCreacion)
                .Take(10)
                .Select(r => new ReservaRecienteDto
                {
                    ReservaId = r.ReservaId,
                    ClienteNombre = r.Cliente.Nombre + " " + r.Cliente.Apellido,
                    ClienteEmail = r.Cliente.Email,
                    CanchaNombre = r.DetalleReservas.FirstOrDefault().Cancha.Nombre, // Simplificación
                    TipoCancha = r.DetalleReservas.FirstOrDefault().Cancha.TipoCancha.Nombre, // Simplificación
                    Fecha = r.Fecha.ToDateTime(TimeOnly.MinValue),
                    HoraInicio = r.HoraInicio.ToTimeSpan(),
                    HoraFin = r.HoraFin.ToTimeSpan(),
                    Estado = r.EstadoReserva.Nombre,
                    Total = r.Total,
                    FechaCreacion = r.FechaCreacion
                })
                .ToListAsync();
        }

        public Task<List<OcupacionHorarioDto>> GetOcupacionPorHorarioAsync(FiltrosDashboardDto filtros)
        {
            // Lógica compleja de agrupación por franjas horarias.
            // Se omite por brevedad, retornando datos de ejemplo.
            return Task.FromResult(OcupacionHorarioDto.CrearFranjasHorarias());
        }

        public Task<List<AlertaStockDto>> GetAlertasStockAsync(FiltrosDashboardDto filtros)
        {
            // Lógica de Stock (fuera del alcance de 'Reservas', pero DTO existe)
            // Se omite por brevedad, retornando lista vacía.
            return Task.FromResult(new List<AlertaStockDto>());
        }
    }
}