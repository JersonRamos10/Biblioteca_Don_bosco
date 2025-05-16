package bibliotecaudb.servicios.impl;

import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.dao.usuario.UsuarioDAO;
import bibliotecaudb.dao.usuario.TipoUsuarioDAO;
import bibliotecaudb.dao.usuario.impl.UsuarioDAOImpl;
import bibliotecaudb.dao.usuario.impl.TipoUsuarioDAOImpl;
import bibliotecaudb.servicios.UsuarioService;
import bibliotecaudb.excepciones.UsuarioException;
import bibliotecaudb.conexion.LogsError; // Para escribir en el log

import java.sql.SQLException;
import java.util.List;

public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioDAO usuarioDAO; // Objeto para interactuar con la tabla de usuarios
    private final TipoUsuarioDAO tipoUsuarioDAO; // Objeto para interactuar con la tabla de tipos de usuario

    public UsuarioServiceImpl() {
        this.usuarioDAO = new UsuarioDAOImpl(); // Creamos el objeto para manejar usuarios
        this.tipoUsuarioDAO = new TipoUsuarioDAOImpl(); // Creamos el objeto para manejar tipos de usuario
    }

    // Constructor para  pasarle los manejadores 
    public UsuarioServiceImpl(UsuarioDAO usuarioDAO, TipoUsuarioDAO tipoUsuarioDAO) {
        this.usuarioDAO = usuarioDAO;
        this.tipoUsuarioDAO = tipoUsuarioDAO;
    }

    @Override
    public Usuario autenticarUsuario(String correo, String contrasena) throws SQLException, UsuarioException {
        // Este metodo verifica si un usuario puede iniciar sesion con su correo y contrasena.
        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.isEmpty()) {
            throw new UsuarioException("El correo y la contrasena no pueden estar vacios.");
        }

        Usuario usuario = usuarioDAO.validarLogin(correo, contrasena); // Intentamos validar el login con el DAO

        if (usuario == null) { // Si el DAO no devuelve un usuario
            throw new UsuarioException("Correo o contrasena incorrectos, o usuario inactivo.");
        }
        // Escribimos en el log que el usuario inicio sesion
        LogsError.info(this.getClass(), "Usuario autenticado: " + usuario.getCorreo() + ", Tipo: " + (usuario.getTipoUsuario() != null ? usuario.getTipoUsuario().getTipo() : "N/D"));
        return usuario; // Devolvemos el usuario si todo salio bien
    }

    @Override
    public boolean registrarNuevoUsuario(Usuario nuevoUsuario, Usuario actorQueRegistra) throws SQLException, UsuarioException {
        // Este metodo registra un nuevo usuario en el sistema, solo si quien lo registra es un Administrador.
        if (actorQueRegistra == null || actorQueRegistra.getTipoUsuario() == null || !"Administrador".equals(actorQueRegistra.getTipoUsuario().getTipo())) {
            throw new UsuarioException("No tiene permisos para registrar nuevos usuarios.");
        }
        if (nuevoUsuario == null || nuevoUsuario.getCorreo() == null || nuevoUsuario.getCorreo().trim().isEmpty()) {
            throw new UsuarioException("El correo del nuevo usuario es obligatorio.");
        }
        if (nuevoUsuario.getContrasena() == null || nuevoUsuario.getContrasena().isEmpty()) {
            throw new UsuarioException("La contrasena del nuevo usuario es obligatoria.");
        }
        if (nuevoUsuario.getIdTipoUsuario() <= 0) { // El ID del tipo de usuario debe ser valido
            throw new UsuarioException("El tipo de usuario es obligatorio.");
        }
        if (tipoUsuarioDAO.obtenerPorId(nuevoUsuario.getIdTipoUsuario()) == null) { // Verificamos que el tipo de usuario exista
            throw new UsuarioException("El tipo de usuario especificado no es valido.");
        }
        if (usuarioDAO.obtenerPorCorreo(nuevoUsuario.getCorreo()) != null) { // Verificamos que el correo no este ya registrado
            throw new UsuarioException("El correo '" + nuevoUsuario.getCorreo() + "' ya esta registrado.");
        }
        LogsError.info(this.getClass(), "Registrando nuevo usuario: " + nuevoUsuario.getCorreo());
        return usuarioDAO.insertar(nuevoUsuario); // Insertamos el nuevo usuario usando el DAO
    }

    @Override
    public boolean actualizarDatosUsuario(Usuario usuarioConNuevosDatos, Usuario actorQueActualiza) throws SQLException, UsuarioException {
        // Este metodo actualiza los datos de un usuario, con ciertas reglas de permisos.
        Usuario usuarioExistente = usuarioDAO.obtenerPorId(usuarioConNuevosDatos.getId()); // Obtenemos el usuario como esta actualmente en la BD
        if (usuarioExistente == null) {
            throw new UsuarioException("Usuario con ID " + usuarioConNuevosDatos.getId() + " no encontrado para actualizar.");
        }

        boolean tienePermiso = false; // Variable para saber si el actor tiene permiso
        if (actorQueActualiza != null && actorQueActualiza.getTipoUsuario() != null) {
            if ("Administrador".equals(actorQueActualiza.getTipoUsuario().getTipo())) { // Un admin siempre tiene permiso
                tienePermiso = true;
            } else if (actorQueActualiza.getId() == usuarioConNuevosDatos.getId()) { // Un usuario puede actualizar sus propios datos
                 if (usuarioConNuevosDatos.getIdTipoUsuario() != usuarioExistente.getIdTipoUsuario() ||
                    usuarioConNuevosDatos.isEstado() != usuarioExistente.isEstado()) {
                     // Pero solo un admin puede cambiar el tipo de usuario o el estado
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

        // Si se esta cambiando el correo, verificamos que el nuevo correo no este ya en uso por OTRO usuario
        if (!usuarioExistente.getCorreo().equalsIgnoreCase(usuarioConNuevosDatos.getCorreo())) {
            Usuario usuarioConNuevoCorreo = usuarioDAO.obtenerPorCorreo(usuarioConNuevosDatos.getCorreo());
            if (usuarioConNuevoCorreo != null && usuarioConNuevoCorreo.getId() != usuarioExistente.getId()) {
                throw new UsuarioException("El nuevo correo '" + usuarioConNuevosDatos.getCorreo() + "' ya esta en uso por otro usuario.");
            }
        }

        // Importante: Este metodo NO actualiza la contrasena. Para eso esta 'cambiarContrasena'.
        // Mantenemos la contrasena que ya tenia.
        usuarioConNuevosDatos.setContrasena(usuarioExistente.getContrasena());
        LogsError.info(this.getClass(), "Actualizando datos para usuario ID: " + usuarioConNuevosDatos.getId());
        return usuarioDAO.actualizar(usuarioConNuevosDatos); // Actualizamos con el DAO
    }

    @Override
    public boolean cambiarContrasena(String correoUsuarioAModificar, String nuevaContrasena, Usuario actorQueModifica) throws SQLException, UsuarioException {
        // Este metodo permite cambiar la contrasena de un usuario.
        if (actorQueModifica == null || actorQueModifica.getTipoUsuario() == null) {
            throw new UsuarioException("Se requiere un actor valido para cambiar la contrasena.");
        }
        Usuario usuarioAModificar = usuarioDAO.obtenerPorCorreo(correoUsuarioAModificar); // Buscamos al usuario por su correo
        if (usuarioAModificar == null) {
            throw new UsuarioException("Usuario con correo '" + correoUsuarioAModificar + "' no encontrado.");
        }

        // Un admin puede cambiar cualquier contrasena. Un usuario normal solo la suya.
        if (!"Administrador".equals(actorQueModifica.getTipoUsuario().getTipo())) {
             if (actorQueModifica.getId() != usuarioAModificar.getId()) { // Si no es admin y no es el mismo usuario
                throw new UsuarioException("No tiene permisos para cambiar la contrasena de otro usuario.");
             }
        }

        if (nuevaContrasena == null || nuevaContrasena.trim().isEmpty()) {
            throw new UsuarioException("La nueva contrasena no puede estar vacia.");
        }

        LogsError.info(this.getClass(), "Cambiando contrasena para usuario: " + correoUsuarioAModificar + " por actor: " + actorQueModifica.getCorreo());
        return usuarioDAO.actualizarContrasena(correoUsuarioAModificar, nuevaContrasena); // Usamos el DAO para actualizar la contrasena
    }

    @Override
    public List<Usuario> obtenerTodosLosUsuarios(Usuario actor) throws SQLException, UsuarioException {
        // Este metodo devuelve todos los usuarios, pero solo si el actor es un Administrador.
         if (actor == null || actor.getTipoUsuario() == null || !"Administrador".equals(actor.getTipoUsuario().getTipo())) {
            throw new UsuarioException("No tiene permisos para ver todos los usuarios.");
        }
        return usuarioDAO.obtenerTodos(); // El DAO se encarga de traerlos
    }

    @Override
    public Usuario obtenerUsuarioPorCorreo(String correo) throws SQLException {
        // Este metodo busca un usuario por su correo.
        return usuarioDAO.obtenerPorCorreo(correo);
    }

    @Override
    public Usuario obtenerUsuarioPorId(int idUsuario) throws SQLException {
        // Este metodo busca un usuario por su ID.
        return usuarioDAO.obtenerPorId(idUsuario);
    }
}