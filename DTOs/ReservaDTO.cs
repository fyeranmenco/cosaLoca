namespace complejoDeportivo.DTOs
{
    public class ReservaDTO
    {
        public int ReservaId { get; set; }
        public int ClienteId { get; set; }
        public int CanchaId { get; set; }
        public DateOnly Fecha { get; set; }
        public TimeOnly HoraInicio { get; set; }
        public TimeOnly HoraFin { get; set; }
        public decimal Total { get; set; }
        public required string Estado { get; set; }
        public DateTime FechaCreacion { get; set; }
    }
}
