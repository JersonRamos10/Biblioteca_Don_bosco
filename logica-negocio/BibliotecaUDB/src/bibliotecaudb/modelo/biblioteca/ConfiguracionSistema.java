package bibliotecaudb.modelo.biblioteca; 

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Modelo para representar la tabla 'configuracion_sistema'.
 */
public class ConfiguracionSistema {
    private int id;                     // Corresponde a 'id' (INT, PK, AI)
    private Integer maximoEjemplares;   // Corresponde a 'maximo_ejemplares' (INT) 
    
    // BigDecimal para mora_diaria para evitar problemas de precision con double/float
    private BigDecimal moraDiaria;      // Corresponde a 'mora_diaria' 

    // Constructores
    public ConfiguracionSistema() {
    }

    public ConfiguracionSistema(int id, Integer maximoEjemplares, BigDecimal moraDiaria) {
        this.id = id;
        this.maximoEjemplares = maximoEjemplares;
        this.moraDiaria = moraDiaria;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getMaximoEjemplares() {
        return maximoEjemplares;
    }

    public void setMaximoEjemplares(Integer maximoEjemplares) {
        this.maximoEjemplares = maximoEjemplares;
    }

    public BigDecimal getMoraDiaria() {
        return moraDiaria;
    }

    public void setMoraDiaria(BigDecimal moraDiaria) {
        this.moraDiaria = moraDiaria;
    }

    @Override
    public String toString() {
        return "ConfiguracionSistema{" +
               "id=" + id +
               ", maximoEjemplares=" + maximoEjemplares +
               ", moraDiaria=" + moraDiaria +
               '}';
    }

    // --- equals() y hashCode() generados por el IDE (basados en 'id') ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfiguracionSistema that = (ConfiguracionSistema) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}