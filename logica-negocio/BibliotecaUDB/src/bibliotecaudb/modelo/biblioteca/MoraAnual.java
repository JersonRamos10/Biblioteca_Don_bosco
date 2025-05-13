package bibliotecaudb.modelo.biblioteca; 

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Modelo para representar la tabla 'mora_anual'.
 */
public class MoraAnual {
    private int anio;               // Corresponde a 'anio' (INT, PK)
    private BigDecimal moraDiaria;  // Corresponde a 'mora_diaria' 

    // Constructores
    public MoraAnual() {
    }

    public MoraAnual(int anio, BigDecimal moraDiaria) {
        this.anio = anio;
        this.moraDiaria = moraDiaria;
    }

    // Getters y Setters
    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public BigDecimal getMoraDiaria() {
        return moraDiaria;
    }

    public void setMoraDiaria(BigDecimal moraDiaria) {
        this.moraDiaria = moraDiaria;
    }

    @Override
    public String toString() {
        return "MoraAnual{" +
               "anio=" + anio +
               ", moraDiaria=" + moraDiaria +
               '}';
    }

    // --- equals() y hashCode() generados por el IDE (basados en 'anio') ---
    // 'anio' es la clave primaria de esta tabla.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoraAnual moraAnual = (MoraAnual) o;
        return anio == moraAnual.anio;
    }

    @Override
    public int hashCode() {
        return Objects.hash(anio);
    }
}