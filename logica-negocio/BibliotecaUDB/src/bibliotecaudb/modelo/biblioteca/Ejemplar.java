package bibliotecaudb.modelo.biblioteca;

import bibliotecaudb.conexion.LogsError; // Para el warning en setEstado

/**
 * Representa un ejemplar físico de un documento en la biblioteca.
 * Corresponde a la tabla 'ejemplares'.
 */
public class Ejemplar {
    private int id;
    private int idDocumento;
    private Documento documento; // Objeto Documento anidado
    private String ubicacion;    // Puede ser NULL
    private String estado;       // Enum en BD ('Disponible', 'Prestado'), String en Java

    public static final String ESTADO_DISPONIBLE = "Disponible";
    public static final String ESTADO_PRESTADO = "Prestado";


    public Ejemplar() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(int idDocumento) {
        this.idDocumento = idDocumento;
    }

    public Documento getDocumento() {
        return documento;
    }

    public void setDocumento(Documento documento) {
        this.documento = documento;
        if (documento != null) {
            this.idDocumento = documento.getId();
        }
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        // Validar que el estado sea uno de los permitidos
        if (ESTADO_DISPONIBLE.equals(estado) || ESTADO_PRESTADO.equals(estado)) {
            this.estado = estado;
        } else {
            LogsError.warn(Ejemplar.class, "Estado de ejemplar inválido: '" + estado + "'. Establecido a " + ESTADO_DISPONIBLE + " por defecto.");
            this.estado = ESTADO_DISPONIBLE; // Default o manejar error más estrictamente
        }
    }

    @Override
    public String toString() {
        return "Ejemplar{" +
               "id=" + id +
               ", idDocumento=" + idDocumento +
               ", tituloDocumento=" + (documento != null && documento.getTitulo() != null ? "'" + documento.getTitulo() + "'" : "N/A") +
               ", ubicacion='" + (ubicacion != null ? ubicacion : "N/A") + '\'' +
               ", estado='" + estado + '\'' +
               '}';
    }
}