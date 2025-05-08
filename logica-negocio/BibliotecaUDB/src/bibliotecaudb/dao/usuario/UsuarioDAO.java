/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.dao.usuario;

import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.conexion.LogsError;
/**
 *
 * @author jerson_ramos
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Para obtener el ID generado
import java.util.ArrayList;
import java.util.List;


public class UsuarioDAO {
    public Usuario obtenerPorCorreo(String correo) throws SQLException {
        Usuario usuario = null;
        String sql = "SELECT u.id, u.nombre, u.correo, u.contrasena, u.estado, " +
                     "tu.id AS id_tipo_usuario, tu.tipo AS nombre_tipo_usuario " +
                     "FROM usuarios u " +
                     "INNER JOIN tipo_usuario tu ON u.id_tipo_usuario = tu.id " +
                     "WHERE u.correo = ? AND u.estado = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, correo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // ... mapeo como antes ...
                 usuario = new Usuario();
                 usuario.setId(rs.getInt("id"));
                 usuario.setNombre(rs.getString("nombre"));
                 usuario.setCorreo(rs.getString("correo"));
                 usuario.setContrasena(rs.getString("contrasena"));
                 usuario.setEstado(rs.getBoolean("estado"));
                 TipoUsuario tipo = new TipoUsuario();
                 tipo.setId(rs.getInt("id_tipo_usuario"));
                 tipo.setTipo(rs.getString("nombre_tipo_usuario"));
                 usuario.setTipoUsuario(tipo);
                // LogsError.info(UsuarioDAO.class, "Usuario encontrado por correo: " + correo); // Opcional
            } else {
                LogsError.warn(UsuarioDAO.class, "No se encontró usuario activo con correo: " + correo);
            }
        } catch (SQLException e) {
             LogsError.error(UsuarioDAO.class, "Error al obtener usuario por correo: " + correo, e);
            throw e;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { LogsError.warn(UsuarioDAO.class, "Error al cerrar ResultSet", e); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LogsError.warn(UsuarioDAO.class, "Error al cerrar PreparedStatement", e); }
        }
        return usuario;
    }

    public Usuario obtenerPorId(int id) throws SQLException {
         Usuario usuario = null;
         String sql = "SELECT u.id, u.nombre, u.correo, u.contrasena, u.estado, " +
                      "tu.id AS id_tipo_usuario, tu.tipo AS nombre_tipo_usuario " +
                      "FROM usuarios u " +
                      "INNER JOIN tipo_usuario tu ON u.id_tipo_usuario = tu.id " +
                      "WHERE u.id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                 // ... mapeo como antes ...
                 usuario = new Usuario();
                 usuario.setId(rs.getInt("id"));
                 usuario.setNombre(rs.getString("nombre"));
                 usuario.setCorreo(rs.getString("correo"));
                 usuario.setContrasena(rs.getString("contrasena"));
                 usuario.setEstado(rs.getBoolean("estado"));
                 TipoUsuario tipo = new TipoUsuario();
                 tipo.setId(rs.getInt("id_tipo_usuario"));
                 tipo.setTipo(rs.getString("nombre_tipo_usuario"));
                 usuario.setTipoUsuario(tipo);
                 // LogsError.info(UsuarioDAO.class, "Usuario encontrado por ID: " + id); // Opcional
            } else {
                  LogsError.warn(UsuarioDAO.class, "No se encontró usuario con ID: " + id);
            }
        } catch (SQLException e) {
             LogsError.error(UsuarioDAO.class, "Error al obtener usuario por ID: " + id, e);
             throw e;
        } finally {
             try { if (rs != null) rs.close(); } catch (SQLException e) { LogsError.warn(UsuarioDAO.class, "Error al cerrar ResultSet", e); }
             try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LogsError.warn(UsuarioDAO.class, "Error al cerrar PreparedStatement", e); }
         }
         return usuario;
    }

    public Usuario crearUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, correo, contrasena, id_tipo_usuario, estado) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        if (usuario.getTipoUsuario() == null || usuario.getTipoUsuario().getId() <= 0) { // ID 0 o negativo no válido
            String errorMsg = "Intento de crear usuario sin TipoUsuario válido: " + usuario.getCorreo();
             LogsError.error(UsuarioDAO.class, errorMsg); // Usar error si es una condición que impide continuar
            throw new SQLException("Tipo de usuario no especificado o inválido para el nuevo usuario.");
        }

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getContrasena());
            pstmt.setInt(4, usuario.getTipoUsuario().getId());
            pstmt.setBoolean(5, usuario.isEstado());

            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setId(generatedKeys.getInt(1));
                         LogsError.info(UsuarioDAO.class, "Usuario creado exitosamente con ID: " + usuario.getId() + " para el correo: " + usuario.getCorreo());
                        return usuario;
                    } else {
                         // Esto no debería pasar si filasAfectadas > 0 y la tabla tiene AI PK
                         String errorMsg = "Fallo al crear usuario, no se obtuvo ID generado para: " + usuario.getCorreo();
                         LogsError.error(UsuarioDAO.class, errorMsg);
                         throw new SQLException(errorMsg);
                    }
                }
            } else {
                 // Esto tampoco debería pasar si el INSERT era válido, pero lo logueamos por si acaso
                 LogsError.warn(UsuarioDAO.class, "El comando INSERT no afectó filas para el correo: " + usuario.getCorreo());
            }
        } catch (SQLException e) {
             LogsError.error(UsuarioDAO.class, "Error SQL al crear usuario: " + usuario.getCorreo(), e);
            throw e;
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LogsError.warn(UsuarioDAO.class, "Error al cerrar PreparedStatement", e); }
        }
        return null; // Retorna null si la inserción falló o no se obtuvo ID
    }

    public boolean actualizarContrasena(String correo, String nuevaContrasena) throws SQLException {
        String sql = "UPDATE usuarios SET contrasena = ? WHERE correo = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean exito = false;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nuevaContrasena);
            pstmt.setString(2, correo);

            int filasAfectadas = pstmt.executeUpdate();
            exito = filasAfectadas > 0;
            if (exito) {
                 LogsError.info(UsuarioDAO.class, "Contraseña actualizada para el usuario: " + correo);
            } else {
                 LogsError.warn(UsuarioDAO.class, "No se encontró el usuario o no se actualizó la contraseña para: " + correo);
            }
        } catch (SQLException e) {
             LogsError.error(UsuarioDAO.class, "Error SQL al actualizar contraseña para: " + correo, e);
            throw e;
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LogsError.warn(UsuarioDAO.class, "Error al cerrar PreparedStatement", e); }
        }
        return exito;
    }

    public boolean actualizarUsuario(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, correo = ?, id_tipo_usuario = ?, estado = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean exito = false;

        if (usuario.getTipoUsuario() == null || usuario.getTipoUsuario().getId() <= 0) {
             String errorMsg = "Intento de actualizar usuario sin TipoUsuario válido: " + usuario.getCorreo();
             LogsError.error(UsuarioDAO.class, errorMsg);
             throw new SQLException("Tipo de usuario no especificado o inválido para la actualización.");
        }

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setInt(3, usuario.getTipoUsuario().getId());
            pstmt.setBoolean(4, usuario.isEstado());
            pstmt.setInt(5, usuario.getId());

            int filasAfectadas = pstmt.executeUpdate();
            exito = filasAfectadas > 0;
            if (exito) {
                 LogsError.info(UsuarioDAO.class, "Usuario actualizado: " + usuario.getCorreo());
            } else {
                 LogsError.warn(UsuarioDAO.class, "No se actualizó el usuario (ID no encontrado?): " + usuario.getId());
            }
        } catch (SQLException e) {
             LogsError.error(UsuarioDAO.class, "Error SQL al actualizar usuario: " + usuario.getCorreo(), e);
            throw e;
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LogsError.warn(UsuarioDAO.class, "Error al cerrar PreparedStatement", e); }
        }
        return exito;
    }

    public List<Usuario> obtenerTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.id, u.nombre, u.correo, u.contrasena, u.estado, " +
                     "tu.id AS id_tipo_usuario, tu.tipo AS nombre_tipo_usuario " +
                     "FROM usuarios u " +
                     "INNER JOIN tipo_usuario tu ON u.id_tipo_usuario = tu.id " +
                     "ORDER BY u.nombre";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                 // ... mapeo como antes ...
                 Usuario usuario = new Usuario();
                 usuario.setId(rs.getInt("id"));
                 usuario.setNombre(rs.getString("nombre"));
                 usuario.setCorreo(rs.getString("correo"));
                 usuario.setContrasena(rs.getString("contrasena"));
                 usuario.setEstado(rs.getBoolean("estado"));
                 TipoUsuario tipo = new TipoUsuario();
                 tipo.setId(rs.getInt("id_tipo_usuario"));
                 tipo.setTipo(rs.getString("nombre_tipo_usuario"));
                 usuario.setTipoUsuario(tipo);
                 usuarios.add(usuario);
            }
             LogsError.info(UsuarioDAO.class, "Se obtuvieron " + usuarios.size() + " usuarios.");
        } catch (SQLException e) {
             LogsError.error(UsuarioDAO.class, "Error al obtener todos los usuarios", e);
            throw e;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { LogsError.warn(UsuarioDAO.class, "Error al cerrar ResultSet", e); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LogsError.warn(UsuarioDAO.class, "Error al cerrar PreparedStatement", e); }
        }
        return usuarios;
    }
}
