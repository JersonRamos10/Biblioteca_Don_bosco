/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.dao.usuario;

import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.modelo.usuario.TipoUsuario; 
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author jerson_ramos
 */
public class TipoUsuarioDAO {
   public TipoUsuario obtenerPorId(int id) throws SQLException {
        TipoUsuario tipoUsuario = null;
        String sql = "SELECT id, tipo FROM tipo_usuario WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                tipoUsuario = new TipoUsuario();
                tipoUsuario.setId(rs.getInt("id"));
                tipoUsuario.setTipo(rs.getString("tipo"));
                // LogsError.info(TipoUsuarioDAO.class, "TipoUsuario encontrado por ID: " + id); // Log opcional de éxito
            } else {
                 LogsError.warn(TipoUsuarioDAO.class, "No se encontró TipoUsuario con ID: " + id);
            }
        } catch (SQLException e) {
             LogsError.error(TipoUsuarioDAO.class, "Error al obtener tipo de usuario por ID: " + id, e);
            throw e; 
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { LogsError.warn(TipoUsuarioDAO.class, "Error al cerrar ResultSet", e); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LogsError.warn(TipoUsuarioDAO.class, "Error al cerrar PreparedStatement", e); }
        }
        return tipoUsuario;
    }

    public List<TipoUsuario> obtenerTodos() throws SQLException {
        List<TipoUsuario> tiposUsuario = new ArrayList<>();
        String sql = "SELECT id, tipo FROM tipo_usuario ORDER BY id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                TipoUsuario tipoUsuario = new TipoUsuario();
                tipoUsuario.setId(rs.getInt("id"));
                tipoUsuario.setTipo(rs.getString("tipo"));
                tiposUsuario.add(tipoUsuario);
            }
             LogsError.info(TipoUsuarioDAO.class, "Se obtuvieron " + tiposUsuario.size() + " tipos de usuario.");
        } catch (SQLException e) {
             LogsError.error(TipoUsuarioDAO.class, "Error al obtener todos los tipos de usuario", e);
            throw e; 
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { LogsError.warn(TipoUsuarioDAO.class, "Error al cerrar ResultSet", e); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LogsError.warn(TipoUsuarioDAO.class, "Error al cerrar PreparedStatement", e); }
        }
        return tiposUsuario;
    }
}
