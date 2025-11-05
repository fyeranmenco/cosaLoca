namespace complejoDeportivo.DTOs
{
    public class CrearReservaDTO
    {
        public int ClienteId { get; set; }
        public int CanchaId { get; set; }
        public DateOnly Fecha { get; set; } // date part used
        public TimeOnly HoraInicio { get; set; } // e.g. 18:00
        public TimeOnly HoraFin { get; set; }    // must be HoraInicio + N * 1h
        public string Ambito { get; set; } = "Web";
    }
}
