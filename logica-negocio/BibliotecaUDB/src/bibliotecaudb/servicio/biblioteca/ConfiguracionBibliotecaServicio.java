package bibliotecaudb.servicio.biblioteca; 

import bibliotecaudb.dao.biblioteca.ConfiguracionSistemaDAO;
import bibliotecaudb.dao.biblioteca.MoraAnualDAO;
import bibliotecaudb.modelo.biblioteca.ConfiguracionSistema;
import bibliotecaudb.modelo.biblioteca.MoraAnual;
import bibliotecaudb.modelo.usuario.Usuario; // Para verificar permisos de Admin
import bibliotecaudb.conexion.LogsError;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ConfiguracionBibliotecaServicio {

    // Constante para el nombre del tipo de usuario Administrador
    private static final String TIPO_USUARIO_ADMIN = "Administrador";
    // ID estandar para la fila de configuracion en la tabla configuracion_sistema
    private static final int ID_CONFIGURACION_SISTEMA_PREDET = 1;


    // Instancias de los DAOs necesarios
    private ConfiguracionSistemaDAO configuracionSistemaDAO;
    private MoraAnualDAO moraAnualDAO;

    public ConfiguracionBibliotecaServicio() {
        this.configuracionSistemaDAO = new ConfiguracionSistemaDAO();
        this.moraAnualDAO = new MoraAnualDAO();
    }

    /**
     * Obtiene la configuracion actual del sistema (maximo ejemplares, mora diaria general).
     *
     * @return El objeto ConfiguracionSistema.
     * @throws Exception Si no se encuentra la configuración o hay un error de BD.
     */
    public ConfiguracionSistema obtenerConfiguracionActual() throws Exception {
        try {
            ConfiguracionSistema config = configuracionSistemaDAO.obtenerConfiguracion(ID_CONFIGURACION_SISTEMA_PREDET);
            if (config == null) {
                LogsError.error(ConfiguracionBibliotecaServicio.class, "No se encontro la configuración del sistema en la BD (ID: " + ID_CONFIGURACION_SISTEMA_PREDET + ").");
                throw new Exception("La configuracion base del sistema no ha sido establecida. Contacte al administrador.");
            }
            return config;
        } catch (SQLException e) {
            LogsError.error(ConfiguracionBibliotecaServicio.class, "Error de BD al obtener la configuración del sistema.", e);
            throw new Exception("Error al obtener la configuración del sistema.", e);
        }
    }

    /**
     * Actualiza el numero maximo de ejemplares que un usuario puede tener en prestamo.
     * Requiere que el usuario operador sea Administrador.
     *
     * @param nuevoMaximo     El nuevo limite máximo de ejemplares.
     * @param usuarioOperador El usuario que realiza la operación.
     * @return true si la actualizacion fue exitosa.
     * @throws Exception Si el usuario no es Admin, el valor es invalido o hay error de BD.
     */
    public boolean actualizarMaximoEjemplaresPrestamo(int nuevoMaximo, Usuario usuarioOperador) throws Exception {
        validarPermisoAdmin(usuarioOperador, "actualizar maximo de ejemplares en préstamo");

        if (nuevoMaximo < 0) { 
            throw new Exception("El mximo de ejemplares no puede ser un número negativo.");
        }

        try {
            ConfiguracionSistema configActual = obtenerConfiguracionActual(); // Reutiliza el método para obtenerla
            configActual.setMaximoEjemplares(nuevoMaximo);

            boolean exito = configuracionSistemaDAO.actualizarConfiguracion(configActual);
            if (exito) {
                LogsError.info(ConfiguracionBibliotecaServicio.class, "Máximo de ejemplares actualizado a: " + nuevoMaximo + " por " + usuarioOperador.getCorreo());
            }
            return exito;
        } catch (SQLException e) {
            LogsError.error(ConfiguracionBibliotecaServicio.class, "Error de BD al actualizar máximo de ejemplares.", e);
            throw new Exception("Error al actualizar el máximo de ejemplares.", e);
        }
    }

    /**
     * Obtiene la configuración de mora diaria para un año específico.
     *
     * @param anio El año para el cual se busca la tarifa de mora.
     * @return El objeto MoraAnual, o null si no hay configuración para ese año.
     * @throws Exception Si ocurre un error de BD.
     */
    public MoraAnual obtenerMoraAnual(int anio) throws Exception {
        if (anio <= 0) {
            throw new Exception("El año debe ser un número positivo.");
        }
        try {
            return moraAnualDAO.obtenerPorAnio(anio);
        } catch (SQLException e) {
            LogsError.error(ConfiguracionBibliotecaServicio.class, "Error de BD al obtener mora anual para el año: " + anio, e);
            throw new Exception("Error al obtener la configuración de mora para el año " + anio + ".", e);
        }
    }

    /**
     * Establece o actualiza la tarifa de mora diaria para un año especifico.
     * Requiere que el usuario operador sea Administrador.
     *
     * @param anio            El año para la tarifa de mora.
     * @param nuevaMoraDiaria La nueva tarifa de mora diaria.
     * @param usuarioOperador El usuario que realiza la operación.
     * @return true si la operación fue exitosa.
     * @throws Exception Si el usuario no es Admin, los datos son inválidos o hay error de BD.
     */
    public boolean establecerOActualizarMoraAnual(int anio, BigDecimal nuevaMoraDiaria, Usuario usuarioOperador) throws Exception {
        validarPermisoAdmin(usuarioOperador, "establecer o actualizar mora anual");

        if (anio <= 1900) { // Validación simple del año
            throw new Exception("El año especificado no es valido.");
        }
        Objects.requireNonNull(nuevaMoraDiaria, "La nueva mora diaria no puede ser nula.");
        if (nuevaMoraDiaria.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("La mora diaria no puede ser un valor negativo.");
        }

        try {
            MoraAnual mora = new MoraAnual(anio, nuevaMoraDiaria);
            // El DAO se encarga de decidir si es INSERT o UPDATE
            boolean exito = moraAnualDAO.crearOActualizar(mora);
            if (exito) {
                LogsError.info(ConfiguracionBibliotecaServicio.class, "Mora anual para " + anio + " establecida/actualizada a: " + nuevaMoraDiaria + " por " + usuarioOperador.getCorreo());
            }
            return exito;
        } catch (SQLException e) {
            LogsError.error(ConfiguracionBibliotecaServicio.class, "Error de BD al establecer/actualizar mora anual para el año: " + anio, e);
            throw new Exception("Error al guardar la configuracion de mora para el año " + anio + ".", e);
        }
    }

    /**
     * Obtiene todas las configuraciones de mora anual registradas.
     *
     * @return Lista de objetos MoraAnual.
     * @throws Exception Si ocurre un error de BD.
     */
    public List<MoraAnual> obtenerTodasLasMorasAnuales() throws Exception {
        try {
            return moraAnualDAO.obtenerTodas();
        } catch (SQLException e) {
            LogsError.error(ConfiguracionBibliotecaServicio.class, "Error de BD al obtener todas las moras anuales.", e);
            throw new Exception("Error al obtener el listado de moras anuales.", e);
        }
    }


    // --- Metodo de ayuda privado para validacion de permisos ---
    private void validarPermisoAdmin(Usuario usuarioOperador, String accion) throws Exception {
        Objects.requireNonNull(usuarioOperador, "El usuario operador no puede ser nulo para verificar permisos.");
        Objects.requireNonNull(usuarioOperador.getTipoUsuario(), "El tipo de usuario del operador no puede ser nulo.");

        if (!TIPO_USUARIO_ADMIN.equals(usuarioOperador.getTipoUsuario().getTipo())) {
            LogsError.warn(GestionDocumentosServicio.class, // Podrías cambiar a ConfiguracionBibliotecaServicio.class
                    "Intento no autorizado de '" + accion + "' por usuario: "
                    + usuarioOperador.getCorreo() + " (Tipo: " + usuarioOperador.getTipoUsuario().getTipo() + ")");
            throw new Exception("No tiene permisos para realizar la acción: " + accion + ". Se requiere ser Administrador.");
        }
    }
}