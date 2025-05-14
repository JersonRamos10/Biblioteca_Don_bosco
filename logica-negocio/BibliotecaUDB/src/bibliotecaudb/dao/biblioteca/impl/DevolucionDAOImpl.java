package bibliotecaudb.dao.biblioteca.impl;

import bibliotecaudb.modelo.biblioteca.Devolucion;
import bibliotecaudb.modelo.biblioteca.Prestamo;
import bibliotecaudb.dao.biblioteca.DevolucionDAO;
import bibliotecaudb.dao.biblioteca.PrestamoDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date; // Para convertir LocalDate
import java.sql.Types; // Para setNull
import java.util.ArrayList;
import java.util.List;

public class DevolucionDAOImpl implements DevolucionDAO {

    private static final String SQL_INSERT = "INSERT INTO devoluciones (id_prestamo, fecha_devolucion, mora_pagada) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_BY_ID = "SELECT id, id_prestamo, fecha_devolucion, mora_pagada FROM devoluciones WHERE id = ?";
    private static final String SQL_SELECT_BY_ID_PRESTAMO = "SELECT id, id_prestamo, fecha_devolucion, mora_pagada FROM devoluciones WHERE id_prestamo = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, id_prestamo, fecha_devolucion, mora_pagada FROM devoluciones ORDER BY fecha_devolucion DESC";

    private PrestamoDAO prestamoDAO;

    public DevolucionDAOImpl() {
        this.prestamoDAO = new PrestamoDAOImpl(); // Instanciación directa
    }
    
    // Constructor para inyeccion de dependencias
    public DevolucionDAOImpl(PrestamoDAO prestamoDAO) {
        this.prestamoDAO = prestamoDAO;
    }

    @Override
    public boolean insertar(Devolucion devolucion) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            // El trigger tg_update_ejemplar_estado_disponible se encargara de cambiar el estado del ejemplar.
            // Y el método registrarDevolucion en PrestamoDAO ya actualiza la tabla prestamos.
            // Este DAO se enfoca en la tabla 'devoluciones'.
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, devolucion.getIdPrestamo());
            pstmt.setDate(2, Date.valueOf(devolucion.getFechaDevolucion()));
            if (devolucion.getMoraPagada() != null) {
                pstmt.setBigDecimal(3, devolucion.getMoraPagada());
            } else {
                pstmt.setNull(3, Types.DECIMAL); // O un valor por defecto como 0.00
            }

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_INSERT);
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    devolucion.setId(generatedKeys.getInt(1));
                }
                LogsError.info(this.getClass(), "Devolucion insertada con ID: " + devolucion.getId());
            } else {
                LogsError.warn(this.getClass(), "No se inserto la Devolucion.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar devolucion: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(generatedKeys);
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    private Devolucion mapearResultSet(ResultSet rs) throws SQLException {
        Devolucion d = new Devolucion();
        d.setId(rs.getInt("id"));
        d.setIdPrestamo(rs.getInt("id_prestamo"));
        
        Date sqlFechaDevolucion = rs.getDate("fecha_devolucion");
        if (sqlFechaDevolucion != null) d.setFechaDevolucion(sqlFechaDevolucion.toLocalDate());
        
        d.setMoraPagada(rs.getBigDecimal("mora_pagada"));

        if (this.prestamoDAO != null) {
            Prestamo p = this.prestamoDAO.obtenerPorId(d.getIdPrestamo());
            d.setPrestamo(p);
        }
        return d;
    }

    @Override
    public Devolucion obtenerPorId(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Devolucion devolucion = null;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ID + " con ID: " + id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                devolucion = mapearResultSet(rs);
            } else {
                LogsError.warn(this.getClass(), "No se encontro Devolucion con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener devolucion por ID: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return devolucion;
    }

    @Override
    public List<Devolucion> obtenerPorIdPrestamo(int idPrestamo) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Devolucion> devoluciones = new ArrayList<>();
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID_PRESTAMO);
            pstmt.setInt(1, idPrestamo);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ID_PRESTAMO + " para idPrestamo: " + idPrestamo);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                devoluciones.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener devoluciones por idPrestamo: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return devoluciones;
    }

    @Override
    public List<Devolucion> obtenerTodas() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Devolucion> devoluciones = new ArrayList<>();
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                devoluciones.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todas las devoluciones: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return devoluciones;
    }
}