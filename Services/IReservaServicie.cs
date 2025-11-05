using complejoDeportivo.DTOs;
using complejoDeportivo.Models;

namespace complejoDeportivo.Services
{
    public interface IReservaServicie
    {
        List<DisponibilidadCanchaDTO> ObtenerTurnosDisponibles(int canchaId, DateOnly fecha);
        ReservaDTO CrearReserva(CrearReservaDTO dto);
        List<ReservaDTO> ListarReservasCliente(int clienteId);
        bool CancelarReserva(CancelarReservaDTO dto);
        List<ComplejoDTO> ListarComplejos();
        List<CanchaDTO> ListarCanchasPorComplejo(int complejoId);
        List<HorarioLibreDTO> ObtenerHorariosDisponiblesCancha(int canchaId, DateOnly fecha);
    }
}
