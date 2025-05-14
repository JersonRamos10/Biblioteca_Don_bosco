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
    private int idTipoUsuario;      // En la BD es 'id_tipo_usuario' (INT, FK)
    private boolean estado;         // Corresponde a 'estado' (TINYINT(1)). true si es 1 (activo), false si es 0 (inactivo).

    /**
     * Constructor vacío.
     */
    public Usuario() {
    }

    // Constructor completo
    public Usuario(int id, String nombre, String correo, String contrasena, int idTipoUsuario, boolean estado) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.idTipoUsuario = idTipoUsuario;
        this.estado = estado;
    }


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

    public int getIdTipoUsuario() {
        return idTipoUsuario;
    }

    public void setIdTipoUsuario(int idTipoUsuario) {
        this.idTipoUsuario = idTipoUsuario;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
        if (tipoUsuario != null) {
            this.idTipoUsuario = tipoUsuario.getId();
        }
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Usuario{" +
               "id=" + id +
               ", nombre='" + nombre + '\'' +
               ", correo='" + correo + '\'' +
               ", idTipoUsuario=" + idTipoUsuario +
               ", tipoUsuario=" + (tipoUsuario != null ? tipoUsuario.getTipo() : "N/A") +
               ", estado=" + estado +
               '}';
    }
    
}
