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
}