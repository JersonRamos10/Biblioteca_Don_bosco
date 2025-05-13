package bibliotecaudb.dao.usuario; 

// Importa los modelos y utilidades necesarias
import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del DAO para la entidad Usuario.
 * Accede directamente a la base de datos usando JDBC.
 */
public class UsuarioDAO {

    // Sentencias SQL para las operaciones CRUD y otras consultas
    private static final String SQL_INSERT = "INSERT INTO usuarios (nombre, correo, contrasena, id_tipo_usuario, estado) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE usuarios SET nombre = ?, correo = ?, contrasena = ?, id_tipo_usuario = ?, estado = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM usuarios WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, nombre, correo, contrasena, id_tipo_usuario, estado FROM usuarios ORDER BY id";
    private static final String SQL_SELECT_BY_ID = "SELECT id, nombre, correo, contrasena, id_tipo_usuario, estado FROM usuarios WHERE id = ?";
    private static final String SQL_SELECT_BY_CORREO = "SELECT id, nombre, correo, contrasena, id_tipo_usuario, estado FROM usuarios WHERE correo = ?";
    // Consulta para el login (basada en ConsultasComunes.sql)
  
    private static final String SQL_SELECT_LOGIN = "SELECT id, nombre, correo, contrasena, id_tipo_usuario, estado FROM usuarios WHERE correo = ? AND contrasena = ?";

