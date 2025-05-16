package bibliotecaudb.servicios;

import bibliotecaudb.modelo.biblioteca.Prestamo;
import bibliotecaudb.modelo.biblioteca.MoraAnual;
import bibliotecaudb.modelo.biblioteca.ConfiguracionSistema;
import bibliotecaudb.excepciones.BibliotecaException; 

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface MoraService {

    /**
     * Obtiene la tasa de mora diaria aplicable para una fecha dada.
     * Primero busca en mora_anual para el año de la fecha, si no, usa la mora_diaria global.
     * @param fechaParaCalculo La fecha para la cual se necesita la tasa de mora (usualmente la fecha limite del presstamo).
     * @return La tasa de mora diaria aplicable.
     * @throws SQLException Si hay un error de BD.
     * @throws BibliotecaException Si no se encuentra ninguna configuración de mora aplicable.
     */
    BigDecimal obtenerTasaMoraDiariaAplicable(LocalDate fechaParaCalculo) throws SQLException, BibliotecaException;

    /**
     * Calcula la mora acumulada para un prestamo hasta una fecha de devolucion especifica.
     * @param prestamo El objeto Prestamo.
     * @param fechaDevolucionActual La fecha en que se esta realizando/simulando la devolución.
     * @return La mora calculada. Si no hay mora, devuelve BigDecimal.ZERO.
     * @throws SQLException Si hay un error de BD.
     * @throws BibliotecaException Si no se puede determinar la tasa de mora.
     */
    BigDecimal calcularMoraParaPrestamo(Prestamo prestamo, LocalDate fechaDevolucionActual) throws SQLException, BibliotecaException;

    // --- Gestion de Configuracion de Mora ---
    List<MoraAnual> obtenerTodasLasMorasAnuales() throws SQLException;
    MoraAnual obtenerMoraPorAnio(int anio) throws SQLException;
    boolean guardarMoraAnual(MoraAnual moraAnual) throws SQLException, BibliotecaException; // Inserta o actualiza
    boolean eliminarMoraAnual(int anio) throws SQLException;

    ConfiguracionSistema obtenerConfiguracionGlobalSistema() throws SQLException;
    boolean actualizarConfiguracionGlobalSistema(ConfiguracionSistema config) throws SQLException, BibliotecaException;
}