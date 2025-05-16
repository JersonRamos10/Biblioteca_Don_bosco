package bibliotecaudb.servicios;

import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.excepciones.UsuarioException;
import java.sql.SQLException;
import java.util.List;

public interface UsuarioService {

    Usuario autenticarUsuario(String correo, String contrasena) throws SQLException, UsuarioException; 

    boolean registrarNuevoUsuario(Usuario nuevoUsuario, Usuario actorQueRegistra) throws SQLException, UsuarioException;

    boolean actualizarDatosUsuario(Usuario usuarioConNuevosDatos, Usuario actorQueActualiza) throws SQLException, UsuarioException; 

    boolean cambiarContrasena(String correoUsuarioAModificar, String nuevaContrasena, Usuario actorQueModifica) throws SQLException, UsuarioException; // CAMBIO AQUÍ

    List<Usuario> obtenerTodosLosUsuarios(Usuario actor) throws SQLException, UsuarioException; 
    Usuario obtenerUsuarioPorCorreo(String correo) throws SQLException;
    
    Usuario obtenerUsuarioPorId(int idUsuario) throws SQLException;
}