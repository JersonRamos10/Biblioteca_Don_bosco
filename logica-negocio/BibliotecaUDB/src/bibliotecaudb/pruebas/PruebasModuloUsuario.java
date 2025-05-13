package bibliotecaudb.pruebas; // O el paquete donde quieras poner tu clase de pruebas

// Importa las clases necesarias
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.servicio.ServicioUsuario; // El servicio que vamos a probar
import bibliotecaudb.dao.usuario.TipoUsuarioDAO; // Para obtener un tipo de usuario válido

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PruebasModuloUsuario {

    public static void main(String[] args) {
        // Instancia del servicio
        ServicioUsuario usuarioService = new ServicioUsuario();
        // Instancia del DAO de TipoUsuario (necesaria para crear usuarios)
        TipoUsuarioDAO tipoUsuarioDAO = new TipoUsuarioDAO();

        // --- 1. PRUEBA DE CONEXIÓN A LA BASE DE DATOS ---
        System.out.println("--- INICIANDO PRUEBA DE CONEXIÓN ---");
        Connection connTest = null;
        try {
            connTest = ConexionBD.getConexion();
            if (connTest != null && !connTest.isClosed()) {
                System.out.println("=> ÉXITO: Conexión a la base de datos establecida correctamente.");
                LogsError.info(PruebasModuloUsuario.class, "Prueba de conexión exitosa.");
                // OJO: No cerramos la conexión aquí todavía si vamos a seguir usándola
                // a través del servicio y DAO que comparten la conexión estática.
                // La cerraremos al final de todas las pruebas.
            } else {
                System.err.println("=> ERROR: No se pudo obtener una conexión válida.");
                LogsError.error(PruebasModuloUsuario.class, "Prueba de conexión fallida.");
                // Si la conexión falla aquí, probablemente no tenga sentido continuar.
                return;
            }
        } catch (SQLException e) {
            System.err.println("=> ERROR SQL al intentar conectar: " + e.getMessage());
            e.printStackTrace(); // Muestra el stack trace completo del error
            return; // Salir si no hay conexión
        }
        System.out.println("-------------------------------------\n");


        // --- 2. PRUEBAS DEL UsuarioService ---
        System.out.println("--- INICIANDO PRUEBAS UsuarioService ---");

        try {
            // A. Listar usuarios iniciales
            System.out.println("\n[Prueba Listar Usuarios Iniciales]");
            List<Usuario> usuariosIniciales = usuarioService.obtenerTodosLosUsuarios();
            if (usuariosIniciales.isEmpty()) {
                System.out.println("No hay usuarios iniciales en la BD.");
            } else {
                System.out.println("Usuarios actuales (" + usuariosIniciales.size() + "):");
                for (Usuario u : usuariosIniciales) {
                    System.out.println("  " + u.toString()); // Usa el toString() de Usuario
                }
            }

            // B. Buscar usuarios específicos
            System.out.println("\n[Prueba Buscar Usuarios Específicos]");
            Usuario admin = usuarioService.buscarUsuarioPorCorreo("admin@udb.com");
            System.out.println("Buscando admin@udb.com: " + (admin != null ? admin.toString() : "No encontrado"));
            Usuario noExiste = usuarioService.buscarUsuarioPorCorreo("noexiste@udb.com");
            System.out.println("Buscando noexiste@udb.com: " + (noExiste != null ? noExiste.toString() : "No encontrado"));
            Usuario porId = usuarioService.buscarUsuarioPorId(2); // Buscar Profesor1
            System.out.println("Buscando usuario con ID 2: " + (porId != null ? porId.toString() : "No encontrado"));


            // C. Probar Login
            System.out.println("\n[Prueba Login]");
            Usuario loginOk = usuarioService.login("admin@udb.com", "AdminUdb2025.");
            System.out.println("Login admin@udb.com (correcto): " + (loginOk != null ? "ÉXITO - " + loginOk.getNombre() : "FALLO"));
            Usuario loginPassErr = usuarioService.login("admin@udb.com", "contraseñamal");
            System.out.println("Login admin@udb.com (pass mal): " + (loginPassErr != null ? "ÉXITO (ERROR!)" : "FALLO (esperado)"));
            Usuario loginCorreoErr = usuarioService.login("error@udb.com", "AdminUdb2025.");
            System.out.println("Login error@udb.com: " + (loginCorreoErr != null ? "ÉXITO (ERROR!)" : "FALLO (esperado)"));


            // D. Crear Nuevo Usuario
            System.out.println("\n[Prueba Crear Usuario]");
            // Primero obtenemos un TipoUsuario válido (ej: Alumno con ID 3)
            TipoUsuario tipoAlumno = tipoUsuarioDAO.obtenerPorId(3);
            if (tipoAlumno != null) {
                Usuario nuevo = new Usuario(0, "Nuevo Alumno", "nuevo@udb.com", "NuevoPass123", tipoAlumno, true);
                try {
                    usuarioService.crearUsuario(nuevo);
                    System.out.println("Usuario nuevo@udb.com creado exitosamente.");
                    // Verificación
                    Usuario creado = usuarioService.buscarUsuarioPorCorreo("nuevo@udb.com");
                    System.out.println("Verificación de creación: " + (creado != null ? creado.toString() : "No encontrado!"));
                } catch (Exception e) {
                    System.err.println("ERROR al crear usuario nuevo@udb.com: " + e.getMessage());
                }
            } else {
                 System.err.println("No se pudo encontrar TipoUsuario con ID 3 para crear el nuevo usuario.");
            }

            // D.1 Intentar crear con correo duplicado
            System.out.println("\n[Prueba Crear Usuario Duplicado]");
             if (tipoAlumno != null) {
                Usuario duplicado = new Usuario(0, "Otro Nombre", "admin@udb.com", "OtraPass", tipoAlumno, true);
                try {
                    usuarioService.crearUsuario(duplicado);
                    System.err.println("ERROR: Se permitió crear usuario con correo duplicado admin@udb.com");
                } catch (Exception e) {
                    System.out.println("ÉXITO: No se permitió crear usuario duplicado (Esperado). Mensaje: " + e.getMessage());
                }
             }


            // E. Actualizar Usuario
            System.out.println("\n[Prueba Actualizar Usuario]");
            Usuario aActualizar = usuarioService.buscarUsuarioPorCorreo("nuevo@udb.com");
            if (aActualizar != null) {
                aActualizar.setNombre("Alumno Nuevo Actualizado");
                aActualizar.setEstado(false); // Cambiamos también el estado
                try {
                    usuarioService.actualizarUsuario(aActualizar);
                    System.out.println("Usuario nuevo@udb.com actualizado.");
                    // Verificación
                    Usuario actualizado = usuarioService.buscarUsuarioPorId(aActualizar.getId());
                    System.out.println("Verificación de actualización: " + (actualizado != null ? actualizado.toString() : "No encontrado!"));
                } catch (Exception e) {
                    System.err.println("ERROR al actualizar usuario nuevo@udb.com: " + e.getMessage());
                }
            } else {
                System.out.println("No se encontró el usuario nuevo@udb.com para actualizar.");
            }


            // F. Restablecer Contraseña
            System.out.println("\n[Prueba Restablecer Contraseña]");
            Usuario adminUser = usuarioService.login("admin@udb.com", "AdminUdb2025."); // Obtenemos al admin
            Usuario profesorUser = usuarioService.login("profesor1@udb.com","ProfesorUDB2025."); //Obtenemos al profesor

            if (adminUser != null) {
                // F.1 Intento válido por Admin
                try {
                    usuarioService.restablecerContrasena(adminUser, "alumno1@udb.com", "PassCambiado123");
                    System.out.println("ÉXITO: Contraseña restablecida para alumno1@udb.com por admin.");
                    // Verificación (opcional, intentar login con nueva pass)
                    Usuario alumnoLogin = usuarioService.login("alumno1@udb.com", "PassCambiado123");
                    System.out.println("Verificación login alumno1 con nueva pass: " + (alumnoLogin != null ? "ÉXITO" : "FALLO"));
                } catch (Exception e) {
                    System.err.println("ERROR al restablecer contraseña (admin): " + e.getMessage());
                }

                // F.2 Intento para usuario inexistente
                 try {
                    usuarioService.restablecerContrasena(adminUser, "noexiste@udb.com", "OtraPass");
                    System.err.println("ERROR: Se permitió restablecer contraseña para usuario inexistente.");
                } catch (Exception e) {
                     System.out.println("ÉXITO: No se permitió restablecer contraseña a usuario inexistente (Esperado). Mensaje: " + e.getMessage());
                }

            } else {
                 System.err.println("No se pudo obtener el usuario admin para la prueba de restablecer contraseña.");
            }

             // F.3 Intento inválido por No-Admin (Profesor)
             if(profesorUser != null) {
                 try {
                     usuarioService.restablecerContrasena(profesorUser, "alumno1@udb.com", "IntentoFallido");
                     System.err.println("ERROR: Se permitió restablecer contraseña por usuario no admin (profesor).");
                 } catch (Exception e) {
                     System.out.println("ÉXITO: No se permitió restablecer contraseña por no-admin (Esperado). Mensaje: " + e.getMessage());
                 }
             } else {
                 System.err.println("No se pudo obtener el usuario profesor para la prueba de restablecer contraseña.");
             }


            // G. Eliminar Usuario
            System.out.println("\n[Prueba Eliminar Usuario]");
            Usuario aEliminar = usuarioService.buscarUsuarioPorCorreo("nuevo@udb.com");
            if (aEliminar != null) {
                try {
                    usuarioService.eliminarUsuario(aEliminar.getId());
                    System.out.println("Usuario nuevo@udb.com eliminado.");
                     // Verificación
                    Usuario eliminado = usuarioService.buscarUsuarioPorCorreo("nuevo@udb.com");
                    System.out.println("Verificación de eliminación: " + (eliminado == null ? "ÉXITO (No encontrado)" : "FALLO (Aún existe)"));
                } catch (Exception e) {
                     System.err.println("ERROR al eliminar usuario nuevo@udb.com: " + e.getMessage());
                }
            } else {
                System.out.println("No se encontró el usuario nuevo@udb.com para eliminar (quizás falló la actualización).");
            }


        } catch (Exception e) {
            // Captura general por si alguna operación del servicio lanza Exception
            System.err.println("\n!!! ERROR INESPERADO DURANTE LAS PRUEBAS DEL SERVICIO: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // --- 3. CIERRE FINAL DE CONEXIÓN ---
            System.out.println("\n--- FINALIZANDO PRUEBAS Y CERRANDO CONEXIÓN ---");
            ConexionBD.cerrarConexion(); // Cierra la conexión estática
            System.out.println("----------------------------------------------");
        }
    }
}