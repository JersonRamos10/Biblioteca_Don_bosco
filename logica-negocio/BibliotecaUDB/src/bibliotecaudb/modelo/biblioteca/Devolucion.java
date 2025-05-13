package bibliotecaudb.modelo.biblioteca; 

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Objects;

/**
 * Modelo para representar la tabla 'devoluciones'.
 */
public class Devolucion {
    private int id;                 // Corresponde a 'id' (INT, PK, AI)
    private Prestamo prestamo;      // Objeto Prestamo (FK id_prestamo)
    private Date fechaDevolucion;   // Corresponde a 'fecha_devolucion' (DATE)
    private BigDecimal moraPagada;  // Corresponde a 'mora_pagada' 

    // Constructores
    public Devolucion() {
    }

    public Devolucion(int id, Prestamo prestamo, Date fechaDevolucion, BigDecimal moraPagada) {
        this.id = id;
        this.prestamo = prestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.moraPagada = moraPagada; 
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    public Date getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(Date fechaDevolucion) {
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
               ", idPrestamo=" + (prestamo != null ? prestamo.getId() : "N/A") +
               ", fechaDevolucion=" + fechaDevolucion +
               ", moraPagada=" + moraPagada +
               '}';
    }

    // --- equals() y hashCode() generados por el IDE (basados en 'id') ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Devolucion that = (Devolucion) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}