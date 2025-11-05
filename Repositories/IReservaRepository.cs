using complejoDeportivo.DTOs;
using complejoDeportivo.Models;

namespace complejoDeportivo.Repositories
{
    public interface IReservaRepository
    {
        Reserva ObtenerReservaPorId(int reservaId);
        List<Reserva> ObtenerReservasPorCliente(int clienteId);
        void AgregarReserva(Reserva reserva);
        void AgregarDetalle(DetalleReserva detalle);
        bool ExisteReservaSuperpuesta(int canchaId, DateOnly fecha, TimeOnly inicio, TimeOnly fin);
        bool ExisteBloqueo(int canchaId, DateOnly fecha, TimeOnly inicio, TimeOnly fin);
        Tarifa ObtenerTarifaVigente(int canchaId, DateOnly fecha);
        List<ComplejoDTO> ObtenerComplejos();
        List<CanchaDTO> ObtenerCanchasPorComplejo(int complejoId);
        List<HorarioLibreDTO> ObtenerHorariosDisponiblesCancha(int canchaId, DateOnly fecha, TimeOnly apertura, TimeOnly cierre);
        void Guardar();
        List<DisponibilidadCanchaDTO> ObtenerTurnosDisponibles(int canchaId, DateOnly date, TimeOnly apertura, TimeOnly cierre);
    }
}
