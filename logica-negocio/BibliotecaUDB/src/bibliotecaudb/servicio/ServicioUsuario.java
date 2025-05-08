/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.servicio;

import bibliotecaudb.dao.usuario.UsuarioDAO;
import bibliotecaudb.dao.usuario.TipoUsuarioDAO;
import bibliotecaudb.excepciones.UsuarioException;
import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.conexion.LogsError;

import java.sql.SQLException; // El DAO podría lanzar SQLException
import java.util.List; // Para obtener todos los usuarios


/**
 *
 * @author jerson_ramos
 */
public class ServicioUsuario {
    
    
   private UsuarioDAO usuarioDAO;
   private TipoUsuarioDAO tipoUsuarioDAO;

    public ServicioUsuario() {
        this.usuarioDAO = new UsuarioDAO();
        this.tipoUsuarioDAO = new TipoUsuarioDAO();
    }

    //metodo para trabajr el login
    public Usuario login(String correo, String contrasenaIngresada) throws UsuarioException {
         LogsError.info(ServicioUsuario.class, "Servicio: Intento de login para el correo: " + correo);
        Usuario usuarioDeBD;

        try {
            usuarioDeBD = usuarioDAO.obtenerPorCorreo(correo);
        } catch (SQLException e) {
             LogsError.error(ServicioUsuario.class, "Servicio: Error de BD durante login para: " + correo, e);
            throw new UsuarioException("Error al procesar la solicitud de login. Intente más tarde.", e);
        }

        if (usuarioDeBD == null) {
             LogsError.warn(ServicioUsuario.class, "Servicio: Login fallido - Usuario no encontrado: " + correo);
            throw new UsuarioException("Credenciales incorrectas. Verifique su correo y contraseña.");
        }

        if (!usuarioDeBD.isEstado()) {
             LogsError.warn(ServicioUsuario.class, "Servicio: Login fallido - Usuario inactivo: " + correo);
            throw new UsuarioException("Su cuenta de usuario esta inactiva. Contacte al administrador.");
        }

        if (!usuarioDeBD.getContrasena().equals(contrasenaIngresada)) {
             LogsError.warn(ServicioUsuario.class, "Servicio: Login fallido - Contraseña incorrecta para: " + correo);
            throw new UsuarioException("Credenciales incorrectas. Verifique su correo y contraseña.");
        }

         LogsError.info(ServicioUsuario.class, "Servicio: Login exitoso para el usuario: " + usuarioDeBD.getNombre() + " (" + correo + ")");
        return usuarioDeBD;
    }

