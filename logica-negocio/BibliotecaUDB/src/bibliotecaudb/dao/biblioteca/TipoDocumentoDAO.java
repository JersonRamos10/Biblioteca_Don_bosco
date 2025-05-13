package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.modelo.biblioteca.TipoDocumento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TipoDocumentoDAO {

    // SQL Statements
    private static final String SQL_SELECT_BY_ID = "SELECT id, tipo FROM tipo_documento WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, tipo FROM tipo_documento ORDER BY tipo";
    private static final String SQL_INSERT = "INSERT INTO tipo_documento (tipo) VALUES (?)";
    private static final String SQL_UPDATE = "UPDATE tipo_documento SET tipo = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM tipo_documento WHERE id = ?";

    public TipoDocumentoDAO() {
        // Constructor vacío
    }

    public TipoDocumento obtenerPorId(int id) throws SQLException {
        TipoDocumento tipoDocumento = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                tipoDocumento = mapearResultSetATipoDocumento(rs);
            } else {
                 LogsError.warn(TipoDocumentoDAO.class, "No se encontro TipoDocumento con ID: " + id);
            }
        } catch (SQLException e) {
            LogsError.error(TipoDocumentoDAO.class, "Error al obtener TipoDocumento por ID: " + id, e);
            throw e; // Relanzamos para que la capa de servicio la maneje
        } finally {
            // Usamos los helpers de ConexionBD
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
            
        }
        return tipoDocumento;
    }

    public List<TipoDocumento> obtenerTodos() throws SQLException {
        List<TipoDocumento> tiposDocumento = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                tiposDocumento.add(mapearResultSetATipoDocumento(rs));
            }
            LogsError.info(TipoDocumentoDAO.class, "Se obtuvieron " + tiposDocumento.size() + " tipos de documento.");
        } catch (SQLException e) {
            LogsError.error(TipoDocumentoDAO.class, "Error al obtener todos los TipoDocumentos", e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return tiposDocumento;
    }

    public TipoDocumento crear(TipoDocumento tipoDocumento) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, tipoDocumento.getTipo());
            
            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    tipoDocumento.setId(generatedKeys.getInt(1));
                    LogsError.info(TipoDocumentoDAO.class, "TipoDocumento creado con ID: " + tipoDocumento.getId() + ", Tipo: " + tipoDocumento.getTipo());
                } else {
                     LogsError.error(TipoDocumentoDAO.class, "Fallo al crear TipoDocumento (no se obtuvo ID): " + tipoDocumento.getTipo());
                     throw new SQLException("No se pudo obtener el ID generado para el nuevo tipo de documento.");
                }
            } else {
                LogsError.warn(TipoDocumentoDAO.class, "La creación de TipoDocumento no afectó filas: " + tipoDocumento.getTipo());
                // Considerar si devolver null o lanzar excepción es más apropiado aquí
                return null; 
            }
        } catch (SQLException e) {
            LogsError.error(TipoDocumentoDAO.class, "Error al crear TipoDocumento: " + tipoDocumento.getTipo(), e);
            throw e;
        } finally {
            ConexionBD.close(generatedKeys); 
            ConexionBD.close(pstmt);
        }
        return tipoDocumento;
    }

    public boolean actualizar(TipoDocumento tipoDocumento) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int filasAfectadas = 0;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setString(1, tipoDocumento.getTipo());
            pstmt.setInt(2, tipoDocumento.getId());
            
            filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                LogsError.info(TipoDocumentoDAO.class, "TipoDocumento actualizado: ID " + tipoDocumento.getId() + ", Nuevo Tipo: " + tipoDocumento.getTipo());
            } else {
                LogsError.warn(TipoDocumentoDAO.class, "No se actualizo TipoDocumento (ID no encontrado?): " + tipoDocumento.getId());
            }
        } catch (SQLException e) {
            LogsError.error(TipoDocumentoDAO.class, "Error al actualizar TipoDocumento: " + tipoDocumento.getTipo(), e);
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
                LogsError.info(TipoDocumentoDAO.class, "TipoDocumento eliminado con ID: " + id);
            } else {
                LogsError.warn(TipoDocumentoDAO.class, "No se elimino TipoDocumento (ID no encontrado?): " + id);
            }
        } catch (SQLException e) {
            LogsError.error(TipoDocumentoDAO.class, "Error al eliminar TipoDocumento con ID: " + id, e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return filasAfectadas > 0;
    }

    private TipoDocumento mapearResultSetATipoDocumento(ResultSet resultSet) throws SQLException {
        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setId(resultSet.getInt("id"));
        tipoDocumento.setTipo(resultSet.getString("tipo"));
        return tipoDocumento;
    }
}