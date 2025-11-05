using complejoDeportivo.Models;
using complejoDeportivo.DTOs;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Linq;
using complejoDeportivo.Repositories.Interfaces;
using complejoDeportivo.Services.Interfaces;


namespace complejoDeportivo.Services.Implementations
{

	public class CanchaService : ICanchaService
	{
		private readonly ICanchaRepository _repository;

		public CanchaService(ICanchaRepository repository)
		{
			_repository = repository;
		}

		public async Task<IEnumerable<CanchaDTO>> GetAllAsync()
		{
			var canchas = await _repository.GetAllAsync();
			return canchas.Select(c => new CanchaDTO(c));
		}

		public async Task<CanchaDTO> GetByIdAsync(int id)
		{
			var c = await _repository.GetByIdAsync(id);
			if (c == null)
			{
				throw new NotFoundException($"Cancha con ID {id} no encontrado.");
			}
			return new CanchaDTO(c);
		}

		public async Task<CanchaDTO> CreateAsync(CrearCanchaDTO createDto)
		{
			var Cancha = new Cancha(createDto);
			var nuevaCancha = await _repository.CreateAsync(Cancha);
			return new CanchaDTO(nuevaCancha);
		}

		public async Task UpdateAsync(int id, CrearCanchaDTO updateDto)
		{
			var cancha = await _repository.GetByIdAsync(id);
			if (cancha == null)
			{
				throw new NotFoundException($"Cancha con ID {id} no encontrado.");
			}

			cancha = new Cancha(updateDto);
			cancha.CanchaId = id;
			await _repository.UpdateAsync(cancha);
		}

		public async Task DeleteAsync(int id)
		{
			var cancha = await _repository.GetByIdAsync(id);
			if (cancha == null)
			{
				throw new NotFoundException($"Cancha con ID {id} no encontrado.");
			}
			await _repository.DeleteAsync(id);
		}
	}
}