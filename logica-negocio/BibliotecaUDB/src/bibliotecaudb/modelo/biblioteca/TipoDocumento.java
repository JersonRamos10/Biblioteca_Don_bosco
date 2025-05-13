/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.modelo.biblioteca;

import java.util.Objects; 

/**
 * Modelo para representar la tabla 'tipo_documento'.
 */
public class TipoDocumento {
    private int id;
    private String tipo;

    // Constructores
    public TipoDocumento() {
    }

    public TipoDocumento(int id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "TipoDocumento{" +
               "id=" + id +
               ", tipo='" + tipo + '\'' +
               '}';
    }

    // --- equals() y hashCode() generados por el IDE (basados en 'id') ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Si es la misma instancia, son iguales
        if (o == null || getClass() != o.getClass()) return false; 
        TipoDocumento that = (TipoDocumento) o; // cast seguro
        return id == that.id; // Comparamos por el campo 'id'
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Genera un hash basado en el campo 'id'
    }
}