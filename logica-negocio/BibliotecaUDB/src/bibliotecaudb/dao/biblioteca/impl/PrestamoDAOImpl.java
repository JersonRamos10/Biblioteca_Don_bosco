package bibliotecaudb.dao.biblioteca.impl;

import bibliotecaudb.modelo.biblioteca.Prestamo;
import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.dao.usuario.impl.UsuarioDAOImpl;
import bibliotecaudb.modelo.biblioteca.Ejemplar;
import bibliotecaudb.dao.biblioteca.PrestamoDAO;
import bibliotecaudb.dao.usuario.UsuarioDAO;
import bibliotecaudb.dao.biblioteca.EjemplarDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date; // Para convertir LocalDate a fecha de SQL
import java.sql.Types; // Para usar valores nulos (null)
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAOImpl implements PrestamoDAO {

    private static final String SQL_INSERT = "INSERT INTO prestamos (id_usuario, id_ejemplar, fecha_prestamo, fecha_limite, mora) VALUES (?, ?, ?, ?, ?)";
    // Esta consulta de actualizar se usa mas que todo para poner la fecha de devolucion y la mora cuando se devuelve un libro.
    private static final String SQL_UPDATE = "UPDATE prestamos SET id_usuario = ?, id_ejemplar = ?, fecha_prestamo = ?, fecha_devolucion = ?, fecha_limite = ?, mora = ? WHERE id = ?";
    private static final String SQL_REGISTRAR_DEVOLUCION = "UPDATE prestamos SET fecha_devolucion = ?, mora = ? WHERE id = ?";
    private static final String SQL_SELECT_BY_ID = "SELECT id, id_usuario, id_ejemplar, fecha_prestamo, fecha_devolucion, fecha_limite, mora FROM prestamos WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, id_usuario, id_ejemplar, fecha_prestamo, fecha_devolucion, fecha_limite, mora FROM prestamos ORDER BY fecha_prestamo DESC";
    private static final String SQL_SELECT_BY_ID_USUARIO = "SELECT id, id_usuario, id_ejemplar, fecha_prestamo, fecha_devolucion, fecha_limite, mora FROM prestamos WHERE id_usuario = ? ORDER BY fecha_prestamo DESC";
    private static final String SQL_SELECT_ACTIVOS_BY_ID_USUARIO = "SELECT id, id_usuario, id_ejemplar, fecha_prestamo, fecha_devolucion, fecha_limite, mora FROM prestamos WHERE id_usuario = ? AND fecha_devolucion IS NULL ORDER BY fecha_prestamo DESC";
    private static final String SQL_COUNT_ACTIVOS_BY_ID_USUARIO = "SELECT COUNT(*) FROM prestamos WHERE id_usuario = ? AND fecha_devolucion IS NULL";
    private static final String SQL_SELECT_ACTIVOS = "SELECT p.id, p.id_usuario, p.id_ejemplar, p.fecha_prestamo, p.fecha_devolucion, p.fecha_limite, p.mora FROM prestamos p WHERE p.fecha_devolucion IS NULL ORDER BY p.fecha_prestamo DESC";
    private static final String SQL_SELECT_CON_MORA_PENDIENTE = "SELECT id, id_usuario, id_ejemplar, fecha_prestamo, fecha_devolucion, fecha_limite, mora FROM prestamos WHERE fecha_devolucion IS NULL AND fecha_limite < CURDATE()";
    private static final String SQL_SELECT_CON_MORA_PENDIENTE_POR_USUARIO = "SELECT id, id_usuario, id_ejemplar, fecha_prestamo, fecha_devolucion, fecha_limite, mora FROM prestamos WHERE id_usuario = ? AND fecha_devolucion IS NULL AND fecha_limite < CURDATE()";


    private UsuarioDAO usuarioDAO; // Objeto para manejar los datos de Usuarios
    private EjemplarDAO ejemplarDAO; // Objeto para manejar los datos de Ejemplares

    public PrestamoDAOImpl() {
        this.usuarioDAO = new UsuarioDAOImpl(); // Creamos el objeto para usuarios
        this.ejemplarDAO = new EjemplarDAOImpl(); // Creamos el objeto para ejemplares
    }

    // Constructor por los manejadores 
    public PrestamoDAOImpl(UsuarioDAO usuarioDAO, EjemplarDAO ejemplarDAO) {
        this.usuarioDAO = usuarioDAO;
        this.ejemplarDAO = ejemplarDAO;
    }

    @Override
    public boolean insertar(Prestamo prestamo) throws SQLException {
        // Este metodo sirve para guardar un nuevo prestamo en la base de datos.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        ResultSet generatedKeys = null; // Para obtener el ID generado
        int rowsAffected = 0; // Para saber cuantas filas se afectaron
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            // Un trigger en la BD (tg_update_ejemplar_estado_prestado) deberia cambiar el estado del ejemplar.
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, prestamo.getIdUsuario());
            pstmt.setInt(2, prestamo.getIdEjemplar());
            pstmt.setDate(3, Date.valueOf(prestamo.getFechaPrestamo())); // Convertimos la fecha de Java a SQL
            pstmt.setDate(4, Date.valueOf(prestamo.getFechaLimite()));
            pstmt.setBigDecimal(5, prestamo.getMora() != null ? prestamo.getMora() : BigDecimal.ZERO); // Si la mora es null, guardamos CERO

            LogsError.info(this.getClass(), "Ejecutando consulta para insertar prestamo: " + SQL_INSERT);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la insercion
            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys(); // Obtenemos el ID
                if (generatedKeys.next()) {
                    prestamo.setId(generatedKeys.getInt(1)); // Asignamos el ID al objeto prestamo
                }
                LogsError.info(this.getClass(), "Prestamo insertado con ID: " + prestamo.getId());
            } else {
                 LogsError.warn(this.getClass(), "No se inserto el Prestamo.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar prestamo: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(generatedKeys); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se inserto algo
    }

    @Override
    public boolean actualizar(Prestamo prestamo) throws SQLException {
        // Este metodo sirve para actualizar un prestamo existente.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        int rowsAffected = 0; // Filas afectadas
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setInt(1, prestamo.getIdUsuario());
            pstmt.setInt(2, prestamo.getIdEjemplar());
            pstmt.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
            if (prestamo.getFechaDevolucion() != null) {
                pstmt.setDate(4, Date.valueOf(prestamo.getFechaDevolucion()));
            } else {
                pstmt.setNull(4, Types.DATE); // Si no hay fecha de devolucion, guardamos nulo
            }
            pstmt.setDate(5, Date.valueOf(prestamo.getFechaLimite()));
            pstmt.setBigDecimal(6, prestamo.getMora());
            pstmt.setInt(7, prestamo.getId()); // El ID del prestamo a actualizar (WHERE)

            LogsError.info(this.getClass(), "Ejecutando consulta para actualizar prestamo: " + SQL_UPDATE + " para ID: " + prestamo.getId());
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la actualizacion
             if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Prestamo actualizado. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontro Prestamo para actualizar con ID: " + prestamo.getId() + " o los valores eran los mismos.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar prestamo: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se actualizo algo
    }

    @Override
    public boolean registrarDevolucion(int idPrestamo, LocalDate fechaDevolucion, BigDecimal moraPagada) throws SQLException {
        // Este metodo sirve para registrar la devolucion de un prestamo, actualizando su fecha de devolucion y la mora.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        int rowsAffected = 0; // Filas afectadas
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            // Nota: Este metodo solo actualiza la tabla 'prestamos'.
            // La creacion del registro en 'devoluciones' y el cambio de estado del ejemplar
            // se deben manejar en otro lado, quizas en una capa de servicio.
            pstmt = conn.prepareStatement(SQL_REGISTRAR_DEVOLUCION);
            pstmt.setDate(1, Date.valueOf(fechaDevolucion)); // La fecha en que se devolvio
            pstmt.setBigDecimal(2, moraPagada != null ? moraPagada : BigDecimal.ZERO); // La mora que se pago (o CERO si no hubo)
            pstmt.setInt(3, idPrestamo); // El ID del prestamo

            LogsError.info(this.getClass(), "Registrando devolucion en tabla prestamos para ID: " + idPrestamo);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la actualizacion
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Devolucion registrada en prestamo. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontro Prestamo para registrar devolucion con ID: " + idPrestamo);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al registrar devolucion en prestamo: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se actualizo algo
    }

    // Este metodo convierte los datos de la base de datos (ResultSet) a un objeto Prestamo.
    private Prestamo mapearResultSet(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo(); // Creamos un objeto Prestamo vacio
        p.setId(rs.getInt("id"));
        p.setIdUsuario(rs.getInt("id_usuario"));
        p.setIdEjemplar(rs.getInt("id_ejemplar"));

        Date sqlFechaPrestamo = rs.getDate("fecha_prestamo"); // Obtenemos la fecha de prestamo de SQL
        if (sqlFechaPrestamo != null) p.setFechaPrestamo(sqlFechaPrestamo.toLocalDate()); // La convertimos a fecha de Java

        Date sqlFechaDevolucion = rs.getDate("fecha_devolucion"); // Obtenemos la fecha de devolucion
        if (sqlFechaDevolucion != null) p.setFechaDevolucion(sqlFechaDevolucion.toLocalDate()); else p.setFechaDevolucion(null); // Si es nula, la dejamos nula

        Date sqlFechaLimite = rs.getDate("fecha_limite"); // Obtenemos la fecha limite
        if (sqlFechaLimite != null) p.setFechaLimite(sqlFechaLimite.toLocalDate()); // La convertimos a fecha de Java

        p.setMora(rs.getBigDecimal("mora"));

        // Cargamos los objetos Usuario y Ejemplar relacionados
        if (this.usuarioDAO != null) { // Si tenemos el objeto para manejar usuarios
            Usuario u = this.usuarioDAO.obtenerPorId(p.getIdUsuario()); // Buscamos el usuario
            p.setUsuario(u); // Asignamos el objeto Usuario completo
        }
        if (this.ejemplarDAO != null) { // Si tenemos el objeto para manejar ejemplares
            Ejemplar e = this.ejemplarDAO.obtenerPorId(p.getIdEjemplar()); // Buscamos el ejemplar
            p.setEjemplar(e); // Asignamos el objeto Ejemplar completo
        }
        return p; // Devolvemos el prestamo con todos sus datos
    }

    @Override
    public Prestamo obtenerPorId(int id) throws SQLException {
        // Este metodo busca y devuelve un prestamo usando su ID.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para guardar el resultado
        Prestamo prestamo = null; // Variable para el prestamo
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id); // El ID del prestamo que buscamos
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener prestamo por ID: " + SQL_SELECT_BY_ID + " con ID: " + id);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            if (rs.next()) { // Si encontramos el prestamo
                prestamo = mapearResultSet(rs); // Convertimos los datos a objeto
            } else {
                 LogsError.warn(this.getClass(), "No se encontro Prestamo con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener prestamo por ID: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return prestamo; // Devolvemos el prestamo (o null)
    }

    @Override
    public List<Prestamo> obtenerTodos() throws SQLException {
        // Este metodo devuelve una lista con todos los prestamos de la base de datos.
        return obtenerListaDePrestamos(SQL_SELECT_ALL, -1, "Error al obtener todos los prestamos");
    }

    @Override
    public List<Prestamo> obtenerPorIdUsuario(int idUsuario) throws SQLException {
        // Este metodo devuelve una lista con todos los prestamos de un usuario especifico.
         return obtenerListaDePrestamos(SQL_SELECT_BY_ID_USUARIO, idUsuario, "Error al obtener prestamos por idUsuario");
    }

    @Override
    public List<Prestamo> obtenerActivosPorIdUsuario(int idUsuario) throws SQLException {
        // Este metodo devuelve una lista con los prestamos activos (no devueltos) de un usuario.
        return obtenerListaDePrestamos(SQL_SELECT_ACTIVOS_BY_ID_USUARIO, idUsuario, "Error al obtener prestamos activos por idUsuario");
    }

    @Override
    public List<Prestamo> obtenerPrestamosActivos() throws SQLException {
        // Este metodo devuelve una lista con todos los prestamos que estan activos actualmente.
        return obtenerListaDePrestamos(SQL_SELECT_ACTIVOS, -1, "Error al obtener todos los prestamos activos");
    }

    @Override
    public List<Prestamo> obtenerPrestamosConMoraPendiente() throws SQLException {
        // Este metodo devuelve una lista de todos los prestamos que no han sido devueltos y ya pasaron su fecha limite.
        return obtenerListaDePrestamos(SQL_SELECT_CON_MORA_PENDIENTE, -1, "Error al obtener prestamos con mora pendiente");
    }

    @Override
    public List<Prestamo> obtenerPrestamosConMoraPendientePorUsuario(int idUsuario) throws SQLException {
        // Este metodo devuelve una lista de los prestamos con mora pendiente para un usuario especifico.
        return obtenerListaDePrestamos(SQL_SELECT_CON_MORA_PENDIENTE_POR_USUARIO, idUsuario, "Error al obtener prestamos con mora pendiente para el usuario");
    }

    // Metodo privado para no repetir codigo al obtener listas de prestamos.
    private List<Prestamo> obtenerListaDePrestamos(String sql, int parametroId, String errorMsg) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        List<Prestamo> prestamos = new ArrayList<>(); // Lista para guardar los prestamos
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(sql);
            if (parametroId != -1) { // Si necesitamos un parametro (como id_usuario)
                pstmt.setInt(1, parametroId);
            }
            LogsError.info(this.getClass(), "Ejecutando consulta: " + sql + (parametroId != -1 ? " con parametro: " + parametroId : ""));
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            while (rs.next()) { // Mientras haya prestamos
                prestamos.add(mapearResultSet(rs)); // Agregamos el prestamo a la lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), errorMsg + ": " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return prestamos; // Devolvemos la lista de prestamos
    }

    @Override
    public int contarPrestamosActivosPorUsuario(int idUsuario) throws SQLException {
        // Este metodo cuenta cuantos prestamos activos (no devueltos) tiene un usuario.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para el resultado
        int conteo = 0; // Variable para el conteo
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_COUNT_ACTIVOS_BY_ID_USUARIO);
            pstmt.setInt(1, idUsuario); // El ID del usuario
            LogsError.info(this.getClass(), "Ejecutando consulta para contar prestamos activos: " + SQL_COUNT_ACTIVOS_BY_ID_USUARIO + " para idUsuario: " + idUsuario);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            if (rs.next()) { // Si hay resultado
                conteo = rs.getInt(1); // El conteo esta en la primera columna
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al contar prestamos activos por usuario: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return conteo; // Devolvemos el numero de prestamos activos
    }

    @Override
    public boolean verificarUsuarioTieneMora(int idUsuario) throws SQLException {
        // Este metodo verifica si un usuario tiene algun prestamo con mora pendiente.
        List<Prestamo> prestamosConMora = obtenerPrestamosConMoraPendientePorUsuario(idUsuario); // Obtenemos sus prestamos con mora
        return !prestamosConMora.isEmpty(); // Si la lista no esta vacia, entonces tiene mora
    }
}