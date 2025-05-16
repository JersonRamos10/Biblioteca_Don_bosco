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

    // Consultas SQL para la tabla 'usuarios'
    private static final String SQL_INSERT = "INSERT INTO usuarios (nombre, correo, contrasena, id_tipo_usuario, estado) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE usuarios SET nombre = ?, correo = ?, contrasena = ?, id_tipo_usuario = ?, estado = ? WHERE id = ?";
    private static final String SQL_UPDATE_CONTRASENA = "UPDATE usuarios SET contrasena = ? WHERE correo = ?";

    private static final String SQL_DELETE = "DELETE FROM usuarios WHERE id = ?";
    private static final String SQL_SELECT_BY_ID = "SELECT id, nombre, correo, contrasena, id_tipo_usuario, estado FROM usuarios WHERE id = ?";
    private static final String SQL_SELECT_BY_CORREO = "SELECT id, nombre, correo, contrasena, id_tipo_usuario, estado FROM usuarios WHERE correo = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, nombre, correo, contrasena, id_tipo_usuario, estado FROM usuarios ORDER BY nombre";
    // Consulta para el login, tambien trae el id_tipo_usuario para construir el objeto TipoUsuario completo
    private static final String SQL_LOGIN = "SELECT u.id, u.nombre, u.correo, u.contrasena, u.id_tipo_usuario, u.estado, tu.tipo AS tipo_nombre FROM usuarios u INNER JOIN tipo_usuario tu ON u.id_tipo_usuario = tu.id WHERE u.correo = ? AND u.contrasena = ? AND u.estado = 1";


    private TipoUsuarioDAO tipoUsuarioDAO; // Objeto para manejar los datos de TipoUsuario

    public UsuarioDAOImpl() {
        this.tipoUsuarioDAO = new TipoUsuarioDAOImpl(); // Creamos un objeto para TipoUsuario
    }
     public UsuarioDAOImpl(TipoUsuarioDAO tipoUsuarioDAO) { // Constructor para pasarele el manejador de TipoUsuario
        this.tipoUsuarioDAO = tipoUsuarioDAO;
    }


    @Override
    public boolean insertar(Usuario usuario) throws SQLException {
        // Este metodo sirve para guardar un nuevo usuario en la base de datos.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        ResultSet generatedKeys = null; // Para obtener el ID que se genera al insertar
        int rowsAffected = 0; // Para saber cuantas filas se modificaron

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getContrasena());
            pstmt.setInt(4, usuario.getIdTipoUsuario());
            pstmt.setBoolean(5, usuario.isEstado()); // El estado del usuario (activo/inactivo)

            LogsError.info(this.getClass(), "Ejecutando consulta para insertar usuario: " + SQL_INSERT); // Guardamos mensaje en log
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la insercion

            if (rowsAffected > 0) { // Si se inserto el usuario
                generatedKeys = pstmt.getGeneratedKeys(); // Obtenemos el ID generado
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getInt(1)); // Asignamos el nuevo ID al objeto usuario
                }
                LogsError.info(this.getClass(), "Usuario insertado con ID: " + usuario.getId() + ". Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se inserto el usuario. Filas afectadas: " + rowsAffected);
            }

        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar usuario: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error para que otra parte del sistema lo maneje
        } finally {
            ConexionBD.close(generatedKeys); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
            // La conexion se cierra usualmente al final de toda la operacion, no aqui.
        }
        return rowsAffected > 0; // Devolvemos true si se inserto el usuario
    }

    @Override
    public boolean actualizar(Usuario usuario) throws SQLException {
        // Este metodo sirve para actualizar los datos de un usuario existente.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        int rowsAffected = 0; // Para saber cuantas filas se afectaron

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getContrasena());
            pstmt.setInt(4, usuario.getIdTipoUsuario());
            pstmt.setBoolean(5, usuario.isEstado());
            pstmt.setInt(6, usuario.getId()); // El ID del usuario que queremos actualizar (condicion WHERE)

            LogsError.info(this.getClass(), "Ejecutando consulta para actualizar usuario: " + SQL_UPDATE + " para ID: " + usuario.getId());
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la actualizacion
            LogsError.info(this.getClass(), "Usuario actualizado. Filas afectadas: " + rowsAffected);

        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar usuario: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se actualizo el usuario
    }

    @Override
    public boolean actualizarContrasena(String correo, String nuevaContrasena) throws SQLException {
        // Este metodo sirve para cambiar la contrasena de un usuario, usando su correo.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        int rowsAffected = 0; // Filas afectadas

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_UPDATE_CONTRASENA);
            pstmt.setString(1, nuevaContrasena); // La nueva contrasena
            pstmt.setString(2, correo); // El correo del usuario a quien se le cambiara la contrasena

            LogsError.info(this.getClass(), "Ejecutando consulta para actualizar contrasena: " + SQL_UPDATE_CONTRASENA + " para correo: " + correo);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la actualizacion
            LogsError.info(this.getClass(), "Contrasena actualizada para el correo " + correo + ". Filas afectadas: " + rowsAffected);

        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar contrasena: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se actualizo la contrasena
    }

    @Override
    public boolean eliminar(int idUsuario) throws SQLException {
        // Este metodo sirve para eliminar un usuario de la base de datos usando su ID.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        int rowsAffected = 0; // Filas afectadas

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_DELETE);
            pstmt.setInt(1, idUsuario); // El ID del usuario a eliminar

            LogsError.info(this.getClass(), "Ejecutando consulta para eliminar usuario: " + SQL_DELETE + " para ID: " + idUsuario);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la eliminacion
            LogsError.info(this.getClass(), "Usuario eliminado. Filas afectadas: " + rowsAffected);

        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al eliminar usuario: " + ex.getMessage(), ex);
            // Considerar que podria haber problemas si el usuario tiene prestamos activos (llaves foraneas).
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se elimino el usuario
    }

    // Este metodo ayuda a convertir los datos del ResultSet (resultado de la BD) a un objeto Usuario.
    private Usuario mapearResultSetAUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(); // Creamos un objeto Usuario vacio
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setContrasena(rs.getString("contrasena"));
        usuario.setIdTipoUsuario(rs.getInt("id_tipo_usuario"));
        usuario.setEstado(rs.getBoolean("estado"));

        // Obtenemos el objeto TipoUsuario completo para tener mas detalles
        TipoUsuario tipo = tipoUsuarioDAO.obtenerPorId(usuario.getIdTipoUsuario()); // Usamos el DAO de TipoUsuario
        usuario.setTipoUsuario(tipo); // Asignamos el TipoUsuario al Usuario
        return usuario; // Devolvemos el usuario con todos sus datos
    }

    // Este metodo es similar al anterior, pero especifico para los datos que vienen de la consulta de login.
    private Usuario mapearResultSetAUsuarioLogin(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(); // Creamos un objeto Usuario vacio
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setContrasena(rs.getString("contrasena"));
        usuario.setIdTipoUsuario(rs.getInt("id_tipo_usuario"));
        usuario.setEstado(rs.getBoolean("estado"));

        TipoUsuario tipo = new TipoUsuario(); // Creamos un objeto TipoUsuario
        tipo.setId(rs.getInt("id_tipo_usuario")); // El ID del tipo de usuario
        tipo.setTipo(rs.getString("tipo_nombre")); // El nombre del tipo de usuario (viene del JOIN en la consulta SQL_LOGIN)
        usuario.setTipoUsuario(tipo); // Asignamos el TipoUsuario al Usuario
        return usuario; // Devolvemos el usuario
    }


    @Override
    public Usuario obtenerPorId(int idUsuario) throws SQLException {
        // Este metodo busca y devuelve un usuario usando su ID.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para guardar el resultado de la consulta
        Usuario usuario = null; // Variable para el usuario que encontraremos

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, idUsuario); // El ID del usuario que buscamos
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener usuario por ID: " + SQL_SELECT_BY_ID + " para ID: " + idUsuario);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta

            if (rs.next()) { // Si encontramos un usuario
                usuario = mapearResultSetAUsuario(rs); // Convertimos los datos a un objeto Usuario
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener usuario por ID: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return usuario; // Devolvemos el usuario encontrado (o null si no se encontro)
    }

    @Override
    public Usuario obtenerPorCorreo(String correo) throws SQLException {
        // Este metodo busca y devuelve un usuario usando su correo electronico.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para guardar el resultado
        Usuario usuario = null; // Variable para el usuario

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_BY_CORREO);
            pstmt.setString(1, correo); // El correo que buscamos
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener usuario por correo: " + SQL_SELECT_BY_CORREO + " para correo: " + correo);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta

            if (rs.next()) { // Si encontramos el usuario
                 usuario = mapearResultSetAUsuario(rs); // Convertimos los datos a objeto
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener usuario por correo: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return usuario; // Devolvemos el usuario (o null)
    }

    @Override
    public List<Usuario> obtenerTodos() throws SQLException {
        // Este metodo devuelve una lista con todos los usuarios de la base de datos.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        List<Usuario> usuarios = new ArrayList<>(); // Lista para guardar los usuarios

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener todos los usuarios: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta

            while (rs.next()) { // Mientras haya usuarios en el resultado
                usuarios.add(mapearResultSetAUsuario(rs)); // Agregamos el usuario a la lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todos los usuarios: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return usuarios; // Devolvemos la lista de usuarios
    }

    @Override
    public Usuario validarLogin(String correo, String contrasena) throws SQLException {
        // Este metodo verifica si el correo y contrasena son correctos para iniciar sesion.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para el resultado
        Usuario usuario = null; // Variable para el usuario si el login es exitoso

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_LOGIN);
            pstmt.setString(1, correo); // El correo ingresado
            pstmt.setString(2, contrasena); // La contrasena ingresada
            LogsError.info(this.getClass(), "Ejecutando consulta de login para correo: " + correo);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta

            if (rs.next()) { // Si encontramos un usuario que coincide y esta activo
                usuario = mapearResultSetAUsuarioLogin(rs); // Convertimos los datos a objeto Usuario
                LogsError.info(this.getClass(), "Login exitoso para: " + correo + ", Usuario: " + usuario.getNombre() + ", Tipo: " + usuario.getTipoUsuario().getTipo());
            } else {
                LogsError.warn(this.getClass(), "Login fallido para correo: " + correo + ". Usuario no encontrado o contrasena incorrecta o usuario inactivo.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error durante el login: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return usuario; // Devolvemos el usuario (o null si el login fallo)
    }
}