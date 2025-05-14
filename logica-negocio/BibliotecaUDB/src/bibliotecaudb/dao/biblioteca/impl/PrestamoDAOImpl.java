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
import java.sql.Date; // Para convertir LocalDate a java.sql.Date
import java.sql.Types; // Para setNull
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAOImpl implements PrestamoDAO {

    private static final String SQL_INSERT = "INSERT INTO prestamos (id_usuario, id_ejemplar, fecha_prestamo, fecha_limite, mora) VALUES (?, ?, ?, ?, ?)";
    // Actualizar principalmente para registrar fecha_devolucion y mora al devolver.
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


    private UsuarioDAO usuarioDAO;
    private EjemplarDAO ejemplarDAO;

    public PrestamoDAOImpl() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.ejemplarDAO = new EjemplarDAOImpl();
    }

    // Constructor para inyección de dependencias (útil para pruebas)
    public PrestamoDAOImpl(UsuarioDAO usuarioDAO, EjemplarDAO ejemplarDAO) {
        this.usuarioDAO = usuarioDAO;
        this.ejemplarDAO = ejemplarDAO;
    }

    @Override
    public boolean insertar(Prestamo prestamo) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            // El trigger tg_update_ejemplar_estado_prestado se encargará de cambiar el estado del ejemplar.
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, prestamo.getIdUsuario());
            pstmt.setInt(2, prestamo.getIdEjemplar());
            pstmt.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
            pstmt.setDate(4, Date.valueOf(prestamo.getFechaLimite()));
            pstmt.setBigDecimal(5, prestamo.getMora() != null ? prestamo.getMora() : BigDecimal.ZERO);

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_INSERT);
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    prestamo.setId(generatedKeys.getInt(1));
                }
                LogsError.info(this.getClass(), "Préstamo insertado con ID: " + prestamo.getId());
            } else {
                 LogsError.warn(this.getClass(), "No se insertó el Préstamo.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar préstamo: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(generatedKeys);
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    @Override
    public boolean actualizar(Prestamo prestamo) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setInt(1, prestamo.getIdUsuario());
            pstmt.setInt(2, prestamo.getIdEjemplar());
            pstmt.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
            if (prestamo.getFechaDevolucion() != null) {
                pstmt.setDate(4, Date.valueOf(prestamo.getFechaDevolucion()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            pstmt.setDate(5, Date.valueOf(prestamo.getFechaLimite()));
            pstmt.setBigDecimal(6, prestamo.getMora());
            pstmt.setInt(7, prestamo.getId()); // Condición WHERE

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_UPDATE + " para ID: " + prestamo.getId());
            rowsAffected = pstmt.executeUpdate();
             if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Préstamo actualizado. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontró Préstamo para actualizar con ID: " + prestamo.getId() + " o los valores eran los mismos.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar préstamo: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }
    
    @Override
    public boolean registrarDevolucion(int idPrestamo, LocalDate fechaDevolucion, BigDecimal moraPagada) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            // Nota: Este método solo actualiza el préstamo. La creación del registro en la tabla 'devoluciones'
            // y la actualización del estado del ejemplar (a través del trigger de 'devoluciones')
            // se manejarán por separado o como parte de una transacción más grande en una capa de servicio.
            pstmt = conn.prepareStatement(SQL_REGISTRAR_DEVOLUCION);
            pstmt.setDate(1, Date.valueOf(fechaDevolucion));
            pstmt.setBigDecimal(2, moraPagada != null ? moraPagada : BigDecimal.ZERO);
            pstmt.setInt(3, idPrestamo);

            LogsError.info(this.getClass(), "Registrando devolución en tabla prestamos para ID: " + idPrestamo);
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Devolución registrada en préstamo. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontró Préstamo para registrar devolución con ID: " + idPrestamo);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al registrar devolución en préstamo: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }


    private Prestamo mapearResultSet(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setId(rs.getInt("id"));
        p.setIdUsuario(rs.getInt("id_usuario"));
        p.setIdEjemplar(rs.getInt("id_ejemplar"));
        
        Date sqlFechaPrestamo = rs.getDate("fecha_prestamo");
        if (sqlFechaPrestamo != null) p.setFechaPrestamo(sqlFechaPrestamo.toLocalDate());
        
        Date sqlFechaDevolucion = rs.getDate("fecha_devolucion");
        if (sqlFechaDevolucion != null) p.setFechaDevolucion(sqlFechaDevolucion.toLocalDate()); else p.setFechaDevolucion(null);
        
        Date sqlFechaLimite = rs.getDate("fecha_limite");
        if (sqlFechaLimite != null) p.setFechaLimite(sqlFechaLimite.toLocalDate());
        
        p.setMora(rs.getBigDecimal("mora"));

        // Cargar objetos anidados
        if (this.usuarioDAO != null) {
            Usuario u = this.usuarioDAO.obtenerPorId(p.getIdUsuario());
            p.setUsuario(u);
        }
        if (this.ejemplarDAO != null) {
            Ejemplar e = this.ejemplarDAO.obtenerPorId(p.getIdEjemplar());
            p.setEjemplar(e);
        }
        return p;
    }

    @Override
    public Prestamo obtenerPorId(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Prestamo prestamo = null;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ID + " con ID: " + id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                prestamo = mapearResultSet(rs);
            } else {
                 LogsError.warn(this.getClass(), "No se encontró Préstamo con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener préstamo por ID: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return prestamo;
    }

    @Override
    public List<Prestamo> obtenerTodos() throws SQLException {
        return obtenerListaDePrestamos(SQL_SELECT_ALL, -1, "Error al obtener todos los préstamos");
    }

    @Override
    public List<Prestamo> obtenerPorIdUsuario(int idUsuario) throws SQLException {
         return obtenerListaDePrestamos(SQL_SELECT_BY_ID_USUARIO, idUsuario, "Error al obtener préstamos por idUsuario");
    }

    @Override
    public List<Prestamo> obtenerActivosPorIdUsuario(int idUsuario) throws SQLException {
        return obtenerListaDePrestamos(SQL_SELECT_ACTIVOS_BY_ID_USUARIO, idUsuario, "Error al obtener préstamos activos por idUsuario");
    }
    
    @Override
    public List<Prestamo> obtenerPrestamosActivos() throws SQLException {
        return obtenerListaDePrestamos(SQL_SELECT_ACTIVOS, -1, "Error al obtener todos los préstamos activos");
    }

    @Override
    public List<Prestamo> obtenerPrestamosConMoraPendiente() throws SQLException {
        return obtenerListaDePrestamos(SQL_SELECT_CON_MORA_PENDIENTE, -1, "Error al obtener préstamos con mora pendiente");
    }
    
    @Override
    public List<Prestamo> obtenerPrestamosConMoraPendientePorUsuario(int idUsuario) throws SQLException {
        return obtenerListaDePrestamos(SQL_SELECT_CON_MORA_PENDIENTE_POR_USUARIO, idUsuario, "Error al obtener préstamos con mora pendiente para el usuario");
    }

    private List<Prestamo> obtenerListaDePrestamos(String sql, int parametroId, String errorMsg) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Prestamo> prestamos = new ArrayList<>();
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            if (parametroId != -1) { // Si se necesita un parámetro ID
                pstmt.setInt(1, parametroId);
            }
            LogsError.info(this.getClass(), "Ejecutando query: " + sql + (parametroId != -1 ? " con parámetro: " + parametroId : ""));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                prestamos.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), errorMsg + ": " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return prestamos;
    }
    
    @Override
    public int contarPrestamosActivosPorUsuario(int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int conteo = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_COUNT_ACTIVOS_BY_ID_USUARIO);
            pstmt.setInt(1, idUsuario);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_COUNT_ACTIVOS_BY_ID_USUARIO + " para idUsuario: " + idUsuario);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                conteo = rs.getInt(1);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al contar préstamos activos por usuario: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return conteo;
    }

    @Override
    public boolean verificarUsuarioTieneMora(int idUsuario) throws SQLException {
        List<Prestamo> prestamosConMora = obtenerPrestamosConMoraPendientePorUsuario(idUsuario);
        return !prestamosConMora.isEmpty();
    }
}