using complejoDeportivo.DTOs;
using complejoDeportivo.Models;
using complejoDeportivo.Repositories.Interfaces;
using complejoDeportivo.Services.Interfaces;
using BCrypt.Net;
using Microsoft.EntityFrameworkCore;

namespace complejoDeportivo.Services.Implementations
{
    public class UsuarioService : IUsuarioService
    {
        private readonly IUsuarioRepository _usuarioRepo;
        private readonly IClienteRepository _clienteRepo;

        public UsuarioService(IUsuarioRepository usuarioRepository, IClienteRepository clienteRepository)
        {
            _usuarioRepo = usuarioRepository;
            _clienteRepo = clienteRepository;
        }

        public async Task<IEnumerable<UsuarioDTO>> GetAllAsync()
        {
            var usuarios = await _usuarioRepo.GetAllAsync();
            return usuarios.Select(u => new UsuarioDTO
            {
                UsuarioId = u.UsuarioId,
                Email = u.Email,
                TipoUsuario = u.TipoUsuario,
                ClienteId = u.ClienteId,
                EmpleadoId = u.EmpleadoId
            });
        }

        public async Task<UsuarioDTO> GetByIdAsync(int id)
        {
            var u = await _usuarioRepo.GetByIdAsync(id);
            if (u == null)
            {
                throw new NotFoundException($"Usuario con ID {id} no encontrado.");
            }
            return new UsuarioDTO
            {
                UsuarioId = u.UsuarioId,
                Email = u.Email,
                TipoUsuario = u.TipoUsuario,
                ClienteId = u.ClienteId,
                EmpleadoId = u.EmpleadoId
            };
        }

        public async Task<UsuarioDTO> CreateAsync(CreateUsuarioDTO createDto)
        {
            string passwordHash = BCrypt.Net.BCrypt.HashPassword(createDto.Password);

            var usuario = new Usuario
            {
                Email = createDto.Email,
                PasswordHash = passwordHash,
                TipoUsuario = createDto.TipoUsuario,
                ClienteId = createDto.ClienteId,
                EmpleadoId = createDto.EmpleadoId,
                FechaRegistro = DateTime.UtcNow
            };

            var nuevoUsuario = await _usuarioRepo.CreateAsync(usuario);

            return new UsuarioDTO
            {
                UsuarioId = nuevoUsuario.UsuarioId,
                Email = nuevoUsuario.Email,
                TipoUsuario = nuevoUsuario.TipoUsuario,
                ClienteId = nuevoUsuario.ClienteId,
                EmpleadoId = nuevoUsuario.EmpleadoId
            };
        }

        public async Task UpdateAsync(int id, UsuarioDTO updateDto)
        {
            var usuario = await _usuarioRepo.GetByIdAsync(id);
            if (usuario == null)
            {
                throw new NotFoundException($"Usuario con ID {id} no encontrado.");
            }

            usuario.Email = updateDto.Email;
            usuario.TipoUsuario = updateDto.TipoUsuario;
            usuario.ClienteId = updateDto.ClienteId;
            usuario.EmpleadoId = updateDto.EmpleadoId;

            await _usuarioRepo.UpdateAsync(usuario);
        }

        public async Task DeleteAsync(int id)
        {
            var usuario = await _usuarioRepo.GetByIdAsync(id);
            if (usuario == null)
            {
                throw new NotFoundException($"Usuario con ID {id} no encontrado.");
            }
            await _usuarioRepo.DeleteAsync(id);
        }

        public async Task<UsuarioDTO> RegisterClienteAsync(RegisterClienteDTO dto)
        {
            if (await _usuarioRepo.GetByEmailAsync(dto.Email) != null)
            {
                throw new System.Exception("El email ya está registrado.");
            }
            if (await _clienteRepo.DoesDocumentoExistAsync(dto.Documento))
            {
                throw new System.Exception("El documento ya se encuentra registrado.");
            }

            if (await _clienteRepo.DoesTelefonoExistAsync(dto.Telefono))
            {
                throw new System.Exception("El número de teléfono ya se encuentra registrado.");
            }

            var nuevoCliente = new Cliente
            {
                Nombre = dto.Nombre,
                Apellido = dto.Apellido,
                Email = dto.Email,
                Telefono = dto.Telefono,
                Documento = dto.Documento,
                FechaRegistro = DateTime.UtcNow
            };
            var clienteCreado = await _clienteRepo.CreateAsync(nuevoCliente);

            string passwordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password);

            var nuevoUsuario = new Usuario
            {
                Email = dto.Email,
                PasswordHash = passwordHash,
                TipoUsuario = "Cliente",
                ClienteId = clienteCreado.ClienteId,
                EmpleadoId = null,
                FechaRegistro = DateTime.UtcNow
            };
            var usuarioCreado = await _usuarioRepo.CreateAsync(nuevoUsuario);

            return new UsuarioDTO
            {
                UsuarioId = usuarioCreado.UsuarioId,
                Email = usuarioCreado.Email,
                TipoUsuario = usuarioCreado.TipoUsuario,
                ClienteId = usuarioCreado.ClienteId,
                EmpleadoId = usuarioCreado.EmpleadoId
            };
        }
    }
}