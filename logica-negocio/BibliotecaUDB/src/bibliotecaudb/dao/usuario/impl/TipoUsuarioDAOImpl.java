package bibliotecaudb.dao.usuario.impl;

import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.dao.usuario.TipoUsuarioDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoUsuarioDAOImpl implements TipoUsuarioDAO {

    private static final String SQL_SELECT_BY_ID = "SELECT id, tipo FROM tipo_usuario WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, tipo FROM tipo_usuario ORDER BY id";
    private static final String SQL_SELECT_BY_NAME = "SELECT id, tipo FROM tipo_usuario WHERE tipo = ?";

    @Override
    public TipoUsuario obtenerPorId(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        TipoUsuario tipoUsuario = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ID + " con ID: " + id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                tipoUsuario = new TipoUsuario();
                tipoUsuario.setId(rs.getInt("id"));
                tipoUsuario.setTipo(rs.getString("tipo"));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener tipo de usuario por ID: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return tipoUsuario;
    }

    @Override
    public List<TipoUsuario> obtenerTodos() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TipoUsuario> tiposUsuario = new ArrayList<>();

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                TipoUsuario tipoUsuario = new TipoUsuario();
                tipoUsuario.setId(rs.getInt("id"));
                tipoUsuario.setTipo(rs.getString("tipo"));
                tiposUsuario.add(tipoUsuario);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todos los tipos de usuario: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return tiposUsuario;
    }

    @Override
    public TipoUsuario obtenerPorNombre(String nombreTipo) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        TipoUsuario tipoUsuario = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_NAME);
            pstmt.setString(1, nombreTipo);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_NAME + " con nombre: " + nombreTipo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                tipoUsuario = new TipoUsuario();
                tipoUsuario.setId(rs.getInt("id"));
                tipoUsuario.setTipo(rs.getString("tipo"));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener tipo de usuario por nombre: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return tipoUsuario;
    }
}