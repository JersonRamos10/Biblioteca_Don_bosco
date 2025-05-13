package bibliotecaudb.servicio; 

// Importa los DAO, modelos y utilidades
import bibliotecaudb.dao.usuario.UsuarioDAO;
import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.excepciones.UsuarioException;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects; // Para Objects.requireNonNull

/**
 * Clase de servicio para gestionar la lógica de negocio relacionada con los Usuarios.
 */
public class ServicioUsuario {

    // Instancia del DAO para interactuar con la BD
    // Se instancia aquí por simplicidad, en diseños más complejos se inyectaría.
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Constante para identificar el tipo de usuario Administrador (según tu BD)
    private static final int ID_TIPO_ADMINISTRADOR = 1;
    private static final String NOMBRE_TIPO_ADMINISTRADOR = "Administrador"; // Podríamos usar el nombre también

    /**
     * Valida las credenciales de un usuario para iniciar sesión.
     *
     * @param correo El correo electrónico ingresado.
     * @param contrasena La contraseña ingresada.
     * @return El objeto Usuario si el login es exitoso y el usuario está activo, null en caso contrario.
     * @throws ServicioException Si ocurre un error de lógica de negocio o de base de datos.
     */
    public Usuario login(String correo, String contrasena) throws Exception { // Puedes usar tu Exception personalizada
        Usuario usuario = null;
        try {
            // Validación básica de entrada
            if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.isEmpty()) {
                LogsError.warn(ServicioUsuario.class, "Intento de login con datos inválidos (correo/contraseña vacíos).");
                return null; // O lanzar new ServicioException("Correo y contraseña son requeridos.");
            }

            // Llama al DAO para buscar por correo y contraseña
            usuario = usuarioDAO.obtenerPorCorreoYContrasena(correo, contrasena);

            // Verifica si se encontró el usuario y si está activo
            if (usuario != null) {
                if (usuario.isEstado()) {
                    LogsError.info(ServicioUsuario.class, "Login exitoso para: " + correo);
                    return usuario;
                } else {
                    LogsError.warn(ServicioUsuario.class, "Intento de login para usuario inactivo: " + correo);
                    return null; // Usuario encontrado pero inactivo
                }
            } else {
                LogsError.warn(ServicioUsuario.class, "Credenciales incorrectas para: " + correo);
                return null; // Credenciales incorrectas
            }

        } catch (SQLException e) {
            LogsError.error(ServicioUsuario.class, "Error de BD durante el login para: " + correo, e);
            // Relanza como una excepción de servicio o una genérica
            throw new Exception("Error en la base de datos al intentar iniciar sesión.", e);
        }
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * Realiza validaciones como verificar si el correo ya existe.
     *
     * @param nuevoUsuario El objeto Usuario con los datos a crear.
     * @throws ServicioException Si los datos son invalidos, el correo ya existe, o hay error de BD.
     */
    public void crearUsuario(Usuario nuevoUsuario) throws Exception {
        try {
            // Validacion de datos de entrada
            Objects.requireNonNull(nuevoUsuario, "El objeto usuario no puede ser nulo.");
            Objects.requireNonNull(nuevoUsuario.getCorreo(), "El correo no puede ser nulo.");
            Objects.requireNonNull(nuevoUsuario.getContrasena(), "La contraseña no puede ser nula.");
            Objects.requireNonNull(nuevoUsuario.getTipoUsuario(), "El tipo de usuario no puede ser nulo.");
            if (nuevoUsuario.getNombre() == null || nuevoUsuario.getNombre().trim().isEmpty() ||
                nuevoUsuario.getCorreo().trim().isEmpty() || nuevoUsuario.getContrasena().isEmpty()) {
                 throw new Exception("Nombre, correo y contraseña son requeridos."); 
            }
        

            // Verificar si el correo ya existe
            Usuario existente = usuarioDAO.obtenerPorCorreo(nuevoUsuario.getCorreo());
            if (existente != null) {
                LogsError.warn(ServicioUsuario.class, "Intento de crear usuario con correo ya existente: " + nuevoUsuario.getCorreo());
                throw new Exception("El correo electrónico '" + nuevoUsuario.getCorreo() + "' ya esta registrado."); // O ServicioException
            }

            // Si pasa las validaciones, llamar al DAO para insertar
            if (!nuevoUsuario.isEstado()) { // Si no se setea antes, lo ponemos activo
                 nuevoUsuario.setEstado(true);
            }

            usuarioDAO.insertar(nuevoUsuario);
            LogsError.info(ServicioUsuario.class, "Usuario creado exitosamente: " + nuevoUsuario.getCorreo());

        } catch (SQLException e) {
            LogsError.error(ServicioUsuario.class, "Error de BD al crear usuario: " + nuevoUsuario.getCorreo(), e);
            throw new Exception("Error en la base de datos al crear el usuario.", e);
        } catch (NullPointerException e) {
            // Captura por si Objects.requireNonNull falla
             LogsError.error(ServicioUsuario.class, "Datos nulos al intentar crear usuario.", e);
             throw new Exception("Faltan datos obligatorios para crear el usuario.", e);
        }
    }

