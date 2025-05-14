package bibliotecaudb.pruebas;

import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.servicios.UsuarioService;
import bibliotecaudb.servicios.impl.UsuarioServiceImpl;
import bibliotecaudb.excepciones.UsuarioException; // Usando tu excepción
import bibliotecaudb.conexion.LogsError;

import java.sql.SQLException;
import java.util.List;

public class PruebasUsuarioService {

    public static void main(String[] args) {
        UsuarioService usuarioService = new UsuarioServiceImpl();
        Usuario adminAutenticado = null; // Para realizar acciones de administrador

        LogsError.info(PruebasUsuarioService.class, "=== INICIO DE PRUEBAS DEL SERVICIO DE USUARIOS ===");

        try {
            // 1. Autenticar Administrador para usarlo en otras pruebas
            LogsError.info(PruebasUsuarioService.class, "\n[Prueba 1: Autenticar Admin]");
            try {
                adminAutenticado = usuarioService.autenticarUsuario("admin@udb.com", "AdminUdb2025.");
                LogsError.info(PruebasUsuarioService.class, "Admin autenticado: " + adminAutenticado.getNombre() + ", Tipo: " + adminAutenticado.getTipoUsuario().getTipo());
            } catch (UsuarioException e) {
                LogsError.error(PruebasUsuarioService.class, "Error autenticando admin: " + e.getMessage());
                LogsError.info(PruebasUsuarioService.class, "No se pueden continuar muchas pruebas sin un admin autenticado.");
                // Podrías decidir salir o continuar con pruebas que no requieran un admin.
            } catch (SQLException e) {
                LogsError.error(PruebasUsuarioService.class, "Error SQL autenticando admin: " + e.getMessage(), e);
                return; // Salir si hay error de BD
            }

            // 2. Intentar autenticar usuario inexistente
            LogsError.info(PruebasUsuarioService.class, "\n[Prueba 2: Autenticar Usuario Inexistente]");
            try {
                usuarioService.autenticarUsuario("noexiste@udb.com", "cualquierpass");
                LogsError.error(PruebasUsuarioService.class, "FALLO: Se autenticó un usuario que no debería existir.");
            } catch (UsuarioException e) {
                LogsError.info(PruebasUsuarioService.class, "ÉXITO: No se autenticó usuario inexistente: " + e.getMessage());
            } catch (SQLException e) {
                LogsError.error(PruebasUsuarioService.class, "Error SQL en Prueba 2: " + e.getMessage(), e);
            }
            
            // 3. Registrar un nuevo usuario (si el admin se autenticó)
            if (adminAutenticado != null) {
                LogsError.info(PruebasUsuarioService.class, "\n[Prueba 3: Registrar Nuevo Usuario (por Admin)]");
                Usuario nuevoTestUsuario = new Usuario();
                String correoNuevoTestUsuario = "testuser" + System.currentTimeMillis() + "@example.com";
                nuevoTestUsuario.setNombre("Usuario Prueba Servicio");
                nuevoTestUsuario.setCorreo(correoNuevoTestUsuario);
                nuevoTestUsuario.setContrasena("TestPass123"); // Recordar: En real, hashear
                nuevoTestUsuario.setIdTipoUsuario(3); // Alumno
                nuevoTestUsuario.setEstado(true);
                try {
                    boolean registrado = usuarioService.registrarNuevoUsuario(nuevoTestUsuario, adminAutenticado);
                    if (registrado) {
                        LogsError.info(PruebasUsuarioService.class, "ÉXITO: Nuevo usuario registrado: " + correoNuevoTestUsuario + " con ID: " + nuevoTestUsuario.getId());

                        // 3.1 Intentar registrar el mismo correo de nuevo (debería fallar)
                        LogsError.info(PruebasUsuarioService.class, "\n[Prueba 3.1: Intentar Registrar Mismo Correo]");
                        Usuario duplicadoTestUsuario = new Usuario();
                        duplicadoTestUsuario.setNombre("Otro Nombre");
                        duplicadoTestUsuario.setCorreo(correoNuevoTestUsuario);
                        duplicadoTestUsuario.setContrasena("OtraPass");
                        duplicadoTestUsuario.setIdTipoUsuario(3);
                        duplicadoTestUsuario.setEstado(true);
                        try {
                             usuarioService.registrarNuevoUsuario(duplicadoTestUsuario, adminAutenticado);
                             LogsError.error(PruebasUsuarioService.class, "FALLO: Se registró usuario con correo duplicado.");
                        } catch (UsuarioException e) {
                            LogsError.info(PruebasUsuarioService.class, "ÉXITO: No se registró correo duplicado: " + e.getMessage());
                        }

                        // 3.2 Cambiar contraseña del nuevo usuario (por Admin)
                        LogsError.info(PruebasUsuarioService.class, "\n[Prueba 3.2: Cambiar Contraseña (por Admin)]");
                        String nuevaPass = "NuevaSuperPass789";
                        boolean passCambiada = usuarioService.cambiarContrasena(correoNuevoTestUsuario, nuevaPass, adminAutenticado);
                        if (passCambiada) {
                            LogsError.info(PruebasUsuarioService.class, "ÉXITO: Contraseña cambiada para " + correoNuevoTestUsuario);
                            // Verificar autenticación con nueva contraseña
                            Usuario usrConNuevaPass = usuarioService.autenticarUsuario(correoNuevoTestUsuario, nuevaPass);
                            LogsError.info(PruebasUsuarioService.class, "Autenticación con nueva contraseña exitosa para: " + usrConNuevaPass.getNombre());
                        } else {
                            LogsError.error(PruebasUsuarioService.class, "FALLO: No se pudo cambiar la contraseña para " + correoNuevoTestUsuario);
                        }

                    } else {
                        LogsError.error(PruebasUsuarioService.class, "FALLO: No se registró el nuevo usuario.");
                    }
                } catch (UsuarioException e) {
                    LogsError.error(PruebasUsuarioService.class, "Error registrando nuevo usuario: " + e.getMessage());
                } catch (SQLException e) {
                    LogsError.error(PruebasUsuarioService.class, "Error SQL en Prueba 3: " + e.getMessage(), e);
                }
            } else {
                 LogsError.warn(PruebasUsuarioService.class, "Saltando pruebas de registro y cambio de contraseña porque el admin no se autenticó.");
            }
            
            // 4. Obtener todos los usuarios (si el admin se autenticó)
            if (adminAutenticado != null) {
                LogsError.info(PruebasUsuarioService.class, "\n[Prueba 4: Obtener Todos los Usuarios (por Admin)]");
                try {
                    List<Usuario> todos = usuarioService.obtenerTodosLosUsuarios(adminAutenticado);
                    LogsError.info(PruebasUsuarioService.class, "Total de usuarios obtenidos: " + todos.size());
                    // todos.forEach(u -> LogsError.info(PruebasUsuarioService.class, u.toString())); // Descomentar para ver detalles
                } catch (UsuarioException e) {
                     LogsError.error(PruebasUsuarioService.class, "Error obteniendo todos los usuarios: " + e.getMessage());
                } catch (SQLException e) {
                    LogsError.error(PruebasUsuarioService.class, "Error SQL en Prueba 4: " + e.getMessage(), e);
                }
            }


        } catch (Exception e) { // Captura general para errores no esperados en la configuración de pruebas
            LogsError.fatal(PruebasUsuarioService.class, "ERROR INESPERADO EN LA CONFIGURACIÓN DE PRUEBAS DE USUARIO: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            LogsError.info(PruebasUsuarioService.class, "\n=== FIN DE PRUEBAS DEL SERVICIO DE USUARIOS ===");
        }
    }
}