package bibliotecaudb.modelo.biblioteca; 

import java.math.BigDecimal;


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

}