    // Dependencia del DAO de TipoUsuario para obtener el objeto completo
    // Se instancia aquí.
    private TipoUsuarioDAO tipoUsuarioDAO = new TipoUsuarioDAO();

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param usuario El objeto Usuario a insertar (sin ID, ya que es autoincremental).
     * @throws SQLException Si ocurre un error durante el acceso a la base de datos.
     */
    public void insertar(Usuario usuario) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT);

            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getContrasena()); // Guardando contraseña en texto plano
            // condicional para asegurarse que el objeto tipoUsuario no sea null antes de obtener su ID
            if (usuario.getTipoUsuario() != null) {
                pstmt.setInt(4, usuario.getTipoUsuario().getId());
            } else {
                // Lanzar error o asignar un tipo por defecto si es necesario
                throw new SQLException("El tipo de usuario no puede ser nulo para insertar.");
            }
            pstmt.setBoolean(5, usuario.isEstado()); // true para 1, false para 0

            pstmt.executeUpdate();
            LogsError.info(UsuarioDAO.class, "Usuario insertado: " + usuario.getCorreo());

        } finally {
            ConexionBD.close(pstmt);
           
        }
    }

    /**
     * Actualiza un usuario existente en la base de datos.
     *
     * @param usuario El objeto Usuario a actualizar (debe tener un ID valido).
     * @throws SQLException Si ocurre un error durante el acceso a la base de datos.
     */
    public void actualizar(Usuario usuario) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);

            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getContrasena());
            if (usuario.getTipoUsuario() != null) {
                pstmt.setInt(4, usuario.getTipoUsuario().getId());
            } else {
                 throw new SQLException("El tipo de usuario no puede ser nulo para actualizar.");
            }
            pstmt.setBoolean(5, usuario.isEstado());
            pstmt.setInt(6, usuario.getId()); // ID para la cláusula WHERE

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                 LogsError.info(UsuarioDAO.class, "Usuario actualizado: ID=" + usuario.getId() + ", Correo=" + usuario.getCorreo());
            } else {
                 LogsError.warn(UsuarioDAO.class, "No se actualizo ningun usuario con ID: " + usuario.getId());
            }

        } finally {
            ConexionBD.close(pstmt);

        }
    }

     /**
     * Elimina un usuario de la base de datos basado en su ID.
     *
     * @param id El ID del usuario a eliminar.
     * @throws SQLException Si ocurre un error durante el acceso a la base de datos.
     */
    public void eliminar(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_DELETE);
            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
             if (rowsAffected > 0) {
                 LogsError.info(UsuarioDAO.class, "Usuario eliminado: ID=" + id);
            } else {
                 LogsError.warn(UsuarioDAO.class, "No se elimino ningún usuario con ID: " + id);
            }

        } finally {
            ConexionBD.close(pstmt);
          
        }
    }

    /**
     * Obtiene un Usuario de la base de datos basado en su ID.
     *
     * @param id El ID del usuario a buscar.
     * @return El objeto Usuario encontrado, o null si no se encuentra.
     * @throws SQLException Si ocurre un error durante el acceso a la base de datos.
     */
    public Usuario obtenerPorId(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Usuario usuario = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                usuario = mapResultSetToUsuario(rs); //  metodo helper
            }

        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        
        }
        return usuario;
    }

    /**
     * Obtiene un Usuario de la base de datos basado en su correo electrónico.
     *
     * @param correo El correo del usuario a buscar.
     * @return El objeto Usuario encontrado, o null si no se encuentra.
     * @throws SQLException Si ocurre un error durante el acceso a la base de datos.
     */
    public Usuario obtenerPorCorreo(String correo) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Usuario usuario = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_CORREO);
            pstmt.setString(1, correo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                 usuario = mapResultSetToUsuario(rs); // metodo helper
            }

        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
            
        }
        return usuario;
    }

     /**
     * Obtiene una lista de todos los Usuarios de la base de datos.
     *
     * @return Una lista de objetos Usuario. La lista estara vacia si no hay usuarios.
     * @throws SQLException Si ocurre un error durante el acceso a la base de datos.
     */
    public List<Usuario> listarTodos() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Usuario> usuarios = new ArrayList<>();

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Usuario usuario = mapResultSetToUsuario(rs); //  metodo helper
                if (usuario != null) { // Verificacion extra por si el mapeo falla
                    usuarios.add(usuario);
                }
            }

        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
            
        }
        return usuarios;
    }

    /**
     * Intenta obtener un usuario basado en correo y contraseña (para login).
     * @param correo El correo del usuario.
     * @param contrasena La contraseña proporcionada por el usuario.
     * @return El objeto Usuario si las credenciales son correctas, null en caso contrario.
     * @throws SQLException Si ocurre un error durante el acceso a la base de datos.
     */
    public Usuario obtenerPorCorreoYContrasena(String correo, String contrasena) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Usuario usuario = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_LOGIN);
            pstmt.setString(1, correo);
            pstmt.setString(2, contrasena); // Comparando texto plano
            rs = pstmt.executeQuery();

            if (rs.next()) {
                usuario = mapResultSetToUsuario(rs); // metodo helper
            }

        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
       
        }
        return usuario;
    }


    // --- Metodo Helper Privado ---

    /**
     * Mapea una fila del ResultSet a un objeto Usuario.
     * Incluye la obtencion del objeto TipoUsuario asociado.
     *
     * @param rs El ResultSet posicionado en la fila a mapear.
     * @return Un objeto Usuario completo.
     * @throws SQLException Si ocurre un error al leer el ResultSet o al buscar el TipoUsuario.
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setContrasena(rs.getString("contrasena")); // Leemos la contraseña (texto plano)
        usuario.setEstado(rs.getBoolean("estado"));

        // Obtener el TipoUsuario asociado usando TipoUsuarioDAO
        int idTipoUsuario = rs.getInt("id_tipo_usuario");
        TipoUsuario tipoUsuario = tipoUsuarioDAO.obtenerPorId(idTipoUsuario); // Llamada al otro DAO

        if (tipoUsuario == null) {
            // Manejar el caso donde el id_tipo_usuario en la BD es invalido o no existe
             LogsError.warn(UsuarioDAO.class, "No se encontro TipoUsuario con ID: " + idTipoUsuario + " para Usuario ID: " + usuario.getId());
             usuario.setTipoUsuario(null);
            
        } else {
            usuario.setTipoUsuario(tipoUsuario);
        }
        return usuario;
    }

}