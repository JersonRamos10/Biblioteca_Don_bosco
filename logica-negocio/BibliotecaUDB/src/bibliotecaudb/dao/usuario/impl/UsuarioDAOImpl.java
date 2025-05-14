package bibliotecaudb.dao.usuario.impl;

import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.dao.usuario.UsuarioDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.dao.usuario.TipoUsuarioDAO; // Para obtener el objeto TipoUsuario

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    // Sentencias SQL basadas en la tabla 'usuarios'
    private static final String SQL_INSERT = "INSERT INTO usuarios (nombre, correo, contrasena, id_tipo_usuario, estado) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE usuarios SET nombre = ?, correo = ?, contrasena = ?, id_tipo_usuario = ?, estado = ? WHERE id = ?";
    private static final String SQL_UPDATE_CONTRASENA = "UPDATE usuarios SET contrasena = ? WHERE correo = ?";
 
    private static final String SQL_DELETE = "DELETE FROM usuarios WHERE id = ?";
    private static final String SQL_SELECT_BY_ID = "SELECT id, nombre, correo, contrasena, id_tipo_usuario, estado FROM usuarios WHERE id = ?";
    private static final String SQL_SELECT_BY_CORREO = "SELECT id, nombre, correo, contrasena, id_tipo_usuario, estado FROM usuarios WHERE correo = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, nombre, correo, contrasena, id_tipo_usuario, estado FROM usuarios ORDER BY nombre";
    // Consulta para login ConsultasComunes.sql, trayendo tambien el id_tipo_usuario para construir el objeto TipoUsuario
    private static final String SQL_LOGIN = "SELECT u.id, u.nombre, u.correo, u.contrasena, u.id_tipo_usuario, u.estado, tu.tipo AS tipo_nombre FROM usuarios u INNER JOIN tipo_usuario tu ON u.id_tipo_usuario = tu.id WHERE u.correo = ? AND u.contrasena = ? AND u.estado = 1";


    private TipoUsuarioDAO tipoUsuarioDAO; // Para obtener detalles del TipoUsuario

    public UsuarioDAOImpl() {
        this.tipoUsuarioDAO = new TipoUsuarioDAOImpl(); // Instanciacion directa 
    }
     public UsuarioDAOImpl(TipoUsuarioDAO tipoUsuarioDAO) {
        this.tipoUsuarioDAO = tipoUsuarioDAO;
    }


    @Override
    public boolean insertar(Usuario usuario) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        int rowsAffected = 0;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getContrasena()); 
            pstmt.setInt(4, usuario.getIdTipoUsuario());
            pstmt.setBoolean(5, usuario.isEstado());

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_INSERT);
            rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getInt(1));
                }
                LogsError.info(this.getClass(), "Usuario insertado con ID: " + usuario.getId() + ". Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se inserto el usuario. Filas afectadas: " + rowsAffected);
            }

        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar usuario: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(generatedKeys);
            ConexionBD.close(pstmt);
            
        }
        return rowsAffected > 0;
    }

    @Override
    public boolean actualizar(Usuario usuario) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getContrasena()); 
            pstmt.setInt(4, usuario.getIdTipoUsuario());
            pstmt.setBoolean(5, usuario.isEstado());
            pstmt.setInt(6, usuario.getId());

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_UPDATE + " para ID: " + usuario.getId());
            rowsAffected = pstmt.executeUpdate();
            LogsError.info(this.getClass(), "Usuario actualizado. Filas afectadas: " + rowsAffected);

        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar usuario: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    @Override
    public boolean actualizarContrasena(String correo, String nuevaContrasena) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE_CONTRASENA);
            pstmt.setString(1, nuevaContrasena); 
            pstmt.setString(2, correo);

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_UPDATE_CONTRASENA + " para correo: " + correo);
            rowsAffected = pstmt.executeUpdate();
            LogsError.info(this.getClass(), "Contraseña actualizada para el correo " + correo + ". Filas afectadas: " + rowsAffected);

        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar contraseña: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    @Override
    public boolean eliminar(int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_DELETE);
            pstmt.setInt(1, idUsuario);

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_DELETE + " para ID: " + idUsuario);
            rowsAffected = pstmt.executeUpdate();
            LogsError.info(this.getClass(), "Usuario eliminado. Filas afectadas: " + rowsAffected);

        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al eliminar usuario: " + ex.getMessage(), ex);
           
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    private Usuario mapearResultSetAUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setContrasena(rs.getString("contrasena")); 
        usuario.setIdTipoUsuario(rs.getInt("id_tipo_usuario"));
        usuario.setEstado(rs.getBoolean("estado"));

        // Obtener el objeto TipoUsuario completo
        TipoUsuario tipo = tipoUsuarioDAO.obtenerPorId(usuario.getIdTipoUsuario());
        usuario.setTipoUsuario(tipo);
        return usuario;
    }
    
    private Usuario mapearResultSetAUsuarioLogin(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setContrasena(rs.getString("contrasena"));
        usuario.setIdTipoUsuario(rs.getInt("id_tipo_usuario"));
        usuario.setEstado(rs.getBoolean("estado"));

        TipoUsuario tipo = new TipoUsuario();
        tipo.setId(rs.getInt("id_tipo_usuario"));
        tipo.setTipo(rs.getString("tipo_nombre"));
        usuario.setTipoUsuario(tipo);
        return usuario;
    }


    @Override
    public Usuario obtenerPorId(int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Usuario usuario = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, idUsuario);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ID + " para ID: " + idUsuario);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                usuario = mapearResultSetAUsuario(rs);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener usuario por ID: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return usuario;
    }

    @Override
    public Usuario obtenerPorCorreo(String correo) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Usuario usuario = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_CORREO);
            pstmt.setString(1, correo);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_CORREO + " para correo: " + correo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                 usuario = mapearResultSetAUsuario(rs);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener usuario por correo: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return usuario;
    }

    @Override
    public List<Usuario> obtenerTodos() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Usuario> usuarios = new ArrayList<>();

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                usuarios.add(mapearResultSetAUsuario(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todos los usuarios: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return usuarios;
    }

    @Override
    public Usuario validarLogin(String correo, String contrasena) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Usuario usuario = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_LOGIN);
            pstmt.setString(1, correo);
            pstmt.setString(2, contrasena); 
            LogsError.info(this.getClass(), "Ejecutando query de login para correo: " + correo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                usuario = mapearResultSetAUsuarioLogin(rs);
                LogsError.info(this.getClass(), "Login exitoso para: " + correo + ", Usuario: " + usuario.getNombre() + ", Tipo: " + usuario.getTipoUsuario().getTipo());
            } else {
                LogsError.warn(this.getClass(), "Login fallido para correo: " + correo + ". Usuario no encontrado o contraseña incorrecta o usuario inactivo.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error durante el login: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return usuario;
    }
}