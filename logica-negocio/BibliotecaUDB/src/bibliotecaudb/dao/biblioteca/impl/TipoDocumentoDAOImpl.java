package bibliotecaudb.dao.biblioteca.impl;

import bibliotecaudb.modelo.biblioteca.TipoDocumento;
import bibliotecaudb.dao.biblioteca.TipoDocumentoDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TipoDocumentoDAOImpl implements TipoDocumentoDAO {

    private static final String SQL_INSERT = "INSERT INTO tipo_documento (tipo) VALUES (?)";
    private static final String SQL_UPDATE = "UPDATE tipo_documento SET tipo = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM tipo_documento WHERE id = ?";
    private static final String SQL_SELECT_BY_ID = "SELECT id, tipo FROM tipo_documento WHERE id = ?";
    private static final String SQL_SELECT_BY_NAME = "SELECT id, tipo FROM tipo_documento WHERE tipo = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, tipo FROM tipo_documento ORDER BY tipo";

    @Override
    public boolean insertar(TipoDocumento tipoDocumento) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, tipoDocumento.getTipo());

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_INSERT + " con tipo: " + tipoDocumento.getTipo());
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    tipoDocumento.setId(generatedKeys.getInt(1));
                }
                LogsError.info(this.getClass(), "TipoDocumento insertado con ID: " + tipoDocumento.getId());
            } else {
                LogsError.warn(this.getClass(), "No se insertó el TipoDocumento.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar tipo de documento: " + ex.getMessage(), ex);
            // Podría ser por violar UNIQUE key de tipo si ya existe
            throw ex;
        } finally {
            ConexionBD.close(generatedKeys);
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    @Override
    public boolean actualizar(TipoDocumento tipoDocumento) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setString(1, tipoDocumento.getTipo());
            pstmt.setInt(2, tipoDocumento.getId());

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_UPDATE + " para ID: " + tipoDocumento.getId());
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                 LogsError.info(this.getClass(), "TipoDocumento actualizado. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontró TipoDocumento para actualizar con ID: " + tipoDocumento.getId() + " o el valor era el mismo.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar tipo de documento: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_DELETE);
            pstmt.setInt(1, id);

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_DELETE + " para ID: " + id);
            rowsAffected = pstmt.executeUpdate();
             if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "TipoDocumento eliminado. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontró TipoDocumento para eliminar con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al eliminar tipo de documento: " + ex.getMessage(), ex);
            // Podría fallar si hay documentos referenciando este tipo.
            // La BD no especifica ON DELETE para documentos -> tipo_documento, así que podría dar error de FK.
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    private TipoDocumento mapearResultSet(ResultSet rs) throws SQLException {
        TipoDocumento td = new TipoDocumento();
        td.setId(rs.getInt("id"));
        td.setTipo(rs.getString("tipo"));
        return td;
    }

    @Override
    public TipoDocumento obtenerPorId(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        TipoDocumento tipoDocumento = null;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ID + " con ID: " + id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                tipoDocumento = mapearResultSet(rs);
            } else {
                LogsError.warn(this.getClass(), "No se encontró TipoDocumento con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener tipo de documento por ID: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return tipoDocumento;
    }
    
    @Override
    public TipoDocumento obtenerPorNombre(String nombreTipo) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        TipoDocumento tipoDocumento = null;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_NAME);
            pstmt.setString(1, nombreTipo);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_NAME + " con nombre: " + nombreTipo);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                tipoDocumento = mapearResultSet(rs);
            } else {
                LogsError.warn(this.getClass(), "No se encontró TipoDocumento con nombre: " + nombreTipo);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener tipo de documento por nombre: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return tipoDocumento;
    }

    @Override
    public List<TipoDocumento> obtenerTodos() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TipoDocumento> tiposDocumento = new ArrayList<>();
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                tiposDocumento.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todos los tipos de documento: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return tiposDocumento;
    }
}