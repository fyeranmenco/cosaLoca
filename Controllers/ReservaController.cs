using Microsoft.AspNetCore.Mvc;
using complejoDeportivo.Models;
using complejoDeportivo.Services;
using complejoDeportivo.DTOs;
using Microsoft.AspNetCore.Authorization;

namespace complejoDeportivo.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ReservaController : ControllerBase
    {
        private readonly IReservaServicie _reservaService;

        public ReservaController(IReservaServicie reservaService)
        {
            _reservaService = reservaService;
        }

        [HttpGet("complejos")]
        [AllowAnonymous]
        public ActionResult<IEnumerable<ComplejoDTO>> GetComplejos()
        {
            var complejos = _reservaService.ListarComplejos();
            return Ok(complejos);
        }

        [HttpGet("canchas/{complejoId}")]
        [AllowAnonymous]
        public ActionResult<IEnumerable<CanchaDTO>> GetCanchasPorComplejo(int complejoId)
        {
            var canchas = _reservaService.ListarCanchasPorComplejo(complejoId);
            return Ok(canchas);
        }

        [HttpGet("disponibilidad")]
        [AllowAnonymous]
        public ActionResult<IEnumerable<HorarioLibreDTO>> GetHorariosDisponibles(
            [FromQuery] int canchaId,
            [FromQuery] DateOnly fecha)
        {
            // Este método ya estaba implementado en tu ReservaService y Repository
            var horarios = _reservaService.ObtenerHorariosDisponiblesCancha(canchaId, fecha);
            return Ok(horarios);
        }

        [HttpPost]
        [Authorize(Roles = "Cliente,Admin,Empleado")]
        public ActionResult<ReservaDTO> CrearReserva([FromBody] CrearReservaDTO dto)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            try
            {
                var nuevaReserva = _reservaService.CrearReserva(dto);
                // Devolvemos 201 Created con la ubicación de la nueva reserva
                return CreatedAtAction(nameof(GetReservasCliente), new { clienteId = nuevaReserva.ClienteId }, nuevaReserva);
            }
            catch (Exception ex)
            {
                // Capturamos excepciones de lógica de negocio (ej. "Horario ocupado")
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpGet("cliente/{clienteId}")]
        [Authorize(Roles = "Cliente,Admin,Empleado")]
        public ActionResult<IEnumerable<ReservaDTO>> GetReservasCliente(int clienteId)
        {
            // Aquí se podría añadir validación de seguridad para que un Cliente
            // solo vea sus propias reservas, pero por ahora se implementa la lógica base.
            var reservas = _reservaService.ListarReservasCliente(clienteId);
            return Ok(reservas);
        }

        [HttpPut("cancelar")]
        [Authorize(Roles = "Cliente,Admin,Empleado")]
        public IActionResult CancelarReserva([FromBody] CancelarReservaDTO dto)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            try
            {
                var cancelada = _reservaService.CancelarReserva(dto);
                if (cancelada)
                {
                    return NoContent(); // 204 No Content (éxito)
                }
                else
                {
                    return NotFound(new { message = "Reserva no encontrada o no pertenece al cliente." });
                }
            }
            catch (Exception ex)
            {
                // Capturamos la excepción de las 24hs de anticipación
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}