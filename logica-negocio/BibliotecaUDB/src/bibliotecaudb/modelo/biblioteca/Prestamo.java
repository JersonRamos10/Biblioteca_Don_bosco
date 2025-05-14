package bibliotecaudb.modelo.biblioteca;

import bibliotecaudb.modelo.usuario.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate; // Usaremos LocalDate para las fechas

/**
 * Representa un préstamo de un ejemplar a un usuario.
 * Corresponde a la tabla 'prestamos'.
 */
public class Prestamo {
    private int id;
    private int idUsuario;
    private Usuario usuario; // Objeto Usuario anidado
    private int idEjemplar;
    private Ejemplar ejemplar; // Objeto Ejemplar anidado
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion; // Puede ser NULL si aún no se ha devuelto
    private LocalDate fechaLimite;
    private BigDecimal mora;         // Puede ser NULL o 0.00

    public Prestamo() {
        // Inicializar mora a 0.00 por defecto si es un nuevo préstamo
        this.mora = BigDecimal.ZERO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            this.idUsuario = usuario.getId();
        }
    }

    public int getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(int idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public Ejemplar getEjemplar() {
        return ejemplar;
    }

    public void setEjemplar(Ejemplar ejemplar) {
        this.ejemplar = ejemplar;
        if (ejemplar != null) {
            this.idEjemplar = ejemplar.getId();
        }
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public BigDecimal getMora() {
        return mora;
    }

    public void setMora(BigDecimal mora) {
        this.mora = mora;
    }

    /**
     * Indica si el prestamo esta actualmente activo (no devuelto).
     * @return true si fechaDevolucion es NULL, false en caso contrario.
     */
    public boolean isActivo() {
        return this.fechaDevolucion == null;
    }

    @Override
    public String toString() {
        return "Prestamo{" +
               "id=" + id +
               ", idUsuario=" + idUsuario +
               ", usuario=" + (usuario != null ? usuario.getNombre() : "N/A") +
               ", idEjemplar=" + idEjemplar +
               ", ejemplarTitulo=" + (ejemplar != null && ejemplar.getDocumento() != null ? ejemplar.getDocumento().getTitulo() : "N/A") +
               ", fechaPrestamo=" + fechaPrestamo +
               ", fechaDevolucion=" + fechaDevolucion +
               ", fechaLimite=" + fechaLimite +
               ", mora=" + mora +
               '}';
    }
}