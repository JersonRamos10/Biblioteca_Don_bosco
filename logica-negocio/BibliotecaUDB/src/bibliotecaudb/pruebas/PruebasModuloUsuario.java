package bibliotecaudb.pruebas; // Asegúrate de que este sea el paquete correcto

import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.dao.usuario.TipoUsuarioDAO;
import bibliotecaudb.dao.usuario.UsuarioDAO;
import bibliotecaudb.dao.usuario.impl.TipoUsuarioDAOImpl;
import bibliotecaudb.dao.usuario.impl.UsuarioDAOImpl;
import bibliotecaudb.conexion.ConexionBD; // Para cerrar la conexión al final si es necesario
import bibliotecaudb.conexion.LogsError; // Para mensajes en la prueba

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class PruebasModuloUsuario {

    public static void main(String[] args) {
        // IMPORTANTE: Implementar Hashing de Contraseñas antes de usar en un entorno real.
        // Estas pruebas usan contraseñas en texto plano solo para fines de demostración del DAO.

        TipoUsuarioDAO tipoUsuarioDAO = new TipoUsuarioDAOImpl();
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(tipoUsuarioDAO); // Inyectamos el tipoUsuarioDAO
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== INICIO DE PRUEBAS DEL MÓDULO DE USUARIOS ===");

            // 1. Probar TipoUsuarioDAO
            // ---------------------------------------------------------------
            System.out.println("\n--- Pruebas TipoUsuarioDAO ---");
            // 1.1 Obtener todos los tipos de usuario
            System.out.println("\n[Prueba 1.1: Obtener todos los tipos de usuario]");
            List<TipoUsuario> tipos = tipoUsuarioDAO.obtenerTodos();
            if (tipos.isEmpty()) {
                System.out.println("No se encontraron tipos de usuario. Verifica tu tabla 'tipo_usuario'.");
            } else {
                tipos.forEach(System.out::println);
            }

            // 1.2 Obtener tipo de usuario por ID (Ej: ID 1 = Administrador)
            System.out.println("\n[Prueba 1.2: Obtener tipo de usuario por ID (1)]");
            TipoUsuario adminTipo = tipoUsuarioDAO.obtenerPorId(1);
            if (adminTipo != null) {
                System.out.println("Tipo encontrado: " + adminTipo);
            } else {
                System.out.println("Tipo de usuario con ID 1 no encontrado.");
            }
            
            System.out.println("\n[Prueba 1.3: Obtener tipo de usuario por nombre ('Alumno')]");
            TipoUsuario alumnoTipoPorNombre = tipoUsuarioDAO.obtenerPorNombre("Alumno");
            if (alumnoTipoPorNombre != null) {
                System.out.println("Tipo encontrado: " + alumnoTipoPorNombre);
            } else {
                System.out.println("Tipo de usuario 'Alumno' no encontrado.");
            }


            // 2. Probar UsuarioDAO
            // ---------------------------------------------------------------
            System.out.println("\n--- Pruebas UsuarioDAO ---");

            // 2.1 Insertar un nuevo usuario
            System.out.println("\n[Prueba 2.1: Insertar nuevo usuario (Profesor de Prueba)]");
            Usuario nuevoProfesor = new Usuario();
            nuevoProfesor.setNombre("Profesor Prueba JUnit");
            nuevoProfesor.setCorreo("profesor.junit@udb.edu.sv");
            nuevoProfesor.setContrasena("ProfeJunit123!"); // ¡HASH ESTO!
            nuevoProfesor.setIdTipoUsuario(2); // ID 2 para Profesor (según tu SQL)
            nuevoProfesor.setEstado(true);

            boolean insertado = usuarioDAO.insertar(nuevoProfesor);
            if (insertado && nuevoProfesor.getId() > 0) {
                System.out.println("Usuario insertado con éxito: " + nuevoProfesor);
            } else {
                System.out.println("Falló la inserción del usuario.");
            }

            // 2.2 Obtener usuario por correo
            System.out.println("\n[Prueba 2.2: Obtener usuario por correo (profesor.junit@udb.edu.sv)]");
            Usuario profesorObtenido = usuarioDAO.obtenerPorCorreo("profesor.junit@udb.edu.sv");
            if (profesorObtenido != null) {
                System.out.println("Usuario obtenido por correo: " + profesorObtenido);
                // Verificar que el TipoUsuario también se cargó
                if (profesorObtenido.getTipoUsuario() != null) {
                    System.out.println("  -> Tipo de Usuario: " + profesorObtenido.getTipoUsuario().getTipo());
                } else {
                    System.out.println("  -> Tipo de Usuario no cargado (revisar mapeo).");
                }
            } else {
                System.out.println("No se encontró el usuario profesor.junit@udb.edu.sv.");
            }

            // 2.3 Validar Login (Exitoso)
            System.out.println("\n[Prueba 2.3: Validar Login (Exitoso - admin@udb.com)]");
            Usuario adminLogin = usuarioDAO.validarLogin("admin@udb.com", "AdminUdb2025."); // Contraseña de tu SQL
            if (adminLogin != null) {
                System.out.println("Login exitoso: " + adminLogin.getNombre() + ", Tipo: " + adminLogin.getTipoUsuario().getTipo());
            } else {
                System.out.println("Login fallido para admin@udb.com.");
            }

            // 2.4 Validar Login (Fallido - contraseña incorrecta)
            System.out.println("\n[Prueba 2.4: Validar Login (Fallido - contraseña incorrecta)]");
            Usuario loginFallido = usuarioDAO.validarLogin("admin@udb.com", "contrasenaIncorrecta");
            if (loginFallido == null) {
                System.out.println("Login fallido como se esperaba.");
            } else {
                System.out.println("Error: Login exitoso con contraseña incorrecta.");
            }
            
            // 2.5 Validar Login (Fallido - usuario inactivo si tuvieras uno)
            // Para esto, necesitarías un usuario con estado = 0 en tu BD o actualizar uno.
            // Por ejemplo, si el profesor de prueba lo ponemos inactivo:
            if (profesorObtenido != null) {
                System.out.println("\n[Prueba 2.5.1: Actualizar profesor de prueba a inactivo]");
                profesorObtenido.setEstado(false);
                usuarioDAO.actualizar(profesorObtenido);
                System.out.println("Profesor de prueba (" + profesorObtenido.getCorreo() +") actualizado a inactivo.");

                System.out.println("\n[Prueba 2.5.2: Validar Login (Fallido - usuario inactivo)]");
                Usuario loginInactivo = usuarioDAO.validarLogin(profesorObtenido.getCorreo(), "ProfeJunit123!");
                 if (loginInactivo == null) {
                    System.out.println("Login fallido para usuario inactivo como se esperaba.");
                } else {
                    System.out.println("Error: Login exitoso para usuario inactivo.");
                }
                // Volver a activar para otras pruebas
                profesorObtenido.setEstado(true);
                usuarioDAO.actualizar(profesorObtenido);
            }


            // 2.6 Actualizar usuario (el profesor de prueba)
            if (profesorObtenido != null) {
                System.out.println("\n[Prueba 2.6: Actualizar usuario (Profesor Prueba JUnit)]");
                String nuevoNombre = "Profesor Prueba Actualizado";
                profesorObtenido.setNombre(nuevoNombre);
                boolean actualizado = usuarioDAO.actualizar(profesorObtenido);
                if (actualizado) {
                    Usuario profesorReObtenido = usuarioDAO.obtenerPorId(profesorObtenido.getId());
                    if (profesorReObtenido != null && profesorReObtenido.getNombre().equals(nuevoNombre)) {
                        System.out.println("Usuario actualizado con éxito: " + profesorReObtenido);
                    } else {
                        System.out.println("Error al verificar la actualización.");
                    }
                } else {
                    System.out.println("Falló la actualización del usuario.");
                }
            }
            
            // 2.7 Restablecer contraseña (por un administrador)
            // Suponemos que el usuario que ejecuta esto es un administrador.
            // La verificación de si es admin se haría en la capa de servicio/UI.
            System.out.println("\n[Prueba 2.7: Restablecer contraseña para alumno1@udb.com]");
            String correoARestablecer = "alumno1@udb.com"; // Usuario de tu SQL
            String nuevaContrasenaAlumno = "NuevaPassAlumno123!"; // ¡HASH ESTO!
            
            Usuario alumnoParaRestablecer = usuarioDAO.obtenerPorCorreo(correoARestablecer);
            if (alumnoParaRestablecer != null) {
                 boolean contrasenaRestablecida = usuarioDAO.actualizarContrasena(correoARestablecer, nuevaContrasenaAlumno);
                if (contrasenaRestablecida) {
                    System.out.println("Contraseña restablecida para " + correoARestablecer);
                    // Verificar login con nueva contraseña
                    Usuario alumnoLoginNuevaPass = usuarioDAO.validarLogin(correoARestablecer, nuevaContrasenaAlumno);
                    if (alumnoLoginNuevaPass != null) {
                        System.out.println("Login exitoso con nueva contraseña para " + correoARestablecer);
                        // Restaurar contraseña original para no afectar otras pruebas si es necesario
                         usuarioDAO.actualizarContrasena(correoARestablecer, "AlumnoUDB2025."); // Contraseña original del SQL
                         System.out.println("Contraseña original restaurada para " + correoARestablecer);
                    } else {
                        System.out.println("Falló el login con la nueva contraseña para " + correoARestablecer);
                    }
                } else {
                    System.out.println("Falló el restablecimiento de contraseña para " + correoARestablecer);
                }
            } else {
                System.out.println("Usuario " + correoARestablecer + " no encontrado para restablecer contraseña.");
            }


            // 2.8 Obtener todos los usuarios
            System.out.println("\n[Prueba 2.8: Obtener todos los usuarios]");
            List<Usuario> todosLosUsuarios = usuarioDAO.obtenerTodos();
            if (todosLosUsuarios.isEmpty()) {
                System.out.println("No se encontraron usuarios.");
            } else {
                System.out.println("Total de usuarios: " + todosLosUsuarios.size());
                todosLosUsuarios.forEach(u -> {
                    System.out.println(u + " - Tipo: " + (u.getTipoUsuario() != null ? u.getTipoUsuario().getTipo() : "N/A"));
                });
            }

            // 2.9 Eliminar usuario (el profesor de prueba insertado)
            if (profesorObtenido != null) {
                System.out.println("\n[Prueba 2.9: Eliminar usuario (Profesor Prueba Actualizado)]");
                boolean eliminado = usuarioDAO.eliminar(profesorObtenido.getId());
                if (eliminado) {
                    Usuario profesorEliminadoVerif = usuarioDAO.obtenerPorId(profesorObtenido.getId());
                    if (profesorEliminadoVerif == null) {
                        System.out.println("Usuario eliminado con éxito.");
                    } else {
                        System.out.println("Error: Usuario encontrado después de eliminar.");
                    }
                } else {
                    System.out.println("Falló la eliminación del usuario.");
                }
            }


            System.out.println("\n=== FIN DE PRUEBAS DEL MÓDULO DE USUARIOS ===");

        } catch (SQLException e) {
            LogsError.fatal(PruebasModuloUsuario.class, "Error SQL general durante las pruebas: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            // Tu clase ConexionBD usa una conexión estática que se cierra con cerrarConexion().
            // Si decides que cada test gestione su conexión, aquí la cerrarías.
            // Por ahora, si quieres cerrar la conexión global al final de todas las pruebas:
            // ConexionBD.cerrarConexion();
            // System.out.println("Conexión a BD cerrada (si estaba abierta y es gestionada globalmente).");
            scanner.close();
        }
    }
}