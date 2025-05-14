package bibliotecaudb.modelo.biblioteca;


/**
 * Modelo para representar la tabla 'documentos'.
 */
public class Documento {
    private int id;
    private String titulo;
    private String autor;
    private String editorial;
    private Integer anioPublicacion;
    private int idTipoDocumento;
    private TipoDocumento tipoDocumento;

    // Constructores
    public Documento() {
    }

    //getters y setters
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

    public int getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(int idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
        if (tipoDocumento != null) {
            this.idTipoDocumento = tipoDocumento.getId();
        }
    }

    @Override
    public String toString() {
        return "Documento{" +
               "id=" + id +
               ", titulo='" + titulo + '\'' +
               ", autor='" + (autor != null ? autor : "N/A") + '\'' +
               ", editorial='" + (editorial != null ? editorial : "N/A") + '\'' +
               ", anioPublicacion=" + (anioPublicacion != null ? anioPublicacion : "N/A") +
               ", idTipoDocumento=" + idTipoDocumento +
               ", tipoDocumento=" + (tipoDocumento != null ? tipoDocumento.getTipo() : "N/A") +
               '}';
    }
}