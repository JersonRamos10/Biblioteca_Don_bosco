package bibliotecaudb.dao.usuario;

import bibliotecaudb.modelo.usuario.Usuario;
import java.sql.SQLException;
import java.util.List;

public interface UsuarioDAO {
    boolean insertar(Usuario usuario) throws SQLException;
    boolean actualizar(Usuario usuario) throws SQLException;
    boolean actualizarContrasena(String correo, String nuevaContrasena) throws SQLException;
    boolean eliminar(int idUsuario) throws SQLException; // O cambiar estado a inactivo
    Usuario obtenerPorId(int idUsuario) throws SQLException;
    Usuario obtenerPorCorreo(String correo) throws SQLException;
    List<Usuario> obtenerTodos() throws SQLException;
    Usuario validarLogin(String correo, String contrasena) throws SQLException;
}