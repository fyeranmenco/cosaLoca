using Microsoft.AspNetCore.Mvc;
using complejoDeportivo.Models;

namespace complejoDeportivo.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ReservaController: ControllerBase
    {
        private readonly ILogger<ReservaController> _logger;

        public ReservaController(ILogger<ReservaController> logger)
        {
            _logger = logger;
        }

        [HttpGet(Name = "GetReserva")]
        public IEnumerable<Reserva> Get()
		{
			return new LinkedList<Reserva>();	

		}
    }
}
