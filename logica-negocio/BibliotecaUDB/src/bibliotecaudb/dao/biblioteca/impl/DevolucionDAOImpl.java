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
import java.sql.Date; // Para convertir la fecha de Java a fecha de SQL
import java.sql.Types; // Para poder poner valores nulos en la BD
import java.util.ArrayList;
import java.util.List;

public class DevolucionDAOImpl implements DevolucionDAO {

    private static final String SQL_INSERT = "INSERT INTO devoluciones (id_prestamo, fecha_devolucion, mora_pagada) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_BY_ID = "SELECT id, id_prestamo, fecha_devolucion, mora_pagada FROM devoluciones WHERE id = ?";
    private static final String SQL_SELECT_BY_ID_PRESTAMO = "SELECT id, id_prestamo, fecha_devolucion, mora_pagada FROM devoluciones WHERE id_prestamo = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, id_prestamo, fecha_devolucion, mora_pagada FROM devoluciones ORDER BY fecha_devolucion DESC";

    private PrestamoDAO prestamoDAO; // Objeto para acceder a los datos de los prestamos

    public DevolucionDAOImpl() {
        this.prestamoDAO = new PrestamoDAOImpl(); // Creamos un objeto para manejar prestamos
    }

    // Constructor por si necesitamos pasarle el manejador de prestamos
    public DevolucionDAOImpl(PrestamoDAO prestamoDAO) {
        this.prestamoDAO = prestamoDAO;
    }

    @Override
    public boolean insertar(Devolucion devolucion) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet generatedKeys = null; // Para obtener el ID generado
        int rowsAffected = 0; // Para saber cuantas filas se afectaron
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            // El trigger en la BD (tg_update_ejemplar_estado_disponible) deberia cambiar el estado del ejemplar.
            // Y el metodo registrarDevolucion en PrestamoDAO ya actualiza la tabla de prestamos.
            // Esta clase solo se encarga de la tabla 'devoluciones'.
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, devolucion.getIdPrestamo());
            pstmt.setDate(2, Date.valueOf(devolucion.getFechaDevolucion())); // Convertimos la fecha de Java a SQL
            if (devolucion.getMoraPagada() != null) {
                pstmt.setBigDecimal(3, devolucion.getMoraPagada());
            } else {
                pstmt.setNull(3, Types.DECIMAL); // Si no hay mora, guardamos nulo o podria ser 0.00
            }

            LogsError.info(this.getClass(), "Ejecutando consulta para insertar devolucion: " + SQL_INSERT);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la insercion
            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys(); // Obtenemos el ID generado
                if (generatedKeys.next()) {
                    devolucion.setId(generatedKeys.getInt(1)); // Asignamos el ID al objeto devolucion
                }
                LogsError.info(this.getClass(), "Devolucion insertada con ID: " + devolucion.getId());
            } else {
                LogsError.warn(this.getClass(), "No se inserto la Devolucion.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar devolucion: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(generatedKeys); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se inserto algo
    }

    // Este metodo convierte los datos de un ResultSet a un objeto Devolucion
    private Devolucion mapearResultSet(ResultSet rs) throws SQLException {
        Devolucion d = new Devolucion(); // Creamos un nuevo objeto Devolucion
        d.setId(rs.getInt("id"));
        d.setIdPrestamo(rs.getInt("id_prestamo"));

        Date sqlFechaDevolucion = rs.getDate("fecha_devolucion"); // Obtenemos la fecha de SQL
        if (sqlFechaDevolucion != null) d.setFechaDevolucion(sqlFechaDevolucion.toLocalDate()); // La convertimos a fecha de Java

        d.setMoraPagada(rs.getBigDecimal("mora_pagada"));

        if (this.prestamoDAO != null) { // Si tenemos el objeto para manejar prestamos
            Prestamo p = this.prestamoDAO.obtenerPorId(d.getIdPrestamo()); // Buscamos el prestamo asociado
            d.setPrestamo(p); // Asignamos el objeto Prestamo completo a la devolucion
        }
        return d; // Devolvemos la devolucion
    }

    @Override
    public Devolucion obtenerPorId(int id) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        Devolucion devolucion = null; // La devolucion que devolveremos
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id); // El ID de la devolucion que buscamos
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener devolucion por ID: " + SQL_SELECT_BY_ID + " con ID: " + id);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            if (rs.next()) { // Si hay resultado
                devolucion = mapearResultSet(rs); // Convertimos el resultado a objeto Devolucion
            } else {
                LogsError.warn(this.getClass(), "No se encontro Devolucion con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener devolucion por ID: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return devolucion; // Devolvemos la devolucion o null si no se encontro
    }

    @Override
    public List<Devolucion> obtenerPorIdPrestamo(int idPrestamo) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        List<Devolucion> devoluciones = new ArrayList<>(); // Lista para las devoluciones de un prestamo
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID_PRESTAMO);
            pstmt.setInt(1, idPrestamo); // El ID del prestamo para el que buscamos devoluciones
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener devoluciones por idPrestamo: " + SQL_SELECT_BY_ID_PRESTAMO + " para idPrestamo: " + idPrestamo);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            while (rs.next()) { // Mientras haya resultados
                devoluciones.add(mapearResultSet(rs)); // Agregamos la devolucion a la lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener devoluciones por idPrestamo: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return devoluciones; // Devolvemos la lista de devoluciones
    }

    @Override
    public List<Devolucion> obtenerTodas() throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        List<Devolucion> devoluciones = new ArrayList<>(); // Lista para todas las devoluciones
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener todas las devoluciones: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            while (rs.next()) { // Mientras haya resultados
                devoluciones.add(mapearResultSet(rs)); // Agregamos la devolucion a la lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todas las devoluciones: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return devoluciones; // Devolvemos la lista de todas las devoluciones
    }
}