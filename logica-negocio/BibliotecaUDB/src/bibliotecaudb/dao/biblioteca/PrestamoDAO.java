package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.modelo.biblioteca.Prestamo;
import bibliotecaudb.modelo.biblioteca.Ejemplar; // Para el objeto Ejemplar
import bibliotecaudb.modelo.usuario.Usuario;   // Para el objeto Usuario
import bibliotecaudb.dao.usuario.UsuarioDAO; // DAO para obtener Usuario 
import bibliotecaudb.dao.biblioteca.EjemplarDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.sql.Date; 
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    private static final String SQL_SELECT_BASE = "SELECT id, id_usuario, id_ejemplar, fecha_prestamo, fecha_devolucion, fecha_limite, mora FROM prestamos";
    private static final String SQL_INSERT = "INSERT INTO prestamos (id_usuario, id_ejemplar, fecha_prestamo, fecha_limite, mora, fecha_devolucion) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE prestamos SET id_usuario = ?, id_ejemplar = ?, fecha_prestamo = ?, fecha_devolucion = ?, fecha_limite = ?, mora = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM prestamos WHERE id = ?"; 

    // Dependencias de otros DAOs para construir el objeto Prestamo completo
    private UsuarioDAO usuarioDAO;
    private EjemplarDAO ejemplarDAO; 

    public PrestamoDAO() {
        this.usuarioDAO = new UsuarioDAO(); // Instanciar DAO de Usuario
        this.ejemplarDAO = new EjemplarDAO();   // Instanciar DAO de Ejemplar
    }

    public Prestamo obtenerPorId(int id) throws SQLException {
        String sql = SQL_SELECT_BASE + " WHERE id = ?";
        Prestamo prestamo = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                prestamo = mapearResultSetAPrestamo(rs);
            } else {
                 LogsError.warn(PrestamoDAO.class, "No se encontro Prestamo con ID: " + id);
            }
        } catch (SQLException e) {
            LogsError.error(PrestamoDAO.class, "Error al obtener Prestamo por ID: " + id, e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return prestamo;
    }

    public List<Prestamo> obtenerTodos() throws SQLException {
        String sql = SQL_SELECT_BASE + " ORDER BY fecha_prestamo DESC";
        List<Prestamo> prestamos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                prestamos.add(mapearResultSetAPrestamo(rs));
            }
             LogsError.info(PrestamoDAO.class, "Se obtuvieron " + prestamos.size() + " prestamos.");
        } catch (SQLException e) {
            LogsError.error(PrestamoDAO.class, "Error al obtener todos los Prestamos", e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return prestamos;
    }
    
    public List<Prestamo> obtenerPrestamosActivosPorUsuario(int idUsuario) throws SQLException {
        // Ajustamos la consulta para que solo traiga los IDs necesarios para el mapeo posterior
        String sql = "SELECT p.id, p.id_usuario, p.id_ejemplar, p.fecha_prestamo, p.fecha_devolucion, p.fecha_limite, p.mora " +
                     "FROM prestamos p " + // No es necesario el JOIN a ejemplares aquí si el mapeador lo resuelve
                     "WHERE p.id_usuario = ? AND p.fecha_devolucion IS NULL " +
                     "ORDER BY p.fecha_limite ASC";
        List<Prestamo> prestamos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idUsuario);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                prestamos.add(mapearResultSetAPrestamo(rs));
            }
            LogsError.info(PrestamoDAO.class, "Se obtuvieron " + prestamos.size() + " prestamos activos para el usuario ID: " + idUsuario);
        } catch (SQLException e) {
            LogsError.error(PrestamoDAO.class, "Error al obtener prestamos activos para el usuario ID: " + idUsuario, e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return prestamos;
    }

    public List<Prestamo> obtenerPrestamosVencidosNoDevueltos() throws SQLException {
        String sql = SQL_SELECT_BASE + " WHERE fecha_devolucion IS NULL AND fecha_limite < CURRENT_DATE()";
        List<Prestamo> prestamosVencidos = new ArrayList<>();
         Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while(rs.next()){
                prestamosVencidos.add(mapearResultSetAPrestamo(rs));
            }
            LogsError.info(PrestamoDAO.class, "Se obtuvieron " + prestamosVencidos.size() + " préstamos vencidos no devueltos.");
        } catch (SQLException e) {
             LogsError.error(PrestamoDAO.class, "Error al obtener préstamos vencidos no devueltos.", e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return prestamosVencidos;
    }


    public Prestamo crear(Prestamo prestamo) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        // Validaciones básicas
        if (prestamo.getUsuario() == null || prestamo.getUsuario().getId() <= 0) {
            throw new SQLException("Usuario no válido para el préstamo.");
        }
        if (prestamo.getEjemplar() == null || prestamo.getEjemplar().getId() <= 0) {
            throw new SQLException("Ejemplar no válido para el préstamo.");
        }
        if (prestamo.getFechaPrestamo() == null || prestamo.getFechaLimite() == null) {
            throw new SQLException("Las fechas de préstamo y límite son requeridas.");
        }

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, prestamo.getUsuario().getId());
            pstmt.setInt(2, prestamo.getEjemplar().getId());
            pstmt.setDate(3, prestamo.getFechaPrestamo()); // Usar java.sql.Date
            pstmt.setDate(4, prestamo.getFechaLimite());   // Usar java.sql.Date

            // Mora por defecto es 0, pero si viene en el objeto la usamos
            if (prestamo.getMora() != null) {
                pstmt.setBigDecimal(5, prestamo.getMora());
            } else {
                pstmt.setBigDecimal(5, BigDecimal.ZERO);
            }
             // fecha_devolucion es null al crear
            if (prestamo.getFechaDevolucion() != null) {
                pstmt.setDate(6, prestamo.getFechaDevolucion());
            } else {
                pstmt.setNull(6, Types.DATE);
            }


            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    prestamo.setId(generatedKeys.getInt(1));
                    LogsError.info(PrestamoDAO.class, "Prestamo creado con ID: " + prestamo.getId());
                } else {
                     LogsError.error(PrestamoDAO.class, "Fallo al crear Prestamo (no se obtuvo ID).");
                     throw new SQLException("No se pudo obtener el ID generado para el nuevo prestamo.");
                }
            } else {
                LogsError.warn(PrestamoDAO.class, "La creación de Prestamo no afectó filas.");
                return null; // O lanzar excepción
            }
        } catch (SQLException e) {
            LogsError.error(PrestamoDAO.class, "Error al crear Prestamo para Usuario ID: " + prestamo.getUsuario().getId() + ", Ejemplar ID: " + prestamo.getEjemplar().getId(), e);
            throw e;
        } finally {
            ConexionBD.close(generatedKeys);
            ConexionBD.close(pstmt);
        }
        return prestamo;
    }

    public boolean actualizar(Prestamo prestamo) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int filasAfectadas = 0;
        
        if (prestamo.getId() <= 0) {
            throw new SQLException("ID de préstamo no válido para actualizar.");
        }
        // Más validaciones como en crear...

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setInt(1, prestamo.getUsuario().getId());
            pstmt.setInt(2, prestamo.getEjemplar().getId());
            pstmt.setDate(3, prestamo.getFechaPrestamo());
            
            if (prestamo.getFechaDevolucion() != null) {
                pstmt.setDate(4, prestamo.getFechaDevolucion());
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            pstmt.setDate(5, prestamo.getFechaLimite());
            pstmt.setBigDecimal(6, prestamo.getMora() != null ? prestamo.getMora() : BigDecimal.ZERO);
            pstmt.setInt(7, prestamo.getId());

            filasAfectadas = pstmt.executeUpdate();
             if (filasAfectadas > 0) {
                LogsError.info(PrestamoDAO.class, "Prestamo actualizado con ID: " + prestamo.getId());
            } else {
                LogsError.warn(PrestamoDAO.class, "No se actualizó Prestamo (ID no encontrado?): " + prestamo.getId());
            }
        } catch (SQLException e) {
            LogsError.error(PrestamoDAO.class, "Error al actualizar Prestamo con ID: " + prestamo.getId(), e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return filasAfectadas > 0;
    }

    public boolean eliminar(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int filasAfectadas = 0;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_DELETE);
            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                LogsError.info(PrestamoDAO.class, "Préstamo eliminado con ID: " + id);
            } else {
                LogsError.warn(PrestamoDAO.class, "No se elimino Prestamo (ID no encontrado?): " + id);
            }
        } catch (SQLException e) {
            LogsError.error(PrestamoDAO.class, "Error al eliminar Prestamo con ID: " + id, e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return filasAfectadas > 0;
    }

    private Prestamo mapearResultSetAPrestamo(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo();
        prestamo.setId(rs.getInt("id"));
        
        // Obtener Usuario completo
        int idUsuario = rs.getInt("id_usuario");
        if (!rs.wasNull()) {
            Usuario usuario = usuarioDAO.obtenerPorId(idUsuario);
            if (usuario != null) {
                prestamo.setUsuario(usuario);
            } else {
                LogsError.warn(PrestamoDAO.class, "Usuario no encontrado para ID: " + idUsuario + " al mapear Prestamo ID: " + prestamo.getId());
            }
        }
        
        // Obtener Ejemplar completo
        int idEjemplar = rs.getInt("id_ejemplar");
        if (!rs.wasNull()) {
            Ejemplar ejemplar = ejemplarDAO.obtenerPorId(idEjemplar);
            if (ejemplar != null) {
                prestamo.setEjemplar(ejemplar);
            } else {
                 LogsError.warn(PrestamoDAO.class, "Ejemplar no encontrado para ID: " + idEjemplar + " al mapear Prestamo ID: " + prestamo.getId());
            }
        }

        prestamo.setFechaPrestamo(rs.getDate("fecha_prestamo")); // Obtener como java.sql.Date
        prestamo.setFechaDevolucion(rs.getDate("fecha_devolucion")); // Puede ser null
        prestamo.setFechaLimite(rs.getDate("fecha_limite"));
        
        // Obtener mora como BigDecimal, puede ser null
        BigDecimal mora = rs.getBigDecimal("mora");
        if (!rs.wasNull()) {
            prestamo.setMora(mora);
        } else {
            prestamo.setMora(BigDecimal.ZERO); // O null, según la lógica de tu modelo
        }
        return prestamo;
    }
}