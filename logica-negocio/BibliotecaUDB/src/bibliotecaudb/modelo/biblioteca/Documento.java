/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.modelo.biblioteca;

/**
 *
 * @author jerson_ramos
 */

//Modelo para la tabla documentos
public class Documento {
    
    private int id;
    private String titulo;
    private String autor;
    private String editorial;
    private int añoPublicacion;
    private TipoDocumento tipoDocumento; //referencia al tipo asociado
    
    //constructor vacio
    public Documento(){
        
    }
    
    //constructor con parametros
    public Documento (int id, String titulo, String autor, String editorial, int añoPublicacion, TipoDocumento tipoDocumento){
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.añoPublicacion = añoPublicacion;
        this.tipoDocumento = tipoDocumento;
    }
    
    //getters y setters
    public int getId(){
        return id;
    }
    
    public void setId(int id){
        this.id = id;
    }
    
    public String getTitulo(){
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

    public int getAnioPublicacion() {
        return añoPublicacion;
    }

    public void setAnioPublicacion(int anioPublicacion) {
        this.añoPublicacion = anioPublicacion;
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
                ", editorial='" + editorial + '\'' +
                ", anioPublicacion=" + añoPublicacion +
                ", tipoDocumento=" + (tipoDocumento != null ? tipoDocumento.getTipo() : "N/A") +
                '}';
    }
     
}   
