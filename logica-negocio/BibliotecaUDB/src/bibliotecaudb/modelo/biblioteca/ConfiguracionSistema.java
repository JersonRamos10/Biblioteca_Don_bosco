package bibliotecaudb.modelo.biblioteca;

import java.math.BigDecimal;

/**
 * Representa la configuración global del sistema.
 * Corresponde a la tabla 'configuracion_sistema'.
 * NOTA: maximo_ejemplares ahora se gestiona principalmente por 'politicas_prestamo'.
 * Esta clase puede mantener mora_diaria global u otros parámetros futuros.
 */
public class ConfiguracionSistema {
    private int id;
    private Integer maximoEjemplaresGlobal; // Podría ser un fallback o no usarse si todo es por tipo_usuario
    private BigDecimal moraDiariaGlobal;

    public ConfiguracionSistema() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getMaximoEjemplaresGlobal() {
        return maximoEjemplaresGlobal;
    }

    public void setMaximoEjemplaresGlobal(Integer maximoEjemplaresGlobal) {
        this.maximoEjemplaresGlobal = maximoEjemplaresGlobal;
    }

    public BigDecimal getMoraDiariaGlobal() {
        return moraDiariaGlobal;
    }

    public void setMoraDiariaGlobal(BigDecimal moraDiariaGlobal) {
        this.moraDiariaGlobal = moraDiariaGlobal;
    }

    @Override
    public String toString() {
        return "ConfiguracionSistema{" +
               "id=" + id +
               ", maximoEjemplaresGlobal=" + maximoEjemplaresGlobal +
               ", moraDiariaGlobal=" + moraDiariaGlobal +
               '}';
    }
}