using complejoDeportivo.Models;

namespace complejoDeportivo.Repositories.Interfaces
{
    public interface IClienteRepository
    {
        Task<Cliente> CreateAsync(Cliente cliente);
        Task<bool> DoesDocumentoExistAsync(string documento);
        Task<bool> DoesTelefonoExistAsync(string telefono);
    }
}