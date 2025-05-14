package bibliotecaudb.pruebas;

import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import java.sql.Connection;
import java.sql.SQLException;

public class PruebaConexionBD {

    public static void main(String[] args) {
        Connection conn = null;
        try {
            LogsError.info(PruebaConexionBD.class, "Intentando obtener conexión a la base de datos...");
            conn = ConexionBD.getConexion(); // Intenta obtener la conexión

            if (conn != null && !conn.isClosed()) {
                LogsError.info(PruebaConexionBD.class, "¡Conexión a la base de datos establecida exitosamente!");
                LogsError.info(PruebaConexionBD.class, "URL de la BD: " + conn.getMetaData().getURL());
                LogsError.info(PruebaConexionBD.class, "Usuario de la BD: " + conn.getMetaData().getUserName());
            } else {
                LogsError.error(PruebaConexionBD.class, "No se pudo establecer la conexión a la base de datos o la conexión está cerrada.");
            }

        } catch (SQLException e) {
            LogsError.error(PruebaConexionBD.class, "Error de SQL al intentar conectar: " + e.getMessage(), e);
            e.printStackTrace(); // Imprimir la traza para más detalles
        } catch (Exception e) {
            LogsError.fatal(PruebaConexionBD.class, "Error inesperado durante la prueba de conexión: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            // La clase ConexionBD maneja una conexión estática.
            // Si quisieras cerrarla explícitamente después de esta prueba, podrías llamar a:
            // ConexionBD.cerrarConexion();
            // LogsError.info(PruebaConexionBD.class, "Intento de cierre de conexión (si es gestionada globalmente).");
            // Pero para una prueba simple de "si conecta", no es estrictamente necesario cerrarla aquí,
            // especialmente si otras pruebas la van a usar inmediatamente.
            // Considera el ciclo de vida de tu conexión estática.
        }
    }
}