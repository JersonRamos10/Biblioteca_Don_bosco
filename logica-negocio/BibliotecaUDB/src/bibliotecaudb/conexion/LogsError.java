/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.conexion;
// Imports de Log4j 1.x
import org.apache.log4j.Logger;
/**
 *
 * @author jerson_ramos
 */
public class LogsError {
    // --- Métodos de Logging Estáticos ---
   

    public static void debug(Class<?> clazz, String mensaje) {
        Logger.getLogger(clazz).debug(mensaje);
    }

    public static void debug(Class<?> clazz, String mensaje, Throwable t) {
        Logger.getLogger(clazz).debug(mensaje, t);
    }

    public static void info(Class<?> clazz, String mensaje) {
        Logger.getLogger(clazz).info(mensaje);
    }

    public static void info(Class<?> clazz, String mensaje, Throwable t) {
        Logger.getLogger(clazz).info(mensaje, t);
    }

    public static void warn(Class<?> clazz, String mensaje) {
        Logger.getLogger(clazz).warn(mensaje);
    }

    public static void warn(Class<?> clazz, String mensaje, Throwable t) {
        Logger.getLogger(clazz).warn(mensaje, t);
    }

    public static void error(Class<?> clazz, String mensaje) {
        Logger.getLogger(clazz).error(mensaje);
    }

    public static void error(Class<?> clazz, String mensaje, Throwable t) {
        Logger.getLogger(clazz).error(mensaje, t);
    }

    public static void fatal(Class<?> clazz, String mensaje) {
        Logger.getLogger(clazz).fatal(mensaje);
    }

    public static void fatal(Class<?> clazz, String mensaje, Throwable t) {
        Logger.getLogger(clazz).fatal(mensaje, t);
    }
}
