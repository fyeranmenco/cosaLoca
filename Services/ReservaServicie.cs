using complejoDeportivo.DTOs;
using complejoDeportivo.Models;
using complejoDeportivo.Repositories;

namespace complejoDeportivo.Services
{
    public class ReservaServicie : IReservaServicie
    {
        private readonly IReservaRepository _repo;
        private readonly TimeOnly _apertura = new TimeOnly(8, 0, 0);
        private readonly TimeOnly _cierre = new TimeOnly(23, 0, 0);

        public ReservaServicie(IReservaRepository repo)
        {
            _repo = repo;
        }

        public List<DisponibilidadCanchaDTO> ObtenerTurnosDisponibles(int canchaId, DateOnly fecha)
        {
            return _repo.ObtenerTurnosDisponibles(canchaId, fecha, _apertura, _cierre);
        }

        public ReservaDTO CrearReserva(CrearReservaDTO dto)
        {
            var horas = dto.HoraFin - dto.HoraInicio;
            if (horas.TotalHours < 1 || horas.TotalHours % 1 != 0)
                throw new Exception("Las reservas deben ser en bloques de 1 hora.");

            // Verificar disponibilidad hora por hora
            var h = dto.HoraInicio;
            while (h < dto.HoraFin)
            {
                var fin = h.AddHours(1);
                if (_repo.ExisteReservaSuperpuesta(dto.CanchaId, dto.Fecha, h, fin)
                    || _repo.ExisteBloqueo(dto.CanchaId, dto.Fecha, h, fin))
                    throw new Exception($"Horario ocupado: {h} - {fin}");

                h = fin;
            }

            var tarifa = _repo.ObtenerTarifaVigente(dto.CanchaId, dto.Fecha);
            if (tarifa == null) throw new Exception("No existe tarifa vigente.");

            var total = tarifa.Precio * (decimal)horas.TotalHours;

            var reserva = new Reserva
            {
                ClienteId = dto.ClienteId,
                Fecha = dto.Fecha,
                HoraInicio = dto.HoraInicio,
                HoraFin = dto.HoraFin,
                EstadoReservaId = 1, // Confirmada
                Ambito = dto.Ambito,
                FechaCreacion = DateTime.Now,
                Total = total
            };

            _repo.AgregarReserva(reserva);
            _repo.Guardar();

            var detalle = new DetalleReserva
            {
                ReservaId = reserva.ReservaId,
                CanchaId = dto.CanchaId,
                TarifaHoraId = tarifa.TarifaId,
                CantidadHoras = (int)horas.TotalHours,
                Descuento = 0,
                Recargo = 0,
                Subtotal = total
            };

            _repo.AgregarDetalle(detalle);
            _repo.Guardar();

            return new ReservaDTO
            {
                ReservaId = reserva.ReservaId,
                ClienteId = reserva.ClienteId,
                CanchaId = dto.CanchaId,
                Fecha = reserva.Fecha,
                HoraInicio = reserva.HoraInicio,
                HoraFin = reserva.HoraFin,
                Total = reserva.Total,
                Estado = "Confirmada",
                FechaCreacion = reserva.FechaCreacion
            };
        }

        public List<ReservaDTO> ListarReservasCliente(int clienteId)
        {
            return _repo.ObtenerReservasPorCliente(clienteId)
                .Select(r => new ReservaDTO
                {
                    ReservaId = r.ReservaId,
                    ClienteId = r.ClienteId,
                    Fecha = r.Fecha,
                    HoraInicio = r.HoraInicio,
                    HoraFin = r.HoraFin,
                    Total = r.Total,
                    Estado = r.EstadoReservaId == 1 ? "Confirmada" : "Cancelada",
                    FechaCreacion = r.FechaCreacion
                }).ToList();
        }

        public bool CancelarReserva(CancelarReservaDTO dto)
        {
            var reserva = _repo.ObtenerReservaPorId(dto.ReservaId);
            if (reserva == null || reserva.ClienteId != dto.ClienteId) return false;

            var tiempoRestante = reserva.Fecha.ToDateTime(reserva.HoraInicio) - DateTime.Now;
            if (tiempoRestante.TotalHours < 24)
                throw new Exception("La reserva solo se puede cancelar con 24 horas de anticipación.");

            reserva.EstadoReservaId = 2; // Cancelada
            _repo.Guardar();
            return true;
        }
        public List<ComplejoDTO> ListarComplejos()
        {
            return _repo.ObtenerComplejos();
        }

        public List<CanchaDTO> ListarCanchasPorComplejo(int complejoId)
        {
            return _repo.ObtenerCanchasPorComplejo(complejoId);
        }

        public List<HorarioLibreDTO> ObtenerHorariosDisponiblesCancha(int canchaId, DateOnly fecha)
        {
            TimeOnly apertura = new TimeOnly(8, 0, 0);
            TimeOnly cierre = new TimeOnly(23, 0, 0);
            return _repo.ObtenerHorariosDisponiblesCancha(canchaId, fecha, apertura, cierre);
        }

    }
}
