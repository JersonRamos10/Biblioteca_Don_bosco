package bibliotecaudb.dao.biblioteca.impl;

import bibliotecaudb.modelo.biblioteca.MoraAnual;
import bibliotecaudb.dao.biblioteca.MoraAnualDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MoraAnualDAOImpl implements MoraAnualDAO {

    private static final String SQL_INSERT = "INSERT INTO mora_anual (anio, mora_diaria) VALUES (?, ?)";
    private static final String SQL_UPDATE = "UPDATE mora_anual SET mora_diaria = ? WHERE anio = ?";
    private static final String SQL_SELECT_BY_ANIO = "SELECT anio, mora_diaria FROM mora_anual WHERE anio = ?";
    private static final String SQL_SELECT_ALL = "SELECT anio, mora_diaria FROM mora_anual ORDER BY anio DESC";
    private static final String SQL_DELETE_BY_ANIO = "DELETE FROM mora_anual WHERE anio = ?";

    @Override
    public boolean insertar(MoraAnual moraAnual) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();//abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_INSERT);
            pstmt.setInt(1, moraAnual.getAnio());
            pstmt.setBigDecimal(2, moraAnual.getMoraDiaria());

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_INSERT);
            rowsAffected = pstmt.executeUpdate();
            LogsError.info(this.getClass(), "MoraAnual insertada para el año: " + moraAnual.getAnio() + ". Filas afectadas: " + rowsAffected);
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar MoraAnual: " + ex.getMessage(), ex);
            // Podría fallar si el año ya existe (PK duplicada)
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    @Override
    public boolean actualizar(MoraAnual moraAnual) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();//abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setBigDecimal(1, moraAnual.getMoraDiaria());
            pstmt.setInt(2, moraAnual.getAnio()); // Condición WHERE

            LogsError.info(this.getClass(), "Actualizando MoraAnual para el año: " + moraAnual.getAnio());
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "MoraAnual actualizada. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontró MoraAnual para actualizar para el año: " + moraAnual.getAnio() + " o el valor era el mismo.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar MoraAnual: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    private MoraAnual mapearResultSet(ResultSet rs) throws SQLException {
        MoraAnual ma = new MoraAnual();
        ma.setAnio(rs.getInt("anio"));
        ma.setMoraDiaria(rs.getBigDecimal("mora_diaria"));
        return ma;
    }

    @Override
    public MoraAnual obtenerPorAnio(int anio) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        MoraAnual moraAnual = null;
        try {
            conn = ConexionBD.getConexion(); //abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ANIO);
            pstmt.setInt(1, anio);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ANIO + " para año: " + anio);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                moraAnual = mapearResultSet(rs);
            } else {
                LogsError.warn(this.getClass(), "No se encontro MoraAnual para el año: " + anio);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener MoraAnual por año: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return moraAnual;
    }

    @Override
    public List<MoraAnual> obtenerTodas() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<MoraAnual> morasAnuales = new ArrayList<>();
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                morasAnuales.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todas las MoraAnual: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return morasAnuales;
    }
    
    @Override
    public boolean eliminar(int anio) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_DELETE_BY_ANIO);
            pstmt.setInt(1, anio);

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_DELETE_BY_ANIO + " para año: " + anio);
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "MoraAnual para el año " + anio + " eliminada. Filas afectadas: " + rowsAffected);
            } else {
                 LogsError.warn(this.getClass(), "No se encontro MoraAnual para eliminar para el año: " + anio);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al eliminar MoraAnual: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }
}
