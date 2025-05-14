package bibliotecaudb.servicios.impl;

import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.dao.usuario.UsuarioDAO;
import bibliotecaudb.dao.usuario.TipoUsuarioDAO;
import bibliotecaudb.dao.usuario.impl.UsuarioDAOImpl;
import bibliotecaudb.dao.usuario.impl.TipoUsuarioDAOImpl;
import bibliotecaudb.servicios.UsuarioService;
import bibliotecaudb.excepciones.UsuarioException;
import bibliotecaudb.conexion.LogsError; // Para logging

import java.sql.SQLException;
import java.util.List;

public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final TipoUsuarioDAO tipoUsuarioDAO;

    public UsuarioServiceImpl() {
      
        this.usuarioDAO = new UsuarioDAOImpl(); 
        this.tipoUsuarioDAO = new TipoUsuarioDAOImpl();
    }

    public UsuarioServiceImpl(UsuarioDAO usuarioDAO, TipoUsuarioDAO tipoUsuarioDAO) {
        this.usuarioDAO = usuarioDAO;
        this.tipoUsuarioDAO = tipoUsuarioDAO;
    }

    @Override
    public Usuario autenticarUsuario(String correo, String contrasena) throws SQLException, UsuarioException {
        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.isEmpty()) {
            throw new UsuarioException("El correo y la contraseña no pueden estar vacios.");
        }

        Usuario usuario = usuarioDAO.validarLogin(correo, contrasena);

        if (usuario == null) {
            throw new UsuarioException("Correo o contraseña incorrectos, o usuario inactivo.");
        }
        LogsError.info(this.getClass(), "Usuario autenticado: " + usuario.getCorreo() + ", Tipo: " + (usuario.getTipoUsuario() != null ? usuario.getTipoUsuario().getTipo() : "N/D"));
        return usuario;
    }

    @Override
    public boolean registrarNuevoUsuario(Usuario nuevoUsuario, Usuario actorQueRegistra) throws SQLException, UsuarioException {
        if (actorQueRegistra == null || actorQueRegistra.getTipoUsuario() == null || !"Administrador".equals(actorQueRegistra.getTipoUsuario().getTipo())) {
            throw new UsuarioException("No tiene permisos para registrar nuevos usuarios.");
        }
        if (nuevoUsuario == null || nuevoUsuario.getCorreo() == null || nuevoUsuario.getCorreo().trim().isEmpty()) {
            throw new UsuarioException("El correo del nuevo usuario es obligatorio.");
        }
        if (nuevoUsuario.getContrasena() == null || nuevoUsuario.getContrasena().isEmpty()) {
            throw new UsuarioException("La contraseña del nuevo usuario es obligatoria.");
        }
        if (nuevoUsuario.getIdTipoUsuario() <= 0) {
            throw new UsuarioException("El tipo de usuario es obligatorio.");
        }
        if (tipoUsuarioDAO.obtenerPorId(nuevoUsuario.getIdTipoUsuario()) == null) {
            throw new UsuarioException("El tipo de usuario especificado no es válido.");
        }
        if (usuarioDAO.obtenerPorCorreo(nuevoUsuario.getCorreo()) != null) {
            throw new UsuarioException("El correo '" + nuevoUsuario.getCorreo() + "' ya está registrado.");
        }
        LogsError.info(this.getClass(), "Registrando nuevo usuario: " + nuevoUsuario.getCorreo());
        return usuarioDAO.insertar(nuevoUsuario);
    }

    @Override
    public boolean actualizarDatosUsuario(Usuario usuarioConNuevosDatos, Usuario actorQueActualiza) throws SQLException, UsuarioException {
        Usuario usuarioExistente = usuarioDAO.obtenerPorId(usuarioConNuevosDatos.getId());
        if (usuarioExistente == null) {
            throw new UsuarioException("Usuario con ID " + usuarioConNuevosDatos.getId() + " no encontrado para actualizar.");
        }

        boolean tienePermiso = false;
        if (actorQueActualiza != null && actorQueActualiza.getTipoUsuario() != null) {
            if ("Administrador".equals(actorQueActualiza.getTipoUsuario().getTipo())) {
                tienePermiso = true;
            } else if (actorQueActualiza.getId() == usuarioConNuevosDatos.getId()) {
                 if (usuarioConNuevosDatos.getIdTipoUsuario() != usuarioExistente.getIdTipoUsuario() ||
                    usuarioConNuevosDatos.isEstado() != usuarioExistente.isEstado()) {
                     // Solo un admin puede cambiar tipo o estado
                     if (!"Administrador".equals(actorQueActualiza.getTipoUsuario().getTipo())) {
                        throw new UsuarioException("No tiene permisos para cambiar el tipo de usuario o el estado.");
                     }
                 }
                tienePermiso = true;
            }
        }

        if (!tienePermiso) {
            throw new UsuarioException("No tiene permisos para actualizar este usuario.");
        }
        
        if (!usuarioExistente.getCorreo().equalsIgnoreCase(usuarioConNuevosDatos.getCorreo())) {
            Usuario usuarioConNuevoCorreo = usuarioDAO.obtenerPorCorreo(usuarioConNuevosDatos.getCorreo());
            if (usuarioConNuevoCorreo != null && usuarioConNuevoCorreo.getId() != usuarioExistente.getId()) {
                throw new UsuarioException("El nuevo correo '" + usuarioConNuevosDatos.getCorreo() + "' ya está en uso por otro usuario.");
            }
        }
        
        usuarioConNuevosDatos.setContrasena(usuarioExistente.getContrasena()); 
        LogsError.info(this.getClass(), "Actualizando datos para usuario ID: " + usuarioConNuevosDatos.getId());
        return usuarioDAO.actualizar(usuarioConNuevosDatos);
    }

    @Override
    public boolean cambiarContrasena(String correoUsuarioAModificar, String nuevaContrasena, Usuario actorQueModifica) throws SQLException, UsuarioException {
        if (actorQueModifica == null || actorQueModifica.getTipoUsuario() == null) {
            throw new UsuarioException("Se requiere un actor válido para cambiar la contraseña.");
        }
        Usuario usuarioAModificar = usuarioDAO.obtenerPorCorreo(correoUsuarioAModificar);
        if (usuarioAModificar == null) {
            throw new UsuarioException("Usuario con correo '" + correoUsuarioAModificar + "' no encontrado.");
        }

        if (!"Administrador".equals(actorQueModifica.getTipoUsuario().getTipo())) {
             if (actorQueModifica.getId() != usuarioAModificar.getId()) {
                throw new UsuarioException("No tiene permisos para cambiar la contraseña de otro usuario.");
             }
        }
        
        if (nuevaContrasena == null || nuevaContrasena.trim().isEmpty()) {
            throw new UsuarioException("La nueva contraseña no puede estar vacía.");
        }
        
        LogsError.info(this.getClass(), "Cambiando contraseña para usuario: " + correoUsuarioAModificar + " por actor: " + actorQueModifica.getCorreo());
        return usuarioDAO.actualizarContrasena(correoUsuarioAModificar, nuevaContrasena);
    }

    @Override
    public List<Usuario> obtenerTodosLosUsuarios(Usuario actor) throws SQLException, UsuarioException {
         if (actor == null || actor.getTipoUsuario() == null || !"Administrador".equals(actor.getTipoUsuario().getTipo())) {
            throw new UsuarioException("No tiene permisos para ver todos los usuarios.");
        }
        return usuarioDAO.obtenerTodos();
    }

    @Override
    public Usuario obtenerUsuarioPorCorreo(String correo) throws SQLException {
        return usuarioDAO.obtenerPorCorreo(correo);
    }
    
    @Override
    public Usuario obtenerUsuarioPorId(int idUsuario) throws SQLException {
        return usuarioDAO.obtenerPorId(idUsuario);
    }
}