        //metodo para trabajar los registros de usuario
    public Usuario registrarUsuario(String nombre, String correo, String contrasena, int idTipoUsuario) throws UsuarioException {
         LogsError.info(ServicioUsuario.class, "Servicio: Intento de registrar nuevo usuario con correo: " + correo);

        if (nombre == null || nombre.trim().isEmpty() ||
            correo == null || correo.trim().isEmpty() ||
            contrasena == null || contrasena.isEmpty() ||
            idTipoUsuario <= 0) {
             LogsError.warn(ServicioUsuario.class, "Servicio: Intento de registro con datos inválidos.");
             throw new UsuarioException("Datos incompletos o invalidos para el registro.");
        }

        try {
            // Verificar correo existente ANTES de buscar el tipo de usuario
            Usuario existente = usuarioDAO.obtenerPorCorreo(correo.trim());
            if (existente != null) {
                LogsError.warn(ServicioUsuario.class, "Servicio: Registro fallido - El correo " + correo.trim() + "ya existe.");
                throw new UsuarioException("El correo electronico '" + correo.trim() + "' ya esta registrado.");
            }

            TipoUsuario tipoUsuario = tipoUsuarioDAO.obtenerPorId(idTipoUsuario);
            if (tipoUsuario == null) {
                 LogsError.warn(ServicioUsuario.class, "Servicio: Registro fallido - Tipo de usuario ID " + idTipoUsuario + " no encontrado.");
                 throw new UsuarioException("El tipo de usuario especificado no es valido.");
            }

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre.trim());
            nuevoUsuario.setCorreo(correo.trim());
            nuevoUsuario.setContrasena(contrasena); // Aquí se guardara el hash en un sistema 
            nuevoUsuario.setTipoUsuario(tipoUsuario);
            nuevoUsuario.setEstado(true); 

            Usuario usuarioCreado = usuarioDAO.crearUsuario(nuevoUsuario);

            if (usuarioCreado == null || usuarioCreado.getId() == 0) {
                 LogsError.error(ServicioUsuario.class, "Servicio: Fallo en DAO al crear usuario con correo: " + correo.trim());
                 throw new UsuarioException("No se pudo registrar el usuario debido a un error interno del servidor.");
            }
            
             LogsError.info(ServicioUsuario.class, "Servicio: Usuario registrado exitosamente: " + usuarioCreado.getCorreo() + " con ID: " + usuarioCreado.getId());
            return usuarioCreado;

        } catch (SQLException e) {
             LogsError.error(ServicioUsuario.class, "Servicio: Error de BD durante registro para: " + correo.trim(), e);
             throw new UsuarioException("Error al procesar el registro. Intente mas tarde.", e);
        }
    }

        //metodo para restablecer la contraseña
    public boolean restablecerContrasena(String correo, String nuevaContrasena) throws UsuarioException {
         LogsError.info(ServicioUsuario.class, "Servicio: Intento de restablecer contraseña para: " + correo);

        if (correo == null || correo.trim().isEmpty() ||
            nuevaContrasena == null || nuevaContrasena.isEmpty()) {
             LogsError.warn(ServicioUsuario.class, "Servicio: Intento de restablecer contraseña con datos vacios.");
             throw new UsuarioException("Correo o nueva contraseña no pueden estar vacios.");
        }

        try {
            // Verificar primero si el usuario existe
            Usuario usuarioExistente = usuarioDAO.obtenerPorCorreo(correo.trim());
            if (usuarioExistente == null) {
                 LogsError.warn(ServicioUsuario.class, "Servicio: Restablecimiento fallido - Usuario no encontrado: " + correo.trim());
                 throw new UsuarioException("No se encontro un usuario con el correo especificado.");
            }

            boolean exito = usuarioDAO.actualizarContrasena(correo.trim(), nuevaContrasena);
            if (!exito) {
                  LogsError.warn(ServicioUsuario.class, "Servicio: DAO no actualizó contraseña para: " + correo.trim());
                  
               
                  throw new UsuarioException("No se pudo actualizar la contraseña. Verifique el correo o intente más tarde.");
            }
            LogsError.info(ServicioUsuario.class, "Servicio: Contraseña restablecida exitosamente para: " + correo.trim());
            return true;

        } catch (SQLException e) {
             LogsError.error(ServicioUsuario.class, "Servicio: Error de BD al restablecer contraseña para: " + correo.trim(), e);
             throw new UsuarioException("Error al procesar el restablecimiento de contraseña.", e);
        }
    }
    
       //metodo para la gestion de usuario

    public Usuario gestionarUsuario(int idUsuario, String nuevoNombre, String nuevoCorreo, int idNuevoTipoUsuario, boolean nuevoEstado) throws UsuarioException {
         LogsError.info(ServicioUsuario.class, "Servicio: Intento de gestionar datos para usuario ID: " + idUsuario);
        
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty() ||
            nuevoCorreo == null || nuevoCorreo.trim().isEmpty() ||
            idNuevoTipoUsuario <= 0) {
            LogsError.warn(ServicioUsuario.class, "Servicio: Intento de gestionar usuario ID " + idUsuario + " con datos invalidos.");
            throw new UsuarioException("Datos de entrada inválidos para gestionar usuario.");
        }

        try {
            Usuario usuarioActual = usuarioDAO.obtenerPorId(idUsuario);
            if (usuarioActual == null) {
                 LogsError.warn(ServicioUsuario.class, "Servicio: Gestion fallida - Usuario ID " + idUsuario + " no encontrado.");
                 throw new UsuarioException("Usuario con ID " + idUsuario + " no encontrado.");
            }

            TipoUsuario nuevoTipo = tipoUsuarioDAO.obtenerPorId(idNuevoTipoUsuario);
            if (nuevoTipo == null) {
                 LogsError.warn(ServicioUsuario.class, "Servicio: Gestión fallida - Nuevo Tipo de Usuario ID " + idNuevoTipoUsuario + " no encontrado.");
                 throw new UsuarioException("El nuevo tipo de usuario especificado no es válido.");
            }
            
            // Verifica si el correo se esta cambiando y si ya existe para otro usuario
            String correoTrimmed = nuevoCorreo.trim();
            if (!usuarioActual.getCorreo().equalsIgnoreCase(correoTrimmed)) {
                Usuario usuarioConNuevoCorreo = usuarioDAO.obtenerPorCorreo(correoTrimmed);
                
            // Asegurarse que si encuentra un usuario, no sea el mismo que estamos editando
                if (usuarioConNuevoCorreo != null && usuarioConNuevoCorreo.getId() != idUsuario) { 
                    LogsError.warn(ServicioUsuario.class, "Servicio: Gestion fallida - Nuevo correo " + correoTrimmed + " ya existe para otro usuario.");
                    throw new UsuarioException("El nuevo correo electronico '" + correoTrimmed + "' ya esta en uso por otro usuario.");
                    }
                 usuarioActual.setCorreo(correoTrimmed); // Actualizar solo si es diferente y no existe
            }

            usuarioActual.setNombre(nuevoNombre.trim());
            usuarioActual.setTipoUsuario(nuevoTipo);
            usuarioActual.setEstado(nuevoEstado);

            boolean exito = usuarioDAO.actualizarUsuario(usuarioActual);
            if (!exito) {
                 LogsError.warn(ServicioUsuario.class, "Servicio: DAO no actualizo datos para usuario ID: " + idUsuario);
                 throw new UsuarioException("No se pudieron actualizar los datos del usuario.");
            }
            
             LogsError.info(ServicioUsuario.class, "Servicio: Datos actualizados para usuario ID: " + idUsuario);
            return usuarioActual;

        } catch (SQLException e) {
             LogsError.error(ServicioUsuario.class, "Servicio: Error de BD al gestionar datos para usuario ID: " + idUsuario, e);
             throw new UsuarioException("Error al procesar la actualización de datos del usuario.", e);
        }
    }
    
    //listado de usuarios 
    
    public List<Usuario> obtenerTodosLosUsuarios() throws UsuarioException {
         LogsError.info(ServicioUsuario.class, "Servicio: Solicitando lista de todos los usuarios.");
        try {
            List<Usuario> usuarios = usuarioDAO.obtenerTodos();
           
            // for (Usuario u : usuarios) { u.setContrasena(null); }
             LogsError.info(ServicioUsuario.class, "Servicio: Obtenidos " + usuarios.size() + " usuarios.");
            return usuarios;
        } catch (SQLException e) {
             LogsError.error(ServicioUsuario.class, "Servicio: Error de BD al obtener todos los usuarios.", e);
             throw new UsuarioException("Error al obtener la lista de usuarios.", e);
        }
    }
    
}
