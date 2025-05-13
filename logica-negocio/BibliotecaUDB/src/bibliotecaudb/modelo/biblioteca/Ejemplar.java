package bibliotecaudb.modelo.biblioteca;

import java.util.Objects;

/**
 * Modelo para representar la tabla 'ejemplares'.
 */
public class Ejemplar {

    public enum EstadoEjemplar {
        DISPONIBLE("Disponible"),
        PRESTADO("Prestado");
       
        private final String dbValue;
        EstadoEjemplar(String dbValue) { this.dbValue = dbValue; }
        public String getDbValue() { return dbValue; }
        public static EstadoEjemplar fromDbValue(String value) {
            for (EstadoEjemplar estado : values()) {
                if (estado.dbValue.equalsIgnoreCase(value)) return estado;
            }
            return null;
        }
    }

    private int id;
    private Documento documento;
    private String ubicacion;
    private EstadoEjemplar estado;

  //constructores
    public Ejemplar() {
        this.estado = EstadoEjemplar.DISPONIBLE; 
    }
    
    public Ejemplar(int id, Documento documento, String ubicacion, EstadoEjemplar estado) {
        this.id = id;
        this.documento = documento;
        this.ubicacion = ubicacion;
        this.estado = estado;
    }

    // Getters y Setters 
    public int getId() {
        return id; 
    }
    public void setId(int id) {
        this.id = id; 
    }
    public Documento getDocumento() {
        return documento; 
    }
    public void setDocumento(Documento documento) {
        this.documento = documento; 
    }
    public String getUbicacion() {
        return ubicacion; 
    }
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion; 
    }
    public EstadoEjemplar getEstado() {
        return estado;
    }
    public void setEstado(EstadoEjemplar estado) {
        this.estado = estado; 
    }


    @Override
    public String toString() {
        return "Ejemplar{" +
               "id=" + id +
               ", documento=" + (documento != null ? documento.getTitulo() : "N/A") +
               ", ubicacion='" + ubicacion + '\'' +
               ", estado=" + estado +
               '}';
    }

    // --- equals() y hashCode() generados por el IDE (basados en 'id') ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ejemplar ejemplar = (Ejemplar) o;
        return id == ejemplar.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}