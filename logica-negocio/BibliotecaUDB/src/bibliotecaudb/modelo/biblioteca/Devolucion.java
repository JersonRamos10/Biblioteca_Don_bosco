package bibliotecaudb.modelo.biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa la devolucion de un prestamo.
 * Corresponde a la tabla 'devoluciones'.
 */
public class Devolucion {
    private int id;
    private int idPrestamo;
    private Prestamo prestamo; 
    private LocalDate fechaDevolucion;
    private BigDecimal moraPagada; // Puede ser NULL o 0.00

    public Devolucion() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
        if (prestamo != null) {
            this.idPrestamo = prestamo.getId();
        }
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public BigDecimal getMoraPagada() {
        return moraPagada;
    }

    public void setMoraPagada(BigDecimal moraPagada) {
        this.moraPagada = moraPagada;
    }

    @Override
    public String toString() {
        return "Devolucion{" +
               "id=" + id +
               ", idPrestamo=" + idPrestamo +
               ", fechaDevolucion=" + fechaDevolucion +
               ", moraPagada=" + moraPagada +
               '}';
    }
}