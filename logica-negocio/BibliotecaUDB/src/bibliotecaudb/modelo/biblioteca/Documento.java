package bibliotecaudb.modelo.biblioteca;

import java.util.Objects;

/**
 * Modelo para representar la tabla 'documentos'.
 */
public class Documento {
    private int id;
    private String titulo;
    private String autor;
    private String editorial;
    private Integer anioPublicacion;
    private TipoDocumento tipoDocumento;

    // Constructores
    public Documento() {
    }

    public Documento(int id, String titulo, String autor, String editorial, Integer anioPublicacion, TipoDocumento tipoDocumento) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
        this.tipoDocumento = tipoDocumento;
    }

    // Getters y Setters 
    public int getId() { 
        return id; 
    }
    public void setId(int id) {
        this.id = id; 
    }
    public String getTitulo() {
        return titulo; 
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public String getAutor() {
        return autor;
    }
    public void setAutor(String autor) {
        this.autor = autor; 
    }
    public String getEditorial() {
        return editorial; 
    }
    public void setEditorial(String editorial) {
        this.editorial = editorial; 
    }
    public Integer getAnioPublicacion() {
        return anioPublicacion; 
    }
    public void setAnioPublicacion(Integer anioPublicacion) {
        this.anioPublicacion = anioPublicacion; 
    }
    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { 
        this.tipoDocumento = tipoDocumento; 
    }

    
    @Override
    public String toString() {
        return "Documento{" +
               "id=" + id +
               ", titulo='" + titulo + '\'' +
               ", autor='" + autor + '\'' +
               ", tipo=" + (tipoDocumento != null ? tipoDocumento.getTipo() : "N/A") +
               '}';
    }

    // --- equals() y hashCode() generados por el IDE (basados en 'id') ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Documento documento = (Documento) o;
        return id == documento.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}