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

    private final MoraAnualDAO moraAnualDAO;
    private final ConfiguracionSistemaDAO configuracionSistemaDAO;

    public MoraServiceImpl() {
        this.moraAnualDAO = new MoraAnualDAOImpl();
        this.configuracionSistemaDAO = new ConfiguracionSistemaDAOImpl();
    }

    // Constructor para inyeccion de dependencias
    public MoraServiceImpl(MoraAnualDAO moraAnualDAO, ConfiguracionSistemaDAO configuracionSistemaDAO) {
        this.moraAnualDAO = moraAnualDAO;
        this.configuracionSistemaDAO = configuracionSistemaDAO;
    }

    @Override
    public BigDecimal obtenerTasaMoraDiariaAplicable(LocalDate fechaParaCalculo) throws SQLException, BibliotecaException {
        if (fechaParaCalculo == null) {
            throw new IllegalArgumentException("La fecha para calculo de mora no puede ser nula.");
        }
        int anio = fechaParaCalculo.getYear();
        MoraAnual configMoraAnual = moraAnualDAO.obtenerPorAnio(anio);
        BigDecimal moraDiariaAplicable;

        if (configMoraAnual != null && configMoraAnual.getMoraDiaria() != null) {
            moraDiariaAplicable = configMoraAnual.getMoraDiaria();
            LogsError.info(this.getClass(), "Tasa de mora anual (" + anio + ") encontrada: " + moraDiariaAplicable);
        } else {
            ConfiguracionSistema configGlobal = configuracionSistemaDAO.obtenerConfiguracion();
            if (configGlobal != null && configGlobal.getMoraDiariaGlobal() != null) {
                moraDiariaAplicable = configGlobal.getMoraDiariaGlobal();
                LogsError.warn(this.getClass(), "Usando tasa de mora diaria global: " + moraDiariaAplicable + " (no encontrada para el año " + anio + ")");
            } else {
                LogsError.error(this.getClass(), "No se encontro configuracion de mora diaria para el año " + anio + " ni configuracion global.");
                throw new BibliotecaException("No hay configuración de mora diaria disponible para calcular.");
            }
        }
        return moraDiariaAplicable;
    }

    @Override
    public BigDecimal calcularMoraParaPrestamo(Prestamo prestamo, LocalDate fechaDevolucionActual) throws SQLException, BibliotecaException {
        if (prestamo == null || prestamo.getFechaLimite() == null || fechaDevolucionActual == null) {
            throw new IllegalArgumentException("Datos de préstamo o fecha de devolución inválidos para calcular mora.");
        }

        BigDecimal moraCalculada = BigDecimal.ZERO;

        if (fechaDevolucionActual.isAfter(prestamo.getFechaLimite())) {
            long diasDeMora = ChronoUnit.DAYS.between(prestamo.getFechaLimite(), fechaDevolucionActual);
            if (diasDeMora > 0) {
                // La fecha de referencia para la tasa de mora es la fecha límite del préstamo
                BigDecimal tasaMoraDiaria = obtenerTasaMoraDiariaAplicable(prestamo.getFechaLimite());
                moraCalculada = tasaMoraDiaria.multiply(new BigDecimal(diasDeMora));
                LogsError.info(this.getClass(), "Calculo de mora para Prestamo ID " + prestamo.getId() + ": " + diasDeMora + " dias * " + tasaMoraDiaria + "/día = " + moraCalculada);
            }
        }
        return moraCalculada;
    }

    // --- Gestión de Configuración de Mora ---
    @Override
    public List<MoraAnual> obtenerTodasLasMorasAnuales() throws SQLException {
        return moraAnualDAO.obtenerTodas();
    }
    
    @Override
    public MoraAnual obtenerMoraPorAnio(int anio) throws SQLException {
        return moraAnualDAO.obtenerPorAnio(anio);
    }

    @Override
    public boolean guardarMoraAnual(MoraAnual moraAnual) throws SQLException, BibliotecaException {
        if (moraAnual == null || moraAnual.getAnio() <= 1900 || moraAnual.getAnio() > LocalDate.now().getYear() + 10) { // Validación basica del año
            throw new BibliotecaException("El año para la mora anual no es valido.");
        }
        if (moraAnual.getMoraDiaria() == null || moraAnual.getMoraDiaria().compareTo(BigDecimal.ZERO) < 0) {
            throw new BibliotecaException("La mora diaria debe ser un valor no negativo.");
        }

        MoraAnual existente = moraAnualDAO.obtenerPorAnio(moraAnual.getAnio());
        if (existente != null) {
            // Actualizar la mora para el año existente
            existente.setMoraDiaria(moraAnual.getMoraDiaria());
            return moraAnualDAO.actualizar(existente);
        } else {
            // Insertar nueva configuración de mora para el año
            return moraAnualDAO.insertar(moraAnual);
        }
    }

    @Override
    public boolean eliminarMoraAnual(int anio) throws SQLException {
        return moraAnualDAO.eliminar(anio);
    }
    
    @Override
    public ConfiguracionSistema obtenerConfiguracionGlobalSistema() throws SQLException {
        return configuracionSistemaDAO.obtenerConfiguracion();
    }

    @Override
    public boolean actualizarConfiguracionGlobalSistema(ConfiguracionSistema config) throws SQLException, BibliotecaException {
        if (config == null) {
            throw new IllegalArgumentException("El objeto de configuracion no puede ser nulo.");
        }
        if (config.getMoraDiariaGlobal() != null && config.getMoraDiariaGlobal().compareTo(BigDecimal.ZERO) < 0) {
            throw new BibliotecaException("La mora diaria global debe ser un valor no negativo.");
        }
        return configuracionSistemaDAO.actualizarConfiguracion(config);
    }
}