package bibliotecaudb.modelo.usuario;

public class TipoUsuario {
    private int id;
    private String tipo;

    //constructor vacio
    public TipoUsuario() {
    }

    public TipoUsuario(int id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    @Override
    public String toString() {
        return "TipoUsuario{" + "id=" + id + ", tipo='" + tipo + '\'' + '}';
    }
}