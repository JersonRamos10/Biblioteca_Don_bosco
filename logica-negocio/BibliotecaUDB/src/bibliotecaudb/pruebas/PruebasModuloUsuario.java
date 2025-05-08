/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.pruebas;

import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.servicio.ServicioUsuario;
import bibliotecaudb.excepciones.UsuarioException;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError; // Importa tu clase de logs
/**
 *
 * @author jerson_ramos
 */
public class PruebasModuloUsuario {
    
    // --- INSTRUCCIONES ---
        // 1. Asegurarse de haber configurado la BD local y el archivo config.properties del proyecto
        //    (ver README_Configuracion_Usuario.md).
        // 2. Descomenta UNO de los bloques de prueba a la vez (entre // --- BLOQUE ... y // --- FIN BLOQUE...).
        // 3. Ejecuta este archivo (Run File).
        // 4. Observa la salida en la consola y los logs en biblioteca_app.log.
        // 5. Vuelve a comentar el bloque antes de probar otro.
        // ---------------------
    
    public static void main(String[] args) {
    
    
    LogsError.info(PruebasModuloUsuario.class, "--- INICIO DE PRUEBAS MANUALES ---");
        ServicioUsuario servicioUsuario = new ServicioUsuario();

        //<editor-fold defaultstate="collapsed" desc="BLOQUE: Prueba de Conexión Básica (Obtener Tipos Usuario)">
        
        //descomentar o comentar desde aqui! 
        System.out.println("\n--- BLOQUE: Prueba de Conexión Básica (Obtener Tipos Usuario) ---");
        try {
            // Intenta obtener la conexión y leer los tipos de usuario
            java.util.List<TipoUsuario> tipos = new bibliotecaudb.dao.usuario.TipoUsuarioDAO().obtenerTodos();
            if (tipos != null && !tipos.isEmpty()) {
                System.out.println("Conexión y lectura de Tipos de Usuario OK. Encontrados: " + tipos.size());
                for(TipoUsuario tu : tipos) {
                    System.out.println(" -> ID: " + tu.getId() + ", Tipo: " + tu.getTipo());
                }
            } else {
                System.err.println("La conexión parece funcionar, pero no se encontraron tipos de usuario.");
            }
        } catch (Exception e) {
            System.err.println("FALLO la prueba de conexión básica:");
            LogsError.error(PruebasModuloUsuario.class, "Error en prueba básica de conexión", e);
        }
        System.out.println("--- FIN BLOQUE: Prueba de Conexión Básica ---");
        
        
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="BLOQUE: Pruebas de Login">
        
        //comentar o descomentar desde aqui!!
        
        /*System.out.println("\n--- BLOQUE: Pruebas de Login ---");
        // --- 1. Login Correcto (Admin) ---
        System.out.println("[1. Probando login admin@udb.com...]");
        try {
            Usuario admin = servicioUsuario.login("admin@udb.com", "AdminUdb2025.");
            System.out.println("   -> ÉXITO: Bienvenido " + admin.getNombre() + " (" + admin.getTipoUsuario().getTipo() + ")");
        } catch (UsuarioException e) {
            System.err.println("   -> FALLO (INESPERADO): " + e.getMessage());
        } catch (Exception e) { System.err.println("   -> ERROR INESPERADO: "); e.printStackTrace(); }

        // --- 2. Login Contraseña Incorrecta (Alumno) ---
        System.out.println("[2. Probando login alumno1@udb.com con contraseña incorrecta...]");
         try {
            Usuario alumno = servicioUsuario.login("alumno1@udb.com", "incorrecta123");
            System.out.println("   -> ÉXITO (ERROR, no debería pasar)");
        } catch (UsuarioException e) {
            System.err.println("   -> FALLO (ESPERADO): " + e.getMessage());
        } catch (Exception e) { System.err.println("   -> ERROR INESPERADO: "); e.printStackTrace(); }

        // --- 3. Login Usuario No Existente ---
         System.out.println("[3. Probando login con usuario no existente...]");
        try {
            Usuario noExiste = servicioUsuario.login("noexiste@udb.com", "cualquiera");
            System.out.println("   -> ÉXITO (ERROR, no debería pasar)");
        } catch (UsuarioException e) {
            System.err.println("   -> FALLO (ESPERADO): " + e.getMessage());
        } catch (Exception e) { System.err.println("   -> ERROR INESPERADO: "); e.printStackTrace(); }
        System.out.println("--- FIN BLOQUE: Pruebas de Login ---");
        */
        
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="BLOQUE: Prueba de Registro">
        
        //comentar o descomentar desde aqui!!
        /*
        System.out.println("\n--- BLOQUE: Prueba de Registro ---");
        // --- 1. Registrar Nuevo Usuario (Profesor) ---
        System.out.println("[1. Registrando nuevo profesor...]");
        String nuevoCorreoProf = "profe.test." + System.currentTimeMillis() + "@udb.com";
        try {
            Usuario nuevoProfesor = servicioUsuario.registrarUsuario(
                    "Profesor De Prueba", nuevoCorreoProf, "ProfeTestPass1!", 2 // ID 2 = Profesor
            );
            System.out.println("   -> ÉXITO: Registrado " + nuevoProfesor.getNombre() + " con ID " + nuevoProfesor.getId());

            // Intenta hacer login con el nuevo usuario para confirmar
            System.out.println("   Intentando login con el nuevo profesor...");
            Usuario logueado = servicioUsuario.login(nuevoCorreoProf, "ProfeTestPass1!");
            System.out.println("   -> Login post-registro EXITOSO: " + logueado.getNombre());

        } catch (UsuarioException e) {
            System.err.println("   -> FALLO REGISTRO (INESPERADO): " + e.getMessage());
        } catch (Exception e) { System.err.println("   -> ERROR INESPERADO: "); e.printStackTrace(); }

        // --- 2. Registrar Correo Duplicado ---
        System.out.println("[2. Intentando registrar admin@udb.com otra vez...]");
         try {
            servicioUsuario.registrarUsuario("Otro Admin", "admin@udb.com", "pass", 1);
            System.out.println("   -> ÉXITO (ERROR, no debería pasar)");
        } catch (UsuarioException e) {
            System.err.println("   -> FALLO (ESPERADO - Correo duplicado): " + e.getMessage());
        } catch (Exception e) { System.err.println("   -> ERROR INESPERADO: "); e.printStackTrace(); }
        System.out.println("--- FIN BLOQUE: Prueba de Registro ---");
        
        */
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="BLOQUE: Prueba Restablecer Contraseña">
        
        //comentar o descomentar desde aqui
        
        /*
        System.out.println("\n--- BLOQUE: Prueba Restablecer Contraseña ---");
        // Usa un correo que sepas que existe (ej. el admin o uno creado antes)
        String correoARestablecer = "admin@udb.com"; // O usa el 'nuevoCorreoProf' si descomentaste el bloque anterior
        String nuevaPass = "NuevaAdminPass" + System.currentTimeMillis(); // Nueva contraseña única
        String passOriginal = "AdminUdb2025."; // Contraseña original para restaurarla después

        System.out.println("[1. Intentando restablecer contraseña para " + correoARestablecer + "...]");
        try {
            boolean exito = servicioUsuario.restablecerContrasena(correoARestablecer, nuevaPass);
            if (exito) {
                 System.out.println("   -> ÉXITO: Contraseña cambiada.");
                 // Intenta login con la NUEVA contraseña
                 System.out.println("   Intentando login con nueva contraseña...");
                 Usuario user = servicioUsuario.login(correoARestablecer, nuevaPass);
                 System.out.println("   -> Login con nueva contraseña EXITOSO: " + user.getNombre());

                 // Restaurar contraseña original (para no dejarla cambiada)
                 System.out.println("   Restaurando contraseña original...");
                 servicioUsuario.restablecerContrasena(correoARestablecer, passOriginal);
                 System.out.println("   Contraseña original restaurada.");

            } else {
                System.err.println("   -> FALLO: El servicio devolvió false.");
            }
        } catch (UsuarioException e) {
            System.err.println("   -> FALLO RESTABLECIENDO: " + e.getMessage());
        } catch (Exception e) { System.err.println("   -> ERROR INESPERADO: "); e.printStackTrace(); }
        System.out.println("--- FIN BLOQUE: Prueba Restablecer Contraseña ---");
        */
        //</editor-fold>

        
        //<editor-fold defaultstate="collapsed" desc="BLOQUE: Prueba Gestionar/Actualizar Usuario">
        
        //comentar o descomentar desde aqui!!
        /*
        System.out.println("\n--- BLOQUE: Prueba Gestionar/Actualizar Usuario ---");
        // Vamos a modificar 'alumno1@udb.com' (ID 3)
        int idAlumno = 3;
        String correoAlumno = "alumno1@udb.com";
        String passAlumno = "AlumnoUDB2025."; // Necesaria para obtener el objeto inicial si se modifica correo
        String nombreOriginal = "Alumno1"; // Para restaurar
        int idTipoOriginal = 3;           // Para restaurar

        System.out.println("[1. Intentando actualizar datos del Alumno ID " + idAlumno + "...]");
        try {
             // Los datos a cambiar:
             String nombreNuevo = "Alumno Uno Actualizado";
             int idTipoNuevo = 3; // Sigue siendo Alumno
             boolean estadoNuevo = true; // Sigue activo
             // String correoNuevo = "alumno1.cambiado@udb.com"; // Opcional cambiar correo

             Usuario actualizado = servicioUsuario.gestionarUsuario(
                idAlumno, nombreNuevo, correoAlumno, idTipoNuevo, estadoNuevo // Mantenemos correo y tipo
             );
             System.out.println("   -> ÉXITO: Usuario actualizado: " + actualizado.getNombre() + 
                                " (" + actualizado.getTipoUsuario().getTipo() + ")");
             
             // Verificar los cambios (opcional, requiere obtenerPorId)
             // Usuario verificado = new UsuarioDAO().obtenerPorId(idAlumno);
             // System.out.println("   Datos verificados: " + verificado);

             // Restaurar datos originales
             System.out.println("   Restaurando datos originales...");
             servicioUsuario.gestionarUsuario(idAlumno, nombreOriginal, correoAlumno, idTipoOriginal, true);
             System.out.println("   Datos originales restaurados.");

        } catch (UsuarioException e) {
             System.err.println("   -> FALLO ACTUALIZANDO: " + e.getMessage());
        } catch (Exception e) { System.err.println("   -> ERROR INESPERADO: "); e.printStackTrace(); }
        System.out.println("--- FIN BLOQUE: Prueba Gestionar/Actualizar Usuario ---");
        */
        //</editor-fold>

        // --- Limpieza Final ---
        LogsError.info(PruebasModuloUsuario.class, "--- FIN DE PRUEBAS MANUALES ---");
        ConexionBD.cerrarConexion(); // Cierra la conexión de BD al final
    }
    
    
}
