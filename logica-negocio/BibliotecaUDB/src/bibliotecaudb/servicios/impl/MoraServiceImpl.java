package bibliotecaudb.servicios.impl;

import bibliotecaudb.modelo.biblioteca.Prestamo;
import bibliotecaudb.modelo.biblioteca.MoraAnual;
import bibliotecaudb.modelo.biblioteca.ConfiguracionSistema;
import bibliotecaudb.dao.biblioteca.MoraAnualDAO;
import bibliotecaudb.dao.biblioteca.ConfiguracionSistemaDAO;
import bibliotecaudb.dao.biblioteca.impl.MoraAnualDAOImpl;
import bibliotecaudb.dao.biblioteca.impl.ConfiguracionSistemaDAOImpl;
import bibliotecaudb.servicios.MoraService;
import bibliotecaudb.excepciones.BibliotecaException;
import bibliotecaudb.conexion.LogsError;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MoraServiceImpl implements MoraService {

    private final MoraAnualDAO moraAnualDAO; // Objeto para acceder a las moras anuales
    private final ConfiguracionSistemaDAO configuracionSistemaDAO; // Objeto para acceder a la configuracion del sistema

    public MoraServiceImpl() {
        this.moraAnualDAO = new MoraAnualDAOImpl(); // Creamos el objeto para mora anual
        this.configuracionSistemaDAO = new ConfiguracionSistemaDAOImpl(); // Creamos el objeto para configuracion
    }

    // Constructor para pasarle los manejadores 
    public MoraServiceImpl(MoraAnualDAO moraAnualDAO, ConfiguracionSistemaDAO configuracionSistemaDAO) {
        this.moraAnualDAO = moraAnualDAO;
        this.configuracionSistemaDAO = configuracionSistemaDAO;
    }

    @Override
    public BigDecimal obtenerTasaMoraDiariaAplicable(LocalDate fechaParaCalculo) throws SQLException, BibliotecaException {
        // Este metodo nos dice que tasa de mora diaria se debe usar para una fecha especifica.
        if (fechaParaCalculo == null) {
            throw new IllegalArgumentException("La fecha para calculo de mora no puede ser nula.");
        }
        int anio = fechaParaCalculo.getYear(); // Obtenemos el anio de la fecha
        MoraAnual configMoraAnual = moraAnualDAO.obtenerPorAnio(anio); // Buscamos la mora para ese anio
        BigDecimal moraDiariaAplicable; // Variable para guardar la mora que se aplicara

        if (configMoraAnual != null && configMoraAnual.getMoraDiaria() != null) {
            moraDiariaAplicable = configMoraAnual.getMoraDiaria(); // Usamos la mora de ese anio
            LogsError.info(this.getClass(), "Tasa de mora anual (" + anio + ") encontrada: " + moraDiariaAplicable);
        } else {
            // Si no hay mora para ese anio, usamos la mora global del sistema
            ConfiguracionSistema configGlobal = configuracionSistemaDAO.obtenerConfiguracion();
            if (configGlobal != null && configGlobal.getMoraDiariaGlobal() != null) {
                moraDiariaAplicable = configGlobal.getMoraDiariaGlobal();
                LogsError.warn(this.getClass(), "Usando tasa de mora diaria global: " + moraDiariaAplicable + " (no encontrada para el anio " + anio + ")");
            } else {
                LogsError.error(this.getClass(), "No se encontro configuracion de mora diaria para el anio " + anio + " ni configuracion global.");
                throw new BibliotecaException("No hay configuracion de mora diaria disponible para calcular.");
            }
        }
        return moraDiariaAplicable; // Devolvemos la mora diaria a aplicar
    }

    @Override
    public BigDecimal calcularMoraParaPrestamo(Prestamo prestamo, LocalDate fechaDevolucionActual) throws SQLException, BibliotecaException {
        // Este metodo calcula la mora total para un prestamo hasta una fecha de devolucion.
        if (prestamo == null || prestamo.getFechaLimite() == null || fechaDevolucionActual == null) {
            throw new IllegalArgumentException("Datos de prestamo o fecha de devolucion invalidos para calcular mora.");
        }

        BigDecimal moraCalculada = BigDecimal.ZERO; // Empezamos con cero mora

        if (fechaDevolucionActual.isAfter(prestamo.getFechaLimite())) { // Si la devolucion es despues de la fecha limite
            long diasDeMora = ChronoUnit.DAYS.between(prestamo.getFechaLimite(), fechaDevolucionActual); // Calculamos los dias de retraso
            if (diasDeMora > 0) {
                // La tasa de mora se toma segun la fecha limite del prestamo
                BigDecimal tasaMoraDiaria = obtenerTasaMoraDiariaAplicable(prestamo.getFechaLimite());
                moraCalculada = tasaMoraDiaria.multiply(new BigDecimal(diasDeMora)); // Multiplicamos la tasa por los dias
                LogsError.info(this.getClass(), "Calculo de mora para Prestamo ID " + prestamo.getId() + ": " + diasDeMora + " dias * " + tasaMoraDiaria + "/dia = " + moraCalculada);
            }
        }
        return moraCalculada; // Devolvemos la mora total calculada
    }

    // --- Gestion de Configuracion de Mora ---
    @Override
    public List<MoraAnual> obtenerTodasLasMorasAnuales() throws SQLException {
        // Este metodo devuelve todas las configuraciones de mora anual que existen.
        return moraAnualDAO.obtenerTodas();
    }

    @Override
    public MoraAnual obtenerMoraPorAnio(int anio) throws SQLException {
        // Este metodo busca la configuracion de mora para un anio especifico.
        return moraAnualDAO.obtenerPorAnio(anio);
    }

    @Override
    public boolean guardarMoraAnual(MoraAnual moraAnual) throws SQLException, BibliotecaException {
        // Este metodo guarda o actualiza la configuracion de mora para un anio.
        if (moraAnual == null || moraAnual.getAnio() <= 1900 || moraAnual.getAnio() > LocalDate.now().getYear() + 10) { // Validacion simple del anio
            throw new BibliotecaException("El anio para la mora anual no es valido.");
        }
        if (moraAnual.getMoraDiaria() == null || moraAnual.getMoraDiaria().compareTo(BigDecimal.ZERO) < 0) { // La mora no puede ser negativa
            throw new BibliotecaException("La mora diaria debe ser un valor no negativo.");
        }

        MoraAnual existente = moraAnualDAO.obtenerPorAnio(moraAnual.getAnio()); // Vemos si ya existe una mora para ese anio
        if (existente != null) {
            // Si ya existe, la actualizamos
            existente.setMoraDiaria(moraAnual.getMoraDiaria());
            return moraAnualDAO.actualizar(existente);
        } else {
            // Si no existe, la insertamos como nueva
            return moraAnualDAO.insertar(moraAnual);
        }
    }

    @Override
    public boolean eliminarMoraAnual(int anio) throws SQLException {
        // Este metodo elimina la configuracion de mora para un anio.
        return moraAnualDAO.eliminar(anio);
    }

    @Override
    public ConfiguracionSistema obtenerConfiguracionGlobalSistema() throws SQLException {
        // Este metodo devuelve la configuracion global del sistema (como la mora global).
        return configuracionSistemaDAO.obtenerConfiguracion();
    }

    @Override
    public boolean actualizarConfiguracionGlobalSistema(ConfiguracionSistema config) throws SQLException, BibliotecaException {
        // Este metodo actualiza la configuracion global del sistema.
        if (config == null) {
            throw new IllegalArgumentException("El objeto de configuracion no puede ser nulo.");
        }
        if (config.getMoraDiariaGlobal() != null && config.getMoraDiariaGlobal().compareTo(BigDecimal.ZERO) < 0) { // La mora global no puede ser negativa
            throw new BibliotecaException("La mora diaria global debe ser un valor no negativo.");
        }
        return configuracionSistemaDAO.actualizarConfiguracion(config);
    }
}