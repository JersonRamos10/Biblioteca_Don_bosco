package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.modelo.biblioteca.MoraAnual;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MoraAnualDAO {

    private static final String SQL_SELECT_BY_ANIO = "SELECT anio, mora_diaria FROM mora_anual WHERE anio = ?";
    private static final String SQL_SELECT_ALL = "SELECT anio, mora_diaria FROM mora_anual ORDER BY anio DESC";
    private static final String SQL_INSERT = "INSERT INTO mora_anual (anio, mora_diaria) VALUES (?, ?)";
    private static final String SQL_UPDATE = "UPDATE mora_anual SET mora_diaria = ? WHERE anio = ?";
    private static final String SQL_DELETE = "DELETE FROM mora_anual WHERE anio = ?";

    public MoraAnualDAO() {
        // Constructor
    }

    public MoraAnual obtenerPorAnio(int anio) throws SQLException {
        MoraAnual moraAnual = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ANIO);
            pstmt.setInt(1, anio);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                moraAnual = mapearResultSetAMoraAnual(rs);
            } else {
                LogsError.warn(MoraAnualDAO.class, "No se encontró MoraAnual para el año: " + anio);
            }
        } catch (SQLException e) {
            LogsError.error(MoraAnualDAO.class, "Error al obtener MoraAnual para el año: " + anio, e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return moraAnual;
    }

    public List<MoraAnual> obtenerTodas() throws SQLException {
        List<MoraAnual> moras = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                moras.add(mapearResultSetAMoraAnual(rs));
            }
            LogsError.info(MoraAnualDAO.class, "Se obtuvieron " + moras.size() + " registros de mora anual.");
        } catch (SQLException e) {
            LogsError.error(MoraAnualDAO.class, "Error al obtener todas las Moras Anuales", e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return moras;
    }

    public boolean crearOActualizar(MoraAnual moraAnual) throws SQLException {
        // Intentar actualizar primero, si no afecta filas, intentar insertar
        MoraAnual existente = obtenerPorAnio(moraAnual.getAnio());
        if (existente != null) {
            return actualizar(moraAnual);
        } else {
            return crear(moraAnual);
        }
    }


    private boolean crear(MoraAnual moraAnual) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int filasAfectadas = 0;

        if (moraAnual.getMoraDiaria() == null || moraAnual.getMoraDiaria().compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("La mora diaria no puede ser nula o negativa.");
        }

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT);
            pstmt.setInt(1, moraAnual.getAnio());
            pstmt.setBigDecimal(2, moraAnual.getMoraDiaria());

            filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                LogsError.info(MoraAnualDAO.class, "MoraAnual creada para el año: " + moraAnual.getAnio());
            } else {
                 LogsError.warn(MoraAnualDAO.class, "La creación de MoraAnual no afectó filas para el año: " + moraAnual.getAnio());
            }
        } catch (SQLException e) {
            LogsError.error(MoraAnualDAO.class, "Error al crear MoraAnual para el año: " + moraAnual.getAnio(), e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return filasAfectadas > 0;
    }

    private boolean actualizar(MoraAnual moraAnual) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int filasAfectadas = 0;

        if (moraAnual.getMoraDiaria() == null || moraAnual.getMoraDiaria().compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("La mora diaria no puede ser nula o negativa para actualizar.");
        }

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setBigDecimal(1, moraAnual.getMoraDiaria());
            pstmt.setInt(2, moraAnual.getAnio()); // WHERE anio = ?

            filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                LogsError.info(MoraAnualDAO.class, "MoraAnual actualizada para el año: " + moraAnual.getAnio());
            } else {
                LogsError.warn(MoraAnualDAO.class, "No se actualizó MoraAnual (año no encontrado?): " + moraAnual.getAnio());
            }
        } catch (SQLException e) {
            LogsError.error(MoraAnualDAO.class, "Error al actualizar MoraAnual para el año: " + moraAnual.getAnio(), e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return filasAfectadas > 0;
    }

    public boolean eliminar(int anio) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int filasAfectadas = 0;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_DELETE);
            pstmt.setInt(1, anio);

            filasAfectadas = pstmt.executeUpdate();
             if (filasAfectadas > 0) {
                LogsError.info(MoraAnualDAO.class, "MoraAnual eliminada para el año: " + anio);
            } else {
                LogsError.warn(MoraAnualDAO.class, "No se eliminó MoraAnual (año no encontrado?): " + anio);
            }
        } catch (SQLException e) {
            LogsError.error(MoraAnualDAO.class, "Error al eliminar MoraAnual para el año: " + anio, e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return filasAfectadas > 0;
    }

    private MoraAnual mapearResultSetAMoraAnual(ResultSet rs) throws SQLException {
        MoraAnual mora = new MoraAnual();
        mora.setAnio(rs.getInt("anio"));
        mora.setMoraDiaria(rs.getBigDecimal("mora_diaria"));
        if (rs.wasNull()){
            mora.setMoraDiaria(null); // O BigDecimal.ZERO
        }
        return mora;
    }
}