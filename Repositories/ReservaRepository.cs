using complejoDeportivo.DTOs;
using complejoDeportivo.Models;

namespace complejoDeportivo.Repositories
{
    public class ReservaRepository : IReservaRepository
    {
        private readonly ComplejoDeportivoContext _contexto;

        public ReservaRepository(ComplejoDeportivoContext contexto)
        {
            _contexto = contexto;
        }

        public List<DisponibilidadCanchaDTO> ObtenerTurnosDisponibles(int canchaId, DateOnly fecha, TimeOnly apertura, TimeOnly cierre)
        {
            var turnos = new List<DisponibilidadCanchaDTO>();
            var hora = apertura;

            while (hora.AddHours(1) <= cierre)
            {
                var horaFin = hora.AddHours(1);

                if (!ExisteReservaSuperpuesta(canchaId, fecha, hora, horaFin)
                    && !ExisteBloqueo(canchaId, fecha, hora, horaFin))
                {
                    turnos.Add(new DisponibilidadCanchaDTO
                    {
                        CanchaId = canchaId,
                        Fecha = fecha,
                        HoraInicio = hora,
                        HoraFin = horaFin
                    });
                }

                hora = hora.AddHours(1);
            }

            return turnos;
        }

        public bool ExisteReservaSuperpuesta(int canchaId, DateOnly fecha, TimeOnly inicio, TimeOnly fin)
        {
            return (from r in _contexto.Reservas
                    join d in _contexto.DetalleReservas on r.ReservaId equals d.ReservaId
                    where d.CanchaId == canchaId
                    && r.Fecha == fecha
                    && r.HoraInicio < fin
                    && r.HoraFin > inicio
                    select r).Any();
        }

        public bool ExisteBloqueo(int canchaId, DateOnly fecha, TimeOnly inicio, TimeOnly fin)
        {
            return _contexto.BloqueoCanchas
                .Any(b => b.CanchaId == canchaId
                    && b.Fecha == fecha
                    && b.HoraInicio < fin
                    && b.HoraFin > inicio);
        }

        public Tarifa ObtenerTarifaVigente(int canchaId, DateOnly fecha)
        {
            var tarifa = _contexto.Tarifas
                .Where(t => t.CanchaId == canchaId && t.EsActual)
                .OrderByDescending(t => t.FechaVigencia)
                .FirstOrDefault();

            if (tarifa != null) return tarifa;

			var fallback = _contexto.Tarifas
				.Where(t => t.CanchaId == canchaId && t.FechaVigencia <= fecha)
				.OrderByDescending(t => t.FechaVigencia)
				.FirstOrDefault();

			if (fallback != null) return fallback;

			throw new InvalidOperationException($"No se encontró tarifa vigente para la cancha {canchaId} en la fecha {fecha}.");
        }

        public Reserva ObtenerReservaPorId(int reservaId)
        {
			var reserva = _contexto.Reservas.FirstOrDefault(r => r.ReservaId == reservaId);
			if (reserva == null)
				throw new InvalidOperationException($"No se encontró reserva con id {reservaId}.");
			return reserva;
        }

        public List<Reserva> ObtenerReservasPorCliente(int clienteId)
        {
            return _contexto.Reservas
                .Where(r => r.ClienteId == clienteId)
                .OrderByDescending(r => r.Fecha)
                .ToList();
        }

        public void AgregarReserva(Reserva reserva)
        {
            _contexto.Reservas.Add(reserva);
        }

        public void AgregarDetalle(DetalleReserva detalle)
        {
            _contexto.DetalleReservas.Add(detalle);
        }
        public List<ComplejoDTO> ObtenerComplejos()
        {
            return _contexto.Complejos
                .Select(c => new ComplejoDTO
                {
                    ComplejoId = c.ComplejoId,
                    Nombre = c.Nombre
                }).ToList();
        }

        public List<CanchaDTO> ObtenerCanchasPorComplejo(int complejoId)
        {
            return _contexto.Canchas
                .Where(c => c.ComplejoId == complejoId)
                .Select(c => new CanchaDTO
                {
                    CanchaId = c.CanchaId,
                    Nombre = c.Nombre
                }).ToList();
        }
        public List<HorarioLibreDTO> ObtenerHorariosDisponiblesCancha(int canchaId, DateOnly fecha, TimeOnly apertura, TimeOnly cierre)
        {
            List<HorarioLibreDTO> resultado = new List<HorarioLibreDTO>();

            for (TimeOnly hora = apertura; hora < cierre; hora = hora.AddHours(1))
            {
                TimeOnly siguiente = hora.Add(TimeSpan.FromHours(1));

                bool ocupadoPorReserva = _contexto.Reservas
                    .Any(r => r.DetalleReservas.Any(d => d.CanchaId == canchaId) && r.Fecha == fecha &&
                         hora < r.HoraFin && siguiente > r.HoraInicio);

                if (!ocupadoPorReserva)
                {
                    resultado.Add(new HorarioLibreDTO
                    {
                        HoraInicio = hora,
                        HoraFin = siguiente
                    });
                }
            }

            return resultado;
        }

        public void Guardar()
        {
            _contexto.SaveChanges();
        }
    }
}

