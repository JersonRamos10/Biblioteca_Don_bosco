package bibliotecaudb.conexion; // Asumo que este es tu paquete

import bibliotecaudb.conexion.LogsError;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection; // Asegúrate de tener todos los imports necesarios
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class ConexionBD {
    private static Connection conexion = null;
    private static final Properties props = new Properties();

    // Bloque estático para cargar la configuración una sola vez
    static {
        loadConfig();
    }

    private static void loadConfig() {
        // Carga desde el mismo paquete que esta clase
        try (InputStream input = ConexionBD.class.getResourceAsStream("config.properties")) {
            if (input == null) {
                String errorMsg = "Archivo config.properties no encontrado en el paquete conexion.";
                LogsError.fatal(ConexionBD.class, errorMsg); // Usamos fatal porque la app no puede seguir
                throw new RuntimeException(errorMsg);
            }
            props.load(input);
            // Log de información usando la nueva clase
            LogsError.info(ConexionBD.class, String.format("Configuración cargada: URL=[%s], User=[%s]",
                    props.getProperty("db.url"), props.getProperty("db.user")));

        } catch (IOException ex) {
            String errorMsg = "Error crítico al cargar config.properties";
            LogsError.fatal(ConexionBD.class, errorMsg, ex); // Usamos fatal
            throw new RuntimeException(errorMsg, ex);
        }
    }

    /**
     * Obtiene la instancia única de la conexión a la BD.
     * Establece la conexión si es la primera vez o si está cerrada.
     * @return La conexión activa.
     * @throws SQLException Si no se puede establecer la conexión.
     */
    public static synchronized Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            // Log antes de intentar establecer
            LogsError.info(ConexionBD.class, "Conexión es null o está cerrada. Intentando establecer conexión...");
            establishConnection();
        }
        return conexion;
    }

    private static void establishConnection() throws SQLException {
        try {
            // Carga explícita del driver (aunque puede no ser necesario con JDBC 4+)
            Class.forName("com.mysql.cj.jdbc.Driver"); 

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            // Log ANTES de conectar (sin la contraseña)
             LogsError.info(ConexionBD.class, String.format("Intentando conectar a URL=[%s] con User=[%s]", url, user));

            conexion = DriverManager.getConnection(url, user, password);

            // Log DESPUÉS de conectar exitosamente
            LogsError.info(ConexionBD.class, "Conexión establecida exitosamente a: " + url);

        } catch (ClassNotFoundException ex) {
            String errorMsg = "Driver MySQL (com.mysql.cj.jdbc.Driver) no encontrado en el classpath.";
            LogsError.error(ConexionBD.class, errorMsg, ex);
            throw new SQLException(errorMsg, ex);
        } catch (SQLException ex) {
            String errorMsg = "Error SQL al conectar a la BD.";
             // Incluye el código de error SQL y el estado para más diagnóstico
            LogsError.error(ConexionBD.class, String.format("%s SQLState: %s, ErrorCode: %d", 
                            errorMsg, ex.getSQLState(), ex.getErrorCode()), ex);
            throw ex; // Relanza la excepción original para que la capa superior la maneje
        }
    }

    /**
     * Cierra la conexión Singleton si está abierta.
     * Debe llamarse al finalizar la aplicación.
     */
    public static synchronized void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    LogsError.info(ConexionBD.class, "Cerrando la conexión a la BD...");
                    conexion.close();
                    LogsError.info(ConexionBD.class, "Conexión cerrada correctamente.");
                } else {
                     LogsError.info(ConexionBD.class, "La conexión ya estaba cerrada.");
                }
            } catch (SQLException ex) {
                 LogsError.error(ConexionBD.class, "Error al cerrar la conexión a la BD.", ex);
            } finally {
                 // Para asegurar que se marque como null y se fuerce a reconectar la próxima vez
                 conexion = null; 
            }
        } else {
             LogsError.info(ConexionBD.class, "La conexión ya era null, no se necesita cerrar.");
        }
    }
}