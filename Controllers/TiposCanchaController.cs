using complejoDeportivo.DTOs;
using complejoDeportivo.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace complejoDeportivo.Controllers
{
	[ApiController]
    [Route("api/[controller]")]
	[Authorize(Roles = "Admin,Empleado")]
    public class TiposCanchaController : ControllerBase
	{
		ITipoCanchaService _tipoCanchaService;
		public TiposCanchaController(ITipoCanchaService tipoCanchaService)
		{
			_tipoCanchaService = tipoCanchaService;
		}

		[HttpGet]
		public IActionResult GetTiposCancha()
		{
			var tiposCancha = _tipoCanchaService.GetAllAsync();
			return Ok(tiposCancha);
		}

		[HttpGet("{id}")]
		public IActionResult GetTipoCancha(int id)
		{
			var tipoCancha = _tipoCanchaService.GetByIdAsync(id);
			return Ok(tipoCancha);
		}

		[HttpPost]
		public IActionResult CreateTipoCancha([FromBody] CreateTipoCanchaDTO createDto)
		{
			var tipoCancha = _tipoCanchaService.CreateAsync(createDto);
			return Ok(tipoCancha);
		}
	}
}
