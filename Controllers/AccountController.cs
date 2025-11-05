using complejoDeportivo.DTOs;
using complejoDeportivo.Services.Implementations;
using complejoDeportivo.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace complejoDeportivo.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AccountController : ControllerBase
    {
        private readonly IUsuarioService _usuarioService;

        public AccountController(IUsuarioService usuarioService)
        {
            _usuarioService = usuarioService;
        }

        [HttpPost("register")]
        [AllowAnonymous]
        public async Task<ActionResult<UsuarioDTO>> Register(RegisterClienteDTO dto)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            try
            {
                var nuevoUsuario = await _usuarioService.RegisterClienteAsync(dto);
                // Devolvemos 200 OK con los datos del usuario creado
                return Ok(nuevoUsuario);
            }
            catch (System.Exception ex)
            {
                // Captura el error "El email ya está registrado" del servicio
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}