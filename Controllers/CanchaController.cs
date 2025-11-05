using complejoDeportivo.Services.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;


namespace complejoDeportivo.Controllers
{
	[ApiController]
	[Route("api/[controller]")]
	[Authorize(Roles = "Admin,Empleado")]
	public class CanchaController : ControllerBase
	{
		ICanchaService _CanchaService;

		public CanchaController(ICanchaService canchaService)
		{
			_CanchaService = canchaService;
		}

		[HttpGet]

	}
}