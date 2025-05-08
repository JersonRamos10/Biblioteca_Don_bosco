/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.modelo.usuario;

/**
 *
 * @author jerson_ramos
 */
public class Usuario {
    
     private int id;                 // Corresponde a 'id' (INT, PK)
    private String nombre;          // Corresponde a 'nombre' (VARCHAR(100))
    private String correo;          // Corresponde a 'correo' (VARCHAR(100))
    private String contrasena;      // Corresponde a 'contrasena' (VARCHAR(255))
    private TipoUsuario tipoUsuario; // Objeto para representar la relación con 'tipo_usuario'
                                    // En la BD es 'id_tipo_usuario' (INT, FK)
    private boolean estado;         // Corresponde a 'estado' (TINYINT(1)). true si es 1 (activo), false si es 0 (inactivo).

    /**
     * Constructor vacío.
     */
    public Usuario() {
    }

    /**
     * Constructor para crear un Usuario con todos sus atributos.
     * @param id El ID del usuario.
     * @param nombre El nombre completo del usuario.
     * @param correo El correo electrónico del usuario.
     * @param contrasena La contraseña del usuario (en un sistema real, sería un hash).
     * @param tipoUsuario El objeto TipoUsuario asociado a este usuario.
     * @param estado El estado del usuario (activo/inactivo).
     */
    public Usuario(int id, String nombre, String correo, String contrasena, TipoUsuario tipoUsuario, boolean estado) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.tipoUsuario = tipoUsuario;
        this.estado = estado;
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public boolean isEstado() { 
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    // --- Métodos adicionales ---

    @Override
    public String toString() {
        return "Usuario{" +
               "id=" + id +
               ", nombre='" + nombre + '\'' +
               ", correo='" + correo + '\'' +
               // Por seguridad, generalmente no se incluye la contraseña en toString()
               ", tipoUsuario=" + (tipoUsuario != null ? tipoUsuario.getTipo() : "N/A") +
               ", estado=" + estado +
               '}';
    }
    
}
