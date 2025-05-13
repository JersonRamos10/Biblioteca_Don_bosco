package bibliotecaudb.modelo.biblioteca; 

// Importa los otros modelos necesarios
import bibliotecaudb.modelo.usuario.Usuario;
import java.math.BigDecimal; // Para la mora, que es DECIMAL en la BD
import java.sql.Date;        // Usamos java.sql.Date para mapear DATE de SQL
import java.util.Objects;

/**
 * Modelo para representar la tabla 'prestamos'.
 */
public class Prestamo {
    private int id;                 // Corresponde a 'id' (INT, PK, AI)
    private Usuario usuario;        // Objeto Usuario (FK id_usuario)
    private Ejemplar ejemplar;      // Objeto Ejemplar (FK id_ejemplar)
    private Date fechaPrestamo;     // Corresponde a 'fecha_prestamo' (DATE)
    private Date fechaDevolucion;   // Corresponde a 'fecha_devolucion' (DATE), puede ser NULL
    private Date fechaLimite;       // Corresponde a 'fecha_limite' (DATE)
    private BigDecimal mora;        // Corresponde a 'mora' (DECIMAL(10,2)), por defecto 0.00

    // Constructores
    public Prestamo() {
        // Valor por defecto para mora
        this.mora = BigDecimal.ZERO;
    }

    public Prestamo(int id, Usuario usuario, Ejemplar ejemplar, Date fechaPrestamo, Date fechaDevolucion, Date fechaLimite, BigDecimal mora) {
        this.id = id;
        this.usuario = usuario;
        this.ejemplar = ejemplar;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.fechaLimite = fechaLimite;
        this.mora = (mora != null) ? mora : BigDecimal.ZERO;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Ejemplar getEjemplar() {
        return ejemplar;
    }

    public void setEjemplar(Ejemplar ejemplar) {
        this.ejemplar = ejemplar;
    }

    public Date getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(Date fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public Date getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(Date fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public Date getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(Date fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public BigDecimal getMora() {
        return mora;
    }

    public void setMora(BigDecimal mora) {
        this.mora = (mora != null) ? mora : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "Prestamo{" +
               "id=" + id +
               ", usuario=" + (usuario != null ? usuario.getNombre() : "N/A") +
               ", ejemplar=" + (ejemplar != null && ejemplar.getDocumento() != null ? ejemplar.getDocumento().getTitulo() : "N/A") +
               ", fechaPrestamo=" + fechaPrestamo +
               ", fechaLimite=" + fechaLimite +
               ", mora=" + mora +
               '}';
    }

    // --- equals() y hashCode() generados por el IDE (basados en 'id') ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prestamo prestamo = (Prestamo) o;
        return id == prestamo.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}