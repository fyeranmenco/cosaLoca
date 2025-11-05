using complejoDeportivo.DTOs;
using complejoDeportivo.Services.Implementations;
using complejoDeportivo.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace complejoDeportivo.Controllers
{
    [Route("api/admin/complejos")]
    [ApiController]
    [Authorize(Roles = "Admin")] // Solo Admin puede gestionar complejos
    public class ComplejoController : ControllerBase
    {
        private readonly IComplejoService _service;

        public ComplejoController(IComplejoService service)
        {
            _service = service;
        }

        [HttpGet]
        [AllowAnonymous] // Permitimos ver complejos sin loguearse (útil para `ReservaController`)
        public async Task<ActionResult<IEnumerable<ComplejoDetalleDTO>>> GetAll()
        {
            return Ok(await _service.GetAllAsync());
        }

        [HttpGet("{id}")]
        [AllowAnonymous] // Permitimos ver detalle sin loguearse
        public async Task<ActionResult<ComplejoDetalleDTO>> GetById(int id)
        {
            try
            {
                var complejo = await _service.GetByIdAsync(id);
                return Ok(complejo);
            }
            catch (NotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        [HttpPost]
        public async Task<ActionResult<ComplejoDetalleDTO>> Create(CrearComplejoDTO createDto)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }
            var nuevoComplejo = await _service.CreateAsync(createDto);
            return CreatedAtAction(nameof(GetById), new { id = nuevoComplejo.ComplejoId }, nuevoComplejo);
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> Update(int id, ActualizarComplejoDTO updateDto)
        {
            try
            {
                await _service.UpdateAsync(id, updateDto);
                return NoContent();
            }
            catch (NotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            try
            {
                await _service.DeleteAsync(id);
                return NoContent();
            }
            catch (NotFoundException ex)
            {
                return NotFound(new { message = ex.Message });
            }
            catch (InvalidOperationException ex)
            {
                // Captura error de borrado (ej. Foreign Key de Cancha)
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}