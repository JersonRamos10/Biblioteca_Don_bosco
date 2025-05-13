package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.modelo.biblioteca.Devolucion;
import bibliotecaudb.modelo.biblioteca.Prestamo; // Para el objeto Prestamo
// import bibliotecaudb.dao.biblioteca.PrestamoDAO; // Para obtener el Prestamo completo

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.sql.Date; // Usar java.sql.Date
import java.math.BigDecimal; // Usar BigDecimal
import java.util.ArrayList;
import java.util.List;

public class DevolucionDAO {

    private static final String SQL_SELECT_BASE = "SELECT id, id_prestamo, fecha_devolucion, mora_pagada FROM devoluciones";
    private static final String SQL_INSERT = "INSERT INTO devoluciones (id_prestamo, fecha_devolucion, mora_pagada) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE devoluciones SET id_prestamo = ?, fecha_devolucion = ?, mora_pagada = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM devoluciones WHERE id = ?"; // Usar con precaución

    // Dependencia para obtener el objeto Prestamo completo
    private PrestamoDAO prestamoDAO;

    public DevolucionDAO() {
        this.prestamoDAO = new PrestamoDAO(); // Instanciar el DAO dependiente
    }

    public Devolucion obtenerPorId(int id) throws SQLException {
        String sql = SQL_SELECT_BASE + " WHERE id = ?";
        Devolucion devolucion = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                devolucion = mapearResultSetADevolucion(rs);
            } else {
                LogsError.warn(DevolucionDAO.class, "No se encontro Devolucion con ID: " + id);
            }
        } catch (SQLException e) {
            LogsError.error(DevolucionDAO.class, "Error al obtener Devolucion por ID: " + id, e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return devolucion;
    }

    public List<Devolucion> obtenerTodos() throws SQLException {
        String sql = SQL_SELECT_BASE + " ORDER BY fecha_devolucion DESC";
        List<Devolucion> devoluciones = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                devoluciones.add(mapearResultSetADevolucion(rs));
            }
             LogsError.info(DevolucionDAO.class, "Se obtuvieron " + devoluciones.size() + " devoluciones.");
        } catch (SQLException e) {
            LogsError.error(DevolucionDAO.class, "Error al obtener todas las Devoluciones", e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return devoluciones;
    }

    public Devolucion crear(Devolucion devolucion) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        if (devolucion.getPrestamo() == null || devolucion.getPrestamo().getId() <= 0) {
            throw new SQLException("Se requiere un Préstamo válido para crear una Devolución.");
        }
        if (devolucion.getFechaDevolucion() == null) {
             throw new SQLException("La fecha de devolución es requerida.");
        }

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, devolucion.getPrestamo().getId());
            pstmt.setDate(2, devolucion.getFechaDevolucion()); // Usar java.sql.Date
            
            if (devolucion.getMoraPagada() != null) {
                pstmt.setBigDecimal(3, devolucion.getMoraPagada()); // Usar BigDecimal
            } else {
                pstmt.setNull(3, Types.DECIMAL); // Permitir NULL si no hay mora pagada
            }
            
            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    devolucion.setId(generatedKeys.getInt(1));
                     LogsError.info(DevolucionDAO.class, "Devolucion creada con ID: " + devolucion.getId() + " para Prestamo ID: " + devolucion.getPrestamo().getId());
                } else {
                     LogsError.error(DevolucionDAO.class, "Fallo al crear Devolucion (no se obtuvo ID) para Prestamo ID: " + devolucion.getPrestamo().getId());
                     throw new SQLException("No se pudo obtener el ID generado para la nueva devolución.");
                }
            } else {
                LogsError.warn(DevolucionDAO.class, "La creación de Devolucion no afecto filas para Prestamo ID: " + devolucion.getPrestamo().getId());
                return null;
            }
        } catch (SQLException e) {
            LogsError.error(DevolucionDAO.class, "Error al crear Devolucion para Prestamo ID: " + devolucion.getPrestamo().getId(), e);
            throw e;
        } finally {
            ConexionBD.close(generatedKeys);
            ConexionBD.close(pstmt);
        }
        return devolucion;
    }

    public boolean actualizar(Devolucion devolucion) throws SQLException {
        // Actualizar una devolución podría ser menos común que crearla.
        Connection conn = null;
        PreparedStatement pstmt = null;
        int filasAfectadas = 0;

        if (devolucion.getId() <= 0) {
            throw new SQLException("ID de devolución no válido para actualizar.");
        }
        // Más validaciones...

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setInt(1, devolucion.getPrestamo().getId());
            pstmt.setDate(2, devolucion.getFechaDevolucion()); 
            if (devolucion.getMoraPagada() != null) {
                pstmt.setBigDecimal(3, devolucion.getMoraPagada());
            } else {
                pstmt.setNull(3, Types.DECIMAL);
            }
            pstmt.setInt(4, devolucion.getId());

            filasAfectadas = pstmt.executeUpdate();
             if (filasAfectadas > 0) {
                LogsError.info(DevolucionDAO.class, "Devolucion actualizada con ID: " + devolucion.getId());
            } else {
                LogsError.warn(DevolucionDAO.class, "No se actualizo Devolución (ID no encontrado?): " + devolucion.getId());
            }
        } catch (SQLException e) {
            LogsError.error(DevolucionDAO.class, "Error al actualizar Devolucion con ID: " + devolucion.getId(), e);
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
                LogsError.info(DevolucionDAO.class, "Devolucion eliminada con ID: " + id);
            } else {
                LogsError.warn(DevolucionDAO.class, "No se elimino Devolución (ID no encontrado?): " + id);
            }
        } catch (SQLException e) {
            LogsError.error(DevolucionDAO.class, "Error al eliminar Devolucion con ID: " + id, e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return filasAfectadas > 0;
    }

    private Devolucion mapearResultSetADevolucion(ResultSet rs) throws SQLException {
        Devolucion devolucion = new Devolucion();
        devolucion.setId(rs.getInt("id"));
        devolucion.setFechaDevolucion(rs.getDate("fecha_devolucion")); // Usar java.sql.Date
        
        // Mora pagada puede ser null
        BigDecimal moraPagada = rs.getBigDecimal("mora_pagada"); // Usar BigDecimal
        if (!rs.wasNull()) {
            devolucion.setMoraPagada(moraPagada);
        } else {
            devolucion.setMoraPagada(null); // O BigDecimal.ZERO si prefieres no nulos
        }
        
        // Obtener el Prestamo asociado
        int idPrestamo = rs.getInt("id_prestamo");
        if (!rs.wasNull()) {
            Prestamo prestamo = prestamoDAO.obtenerPorId(idPrestamo); // Llama al DAO de Prestamo
            if (prestamo != null) {
                devolucion.setPrestamo(prestamo);
            } else {
                 LogsError.warn(DevolucionDAO.class, "Prestamo no encontrado para ID: " + idPrestamo + " al mapear Devolucion ID: " + devolucion.getId());
            }
        }
        return devolucion;
    }
}