package bibliotecaudb.servicio.biblioteca; // O el paquete que uses para servicios de biblioteca

import bibliotecaudb.dao.biblioteca.*; // Importa todos los DAO de biblioteca
import bibliotecaudb.dao.usuario.UsuarioDAO;
import bibliotecaudb.modelo.biblioteca.*; // Importa todos los modelos de biblioteca
import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.modelo.biblioteca.ConfiguracionSistema; 
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.dao.biblioteca.DevolucionDAO;

import java.sql.SQLException;
import java.sql.Date; 
import java.math.BigDecimal;
import java.time.LocalDate; // Para calculos de fechas
import java.time.temporal.ChronoUnit; // Para calcular diferencia de dias
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class GestionPrestamosServicio {

    // DAOs necesarios
    private PrestamoDAO prestamoDAO;
    private EjemplarDAO ejemplarDAO;
    private UsuarioDAO usuarioDAO; 
    private ConfiguracionSistemaDAO configuracionDAO;
    private MoraAnualDAO moraAnualDAO;
    private DevolucionDAO devolucionDAO;

    // Constantes para nombres de tipos de usuario (para lógica de privilegios/límites)
   
   // Constantes para nombres de tipos de usuario
    private static final String TIPO_USUARIO_ADMIN = "Administrador";
    private static final String TIPO_USUARIO_PROFESOR = "Profesor";
    private static final String TIPO_USUARIO_ALUMNO = "Alumno";

    // --- Limites y dias de prestamos por tipo de usario ---
    // Administradores
    private static final int MAX_EJEMPLARES_ADMIN = 999; // ilimitado
    private static final int DIAS_PRESTAMO_ADMIN = 30;   // 30 días

    // Profesores
    private static final int MAX_EJEMPLARES_PROFESOR = 10; 
    private static final int DIAS_PRESTAMO_PROFESOR = 15; // 15 días

    // Alumnos
    private static final int MAX_EJEMPLARES_ALUMNO = 5;  
    private static final int DIAS_PRESTAMO_ALUMNO = 7;  // 7 días

    public GestionPrestamosServicio() {
        this.prestamoDAO = new PrestamoDAO();
        this.ejemplarDAO = new EjemplarDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.configuracionDAO = new ConfiguracionSistemaDAO();
        this.moraAnualDAO = new MoraAnualDAO();
        this.devolucionDAO = new DevolucionDAO(); 
    }

    /**
     * Realiza un nuevo préstamo de un ejemplar a un usuario.
     * Aplica validaciones de disponibilidad, mora del usuario y límites de préstamo.
     *
     * @param idUsuario El ID del usuario que solicita el préstamo.
     * @param idEjemplar El ID del ejemplar a prestar.
     * @return El objeto Prestamo creado.
     * @throws Exception Si alguna validación falla o ocurre un error de BD.
     */
    
public Prestamo realizarPrestamo(int idUsuario, int idEjemplar) throws Exception {
        LogsError.info(GestionPrestamosServicio.class, "Iniciando proceso de préstamo para Usuario ID: " + idUsuario + ", Ejemplar ID: " + idEjemplar);

        try {
            Usuario usuario = usuarioDAO.obtenerPorId(idUsuario);
            if (usuario == null) throw new Exception("Usuario con ID " + idUsuario + " no encontrado.");
            if (!usuario.isEstado()) throw new Exception("El usuario " + usuario.getCorreo() + " no está activo.");

            Ejemplar ejemplar = ejemplarDAO.obtenerPorId(idEjemplar);
            if (ejemplar == null) throw new Exception("Ejemplar con ID " + idEjemplar + " no encontrado.");
            if (ejemplar.getDocumento() == null) throw new Exception("Ejemplar ID " + idEjemplar + " no tiene documento asociado.");
            if (ejemplar.getEstado() != Ejemplar.EstadoEjemplar.DISPONIBLE) {
                throw new Exception("El ejemplar '" + ejemplar.getDocumento().getTitulo() + "' (ID: " + idEjemplar + ") no está disponible.");
            }

            // Validar si el usuario tiene mora
            List<Prestamo> prestamosActivosUsuario = prestamoDAO.obtenerPrestamosActivosPorUsuario(idUsuario);
            for (Prestamo pActivo : prestamosActivosUsuario) {
                // Convertir java.sql.Date a LocalDate para comparar
                LocalDate fechaLimitePrestamo = pActivo.getFechaLimite().toLocalDate();
                if (fechaLimitePrestamo.isBefore(LocalDate.now())) {
                    throw new Exception("El usuario tiene préstamos vencidos y no puede realizar nuevos préstamos.");
                }
            }

            // Determinar limites y dias de prestamo segun tipo de usuario
            int maxEjemplaresPermitidos;
            int diasDePrestamo;
            String tipoUsuarioNombre = (usuario.getTipoUsuario() != null) ? usuario.getTipoUsuario().getTipo() : "";

            switch (tipoUsuarioNombre) {
                case TIPO_USUARIO_ADMIN:
                    maxEjemplaresPermitidos = MAX_EJEMPLARES_ADMIN;
                    diasDePrestamo = DIAS_PRESTAMO_ADMIN;
                    LogsError.info(GestionPrestamosServicio.class, "Aplicando políticas de préstamo para Administrador.");
                    break;
                case TIPO_USUARIO_PROFESOR:
                    maxEjemplaresPermitidos = MAX_EJEMPLARES_PROFESOR;
                    diasDePrestamo = DIAS_PRESTAMO_PROFESOR;
                    LogsError.info(GestionPrestamosServicio.class, "Aplicando políticas de préstamo para Profesor.");
                    break;
                case TIPO_USUARIO_ALUMNO:
                    maxEjemplaresPermitidos = MAX_EJEMPLARES_ALUMNO;
                    diasDePrestamo = DIAS_PRESTAMO_ALUMNO;
                    LogsError.info(GestionPrestamosServicio.class, "Aplicando políticas de préstamo para Alumno.");
                    break;
                default:
                    // Si hay un tipo de usuario no reconocido o nulo, usar la configuración general del sistema
                    ConfiguracionSistema config = configuracionDAO.obtenerConfiguracion(1); // ID 1 para config general
                    if (config == null || config.getMaximoEjemplares() == null) {
                        throw new Exception("No se pudo cargar la configuración de préstamos por defecto del sistema.");
                    }
                    maxEjemplaresPermitidos = config.getMaximoEjemplares();
                    // Para los días, podríamos tener un default o también leerlo de config.
                    // Por ahora, usamos un default si no es un tipo reconocido
                    diasDePrestamo = DIAS_PRESTAMO_ALUMNO; // O DIAS_PRESTAMO_DEFAULT si lo tuvieras
                    LogsError.warn(GestionPrestamosServicio.class, "Tipo de usuario no reconocido o nulo: " + tipoUsuarioNombre + ". Aplicando políticas por defecto.");
                    break;
            }

            // Validar límite de préstamos
            if (prestamosActivosUsuario.size() >= maxEjemplaresPermitidos) {
                throw new Exception("Ha alcanzado el límite máximo de " + maxEjemplaresPermitidos + " préstamos permitidos para su tipo de usuario.");
            }

            // Crear el préstamo
            Prestamo nuevoPrestamo = new Prestamo();
            nuevoPrestamo.setUsuario(usuario);
            nuevoPrestamo.setEjemplar(ejemplar);

            LocalDate fechaPrestamoActualLD = LocalDate.now();
            LocalDate fechaLimiteCalculadaLD = fechaPrestamoActualLD.plusDays(diasDePrestamo);

            nuevoPrestamo.setFechaPrestamo(Date.valueOf(fechaPrestamoActualLD)); // Convertir a java.sql.Date
            nuevoPrestamo.setFechaLimite(Date.valueOf(fechaLimiteCalculadaLD)); // Convertir a java.sql.Date
            nuevoPrestamo.setMora(BigDecimal.ZERO);
            nuevoPrestamo.setFechaDevolucion(null);

            Prestamo prestamoCreado = prestamoDAO.crear(nuevoPrestamo);

            if (prestamoCreado != null && prestamoCreado.getId() > 0) {
                boolean estadoActualizado = ejemplarDAO.actualizarEstado(ejemplar.getId(), Ejemplar.EstadoEjemplar.PRESTADO);
                if (!estadoActualizado) {
                    LogsError.error(GestionPrestamosServicio.class, "CRÍTICO: Préstamo ID " + prestamoCreado.getId() + " creado, pero NO se pudo actualizar el estado del Ejemplar ID: " + ejemplar.getId());
                    // En un sistema real, aquí se haría un rollback de la creación del préstamo.
                }
                LogsError.info(GestionPrestamosServicio.class, "Préstamo ID " + prestamoCreado.getId() + " realizado. Fecha límite: " + nuevoPrestamo.getFechaLimite());
                return prestamoCreado;
            } else {
                throw new Exception("No se pudo registrar el préstamo en la base de datos.");
            }

        } catch (SQLException e) {
            LogsError.error(GestionPrestamosServicio.class, "Error de BD al realizar préstamo: " + e.getMessage(), e);
            throw new Exception("Error en la base de datos al procesar el préstamo.", e);
        }
    }

    /**
     * Registra la devolución de un ejemplar.
     * Calcula la mora si aplica.
     *
     * @param idPrestamo El ID del préstamo que se está devolviendo.
     * @param fechaDevolucionReal La fecha en que se realiza la devolución.
     * @return El objeto Devolucion creado.
     * @throws Exception Si el préstamo no existe, ya fue devuelto, o error de BD.
     */
    public Devolucion registrarDevolucion(int idPrestamo, Date fechaDevolucionReal) throws Exception { // fechaDevolucionReal es java.sql.Date
        LogsError.info(GestionPrestamosServicio.class, "Iniciando proceso de devolución para Préstamo ID: " + idPrestamo);
        Objects.requireNonNull(fechaDevolucionReal, "La fecha de devolución real es requerida.");

        try {
            Prestamo prestamo = prestamoDAO.obtenerPorId(idPrestamo);
            if (prestamo == null) throw new Exception("Préstamo con ID " + idPrestamo + " no encontrado.");
            if (prestamo.getFechaDevolucion() != null) throw new Exception("Este préstamo (ID: " + idPrestamo + ") ya fue devuelto el " + prestamo.getFechaDevolucion());
            if (prestamo.getEjemplar() == null) throw new Exception("El préstamo ID " + idPrestamo + " no tiene un ejemplar asociado.");

            BigDecimal moraCalculada = BigDecimal.ZERO;
            LocalDate ldFechaDevolucionReal = fechaDevolucionReal.toLocalDate(); // Convertir para cálculo
            LocalDate ldFechaLimite = prestamo.getFechaLimite().toLocalDate();   // Convertir para cálculo

            if (ldFechaDevolucionReal.isAfter(ldFechaLimite)) {
                long diasDeMora = ChronoUnit.DAYS.between(ldFechaLimite, ldFechaDevolucionReal);
                LogsError.info(GestionPrestamosServicio.class, "Préstamo ID " + idPrestamo + " tiene " + diasDeMora + " días de mora.");

                MoraAnual configMora = moraAnualDAO.obtenerPorAnio(ldFechaLimite.getYear());
                BigDecimal tarifaMoraDiaria;

                if (configMora != null && configMora.getMoraDiaria() != null) {
                    tarifaMoraDiaria = configMora.getMoraDiaria();
                } else {
                    ConfiguracionSistema configGeneral = configuracionDAO.obtenerConfiguracion(1);
                    if (configGeneral != null && configGeneral.getMoraDiaria() != null) {
                        tarifaMoraDiaria = configGeneral.getMoraDiaria();
                        LogsError.warn(GestionPrestamosServicio.class, "No se encontró mora para el año " + ldFechaLimite.getYear() + ". Usando mora diaria general: " + tarifaMoraDiaria);
                    } else {
                        LogsError.error(GestionPrestamosServicio.class, "No se pudo determinar la tarifa de mora diaria.");
                        throw new Exception("Configuración de tarifa de mora no encontrada.");
                    }
                }
                
                moraCalculada = tarifaMoraDiaria.multiply(BigDecimal.valueOf(diasDeMora));
                LogsError.info(GestionPrestamosServicio.class, "Mora calculada para Préstamo ID " + idPrestamo + ": $" + moraCalculada);
            }

            prestamo.setFechaDevolucion(fechaDevolucionReal); // Asignar java.sql.Date
            prestamo.setMora(moraCalculada);
            boolean prestamoActualizado = prestamoDAO.actualizar(prestamo);

            if (!prestamoActualizado) {
                 throw new Exception("Error al actualizar el préstamo durante la devolución.");
            }

            Devolucion nuevaDevolucion = new Devolucion();
            nuevaDevolucion.setPrestamo(prestamo);
            nuevaDevolucion.setFechaDevolucion(fechaDevolucionReal); // Asignar java.sql.Date
            nuevaDevolucion.setMoraPagada(moraCalculada);

            Devolucion devolucionCreada = this.devolucionDAO.crear(nuevaDevolucion); // Usar this.devolucionDAO

            if (devolucionCreada != null && devolucionCreada.getId() > 0) {
                boolean estadoActualizado = ejemplarDAO.actualizarEstado(prestamo.getEjemplar().getId(), Ejemplar.EstadoEjemplar.DISPONIBLE);
                if (!estadoActualizado) {
                     LogsError.error(GestionPrestamosServicio.class, "CRÍTICO: Devolución ID " + devolucionCreada.getId() + " registrada, pero NO se pudo actualizar el estado del Ejemplar ID: " + prestamo.getEjemplar().getId());
                }
                LogsError.info(GestionPrestamosServicio.class, "Devolución ID " + devolucionCreada.getId() + " registrada exitosamente para Préstamo ID: " + idPrestamo);
                return devolucionCreada;
            } else {
                 throw new Exception("No se pudo registrar la devolución en la base de datos.");
            }

        } catch (SQLException e) {
            LogsError.error(GestionPrestamosServicio.class, "Error de BD al registrar devolución para Préstamo ID: " + idPrestamo + ": " + e.getMessage(), e);
            throw new Exception("Error en la base de datos al procesar la devolución.", e);
        }
    }

    // --- Otros métodos de consulta ---
    // ... (consultarPrestamosActivosUsuario, consultarHistorialPrestamosUsuario, consultarUsuariosConMora se mantienen igual en concepto) ...
    // Asegúrate de que los tipos de fecha sean java.sql.Date si vienen de los modelos y DAOs,
    // y convierte a LocalDate solo para los cálculos.
    public List<Prestamo> consultarPrestamosActivosUsuario(int idUsuario) throws Exception {
        try {
            return prestamoDAO.obtenerPrestamosActivosPorUsuario(idUsuario);
        } catch (SQLException e) {
             LogsError.error(GestionPrestamosServicio.class, "Error BD consultando préstamos activos para Usuario ID: " + idUsuario, e);
            throw new Exception("Error al consultar préstamos activos.", e);
        }
    }
    
    public List<Prestamo> consultarHistorialPrestamosUsuario(int idUsuario) throws Exception {
        try {
            List<Prestamo> todosLosPrestamos = prestamoDAO.obtenerTodos(); 
            List<Prestamo> historialUsuario = new ArrayList<>();
            for(Prestamo p : todosLosPrestamos){
                if(p.getUsuario() != null && p.getUsuario().getId() == idUsuario){
                    historialUsuario.add(p);
                }
            }
            return historialUsuario;
        } catch (SQLException e) {
             LogsError.error(GestionPrestamosServicio.class, "Error BD consultando historial de préstamos para Usuario ID: " + idUsuario, e);
            throw new Exception("Error al consultar historial de préstamos.", e);
        }
    }

    public List<Prestamo> consultarUsuariosConMora() throws Exception {
        try {
            return prestamoDAO.obtenerPrestamosVencidosNoDevueltos();
        } catch (SQLException e) {
             LogsError.error(GestionPrestamosServicio.class, "Error BD consultando usuarios con mora.", e);
            throw new Exception("Error al consultar usuarios con mora.", e);
        }
    }

}