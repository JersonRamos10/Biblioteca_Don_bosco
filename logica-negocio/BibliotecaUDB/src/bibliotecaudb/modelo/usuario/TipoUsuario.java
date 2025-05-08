/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.modelo.usuario;

/**
 *
 * @author jerson_ramos
 */
public class TipoUsuario {
    private int id;        //columna 'id' (INT, PK)
    private String tipo;   //columna 'tipo' (VARCHAR(100))

    /**
     * Constructor vacío.
     */
    public TipoUsuario() {
    }

    /**
     * Constructor para crear un TipoUsuario con un ID y un tipo.
     * @param id El identificador único del tipo de usuario.
     * @param tipo El nombre del tipo de usuario ("Administrador", "Profesor").
     */
    public TipoUsuario(int id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    // --- Getters y Setters ---

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

    // --- Métodos adicionales (opcional) ---

    /**
     * Representación en String del objeto TipoUsuario.
     * @return Una cadena que representa el objeto.
     */
    @Override
    public String toString() {
        return "TipoUsuario{" +
               "id=" + id +
               ", tipo='" + tipo + '\'' +
               '}';
    }
}
