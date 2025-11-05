using complejoDeportivo.Models;
using complejoDeportivo.Repositories.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace complejoDeportivo.Repositories.Implementations
{
    public class ClienteRepository : IClienteRepository
    {
        private readonly ComplejoDeportivoContext _context;

        public ClienteRepository(ComplejoDeportivoContext context)
        {
            _context = context;
        }

        public async Task<Cliente> CreateAsync(Cliente cliente)
        {
            _context.Clientes.Add(cliente);
            await _context.SaveChangesAsync();
            return cliente; // Devuelve el cliente con el ID ya asignado
        }
        public async Task<bool> DoesDocumentoExistAsync(string documento)
        {
            // Si el documento no es nulo o vacío, busca si algún cliente ya lo tiene
            if (!string.IsNullOrEmpty(documento))
            {
                return await _context.Clientes.AnyAsync(c => c.Documento == documento);
            }
            return false;
        }

        public async Task<bool> DoesTelefonoExistAsync(string telefono)
        {
            // Si el teléfono no es nulo o vacío, busca si algún cliente ya lo tiene
            if (!string.IsNullOrEmpty(telefono))
            {
                return await _context.Clientes.AnyAsync(c => c.Telefono == telefono);
            }
            return false;
        }
    }
}
    