    /**
     * Restablece la contraseña de un usuario especifico.
     * IMPORTANTE: Solo un usuario Administrador puede realizar esta accion.
     *
     * @param usuarioAdmin El usuario que esta realizando la operación (debe ser Administrador).
     * @param correoUsuarioARestablecer El correo del usuario cuya contraseña se restablecera.
     * @param nuevaContrasena La nueva contraseña a establecer.
     * @throws ServicioException Si el usuarioAdmin no es Administrador, si el usuario a restablecer no existe,
     * si la nueva contraseña es invalida, o si ocurre un error de BD.
     */
    public void restablecerContrasena(Usuario usuarioAdmin, String correoUsuarioARestablecer, String nuevaContrasena) throws Exception { // Puedes usar tu Exception personalizada
        try {
            // Validar que el usuario que realiza la accion es Administrador
            Objects.requireNonNull(usuarioAdmin, "El usuario administrador no puede ser nulo.");
            Objects.requireNonNull(usuarioAdmin.getTipoUsuario(), "El tipo de usuario del administrador no puede ser nulo.");

            // Comparamos por ID o por Nombre
            // if (usuarioAdmin.getTipoUsuario().getId() != ID_TIPO_ADMINISTRADOR) {
            if (!NOMBRE_TIPO_ADMINISTRADOR.equals(usuarioAdmin.getTipoUsuario().getTipo())) { // Usando nombre
                LogsError.warn(ServicioUsuario.class, "Intento no autorizado de restablecer contraseña por usuario: " + usuarioAdmin.getCorreo());
                throw new Exception("No tiene permisos para restablecer contraseñas. Se requiere ser Administrador."); // O ServicioException
            }

            // Validar datos de entrada
             Objects.requireNonNull(correoUsuarioARestablecer, "El correo del usuario a restablecer no puede ser nulo.");
            Objects.requireNonNull(nuevaContrasena, "La nueva contraseña no puede ser nula.");
             if (correoUsuarioARestablecer.trim().isEmpty() || nuevaContrasena.isEmpty()) {
                 throw new Exception("El correo y la nueva contraseña son requeridos."); // O ServicioException
             }
             

            // Buscar al usuario cuya contraseña se va a restablecer
            Usuario usuarioARestablecer = usuarioDAO.obtenerPorCorreo(correoUsuarioARestablecer);
            if (usuarioARestablecer == null) {
                LogsError.warn(ServicioUsuario.class, "Intento de restablecer contraseña para usuario no existente: " + correoUsuarioARestablecer);
                throw new Exception("No se encontró ningun usuario con el correo: " + correoUsuarioARestablecer); // O ServicioException
            }

            // Actualizar la contraseña del usuario encontrado
            usuarioARestablecer.setContrasena(nuevaContrasena); // Actualiza el objeto
            usuarioDAO.actualizar(usuarioARestablecer); // Llama al DAO para persistir el cambio

            LogsError.info(ServicioUsuario.class, "Contraseña restablecida para " + correoUsuarioARestablecer + " por " + usuarioAdmin.getCorreo());

        } catch (SQLException e) {
             LogsError.error(ServicioUsuario.class, "Error de BD al restablecer contraseña para: " + correoUsuarioARestablecer, e);
            throw new Exception("Error en la base de datos al restablecer la contraseña.", e);
         } catch (NullPointerException e) {
             LogsError.error(ServicioUsuario.class, "Datos nulos al intentar restablecer contraseña.", e);
             throw new Exception("Faltan datos obligatorios para restablecer la contraseña.", e);
        }
    }

    // --- Otros metodos de servicio ---

    /**
     * Obtiene un usuario por su ID.
     */
    public Usuario buscarUsuarioPorId(int id) throws Exception {
        try {
            return usuarioDAO.obtenerPorId(id);
        } catch (SQLException e) {
            LogsError.error(ServicioUsuario.class, "Error de BD al buscar usuario por ID: " + id, e);
            throw new Exception("Error en la base de datos al buscar el usuario.", e);
        }
    }

     /**
     * Obtiene un usuario por su correo.
     */
    public Usuario buscarUsuarioPorCorreo(String correo) throws Exception {
         try {
             // Validación básica
              if (correo == null || correo.trim().isEmpty()) {
                 return null;
             }
            return usuarioDAO.obtenerPorCorreo(correo);
        } catch (SQLException e) {
            LogsError.error(ServicioUsuario.class, "Error de BD al buscar usuario por correo: " + correo, e);
            throw new Exception("Error en la base de datos al buscar el usuario.", e);
        }
    }

    /**
     * Obtiene la lista de todos los usuarios.
     */
    public List<Usuario> obtenerTodosLosUsuarios() throws Exception {
        try {
            return usuarioDAO.listarTodos();
        } catch (SQLException e) {
            LogsError.error(ServicioUsuario.class, "Error de BD al listar todos los usuarios.", e);
            throw new Exception("Error en la base de datos al obtener la lista de usuarios.", e);
        }
    }

     /**
     * Actualizar los datos de un usuario.
     * Por ejemplo, verificar que el correo no se cambie a uno que ya existe.
     */
     public void actualizarUsuario(Usuario usuario) throws Exception {
         try {
             // Validacion
              Objects.requireNonNull(usuario, "El objeto usuario no puede ser nulo.");

              usuarioDAO.actualizar(usuario);
             LogsError.info(ServicioUsuario.class, "Datos de usuario actualizados para: " + usuario.getCorreo());

         } catch (SQLException e) {
            LogsError.error(ServicioUsuario.class, "Error de BD al actualizar usuario: " + usuario.getCorreo(), e);
            throw new Exception("Error en la base de datos al actualizar el usuario.", e);
         } catch (NullPointerException e) {
             LogsError.error(ServicioUsuario.class, "Datos nulos al intentar actualizar usuario.", e);
             throw new Exception("Faltan datos obligatorios para actualizar el usuario.", e);
        }
     }

     /**
      * Elimina un usuario por ID.
      */
      public void eliminarUsuario(int id) throws Exception {
          try {
              usuarioDAO.eliminar(id);
              LogsError.info(ServicioUsuario.class, "Usuario eliminado con ID: " + id);
          } catch (SQLException e) {
             LogsError.error(ServicioUsuario.class, "Error de BD al eliminar usuario con ID: " + id, e);
             throw new Exception("Error en la base de datos al eliminar el usuario.", e);
         }
      }

}