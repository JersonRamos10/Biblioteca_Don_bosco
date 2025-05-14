package bibliotecaudb.modelo.biblioteca;

import bibliotecaudb.modelo.usuario.TipoUsuario;

/**
 * Representa las políticas de préstamo asociadas a un tipo de usuario.
 * Corresponde a la tabla 'politicas_prestamo'.
 */
public class PoliticasPrestamo {
    private int idPolitica;
    private int idTipoUsuario;
    private TipoUsuario tipoUsuario; // Objeto anidado para referencia
    private int maxEjemplaresPrestamo;
    private int diasPrestamoDefault;

    public PoliticasPrestamo() {
    }

    public int getIdPolitica() {
        return idPolitica;
    }

    public void setIdPolitica(int idPolitica) {
        this.idPolitica = idPolitica;
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

    public int getMaxEjemplaresPrestamo() {
        return maxEjemplaresPrestamo;
    }

    public void setMaxEjemplaresPrestamo(int maxEjemplaresPrestamo) {
        this.maxEjemplaresPrestamo = maxEjemplaresPrestamo;
    }

    public int getDiasPrestamoDefault() {
        return diasPrestamoDefault;
    }

    public void setDiasPrestamoDefault(int diasPrestamoDefault) {
        this.diasPrestamoDefault = diasPrestamoDefault;
    }

    @Override
    public String toString() {
        return "PoliticasPrestamo{" +
               "idPolitica=" + idPolitica +
               ", idTipoUsuario=" + idTipoUsuario +
               ", tipoUsuario=" + (tipoUsuario != null ? tipoUsuario.getTipo() : "N/A") +
               ", maxEjemplaresPrestamo=" + maxEjemplaresPrestamo +
               ", diasPrestamoDefault=" + diasPrestamoDefault +
               '}';
    }
}