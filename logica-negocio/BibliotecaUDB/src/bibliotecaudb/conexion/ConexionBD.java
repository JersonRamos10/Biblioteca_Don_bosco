package bibliotecaudb.conexion;

import bibliotecaudb.conexion.LogsError;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 
import java.sql.SQLException;
import java.util.Properties;


public class ConexionBD {
    //variable que guarda el estado de la conexion en la BD
    private static Connection conexion ;
    //creamos una variable para crear una sola instancia
    private static final Properties props = new Properties(); // Para leer config.properties

    // Bloque estatico para cargar config.properties una sola vez al inicio.
    static {
        // Carga desde el mismo paquete que esta clase (bibliotecaudb.conexion)
        try (InputStream input = ConexionBD.class.getResourceAsStream("config.properties")) {
            if (input == null) {
                // Error critico si no se encuentra el archivo.
                LogsError.fatal(ConexionBD.class, "Archivo config.properties no encontrado.");
                throw new RuntimeException("Archivo config.properties no encontrado.");
            }
            props.load(input);
            LogsError.info(ConexionBD.class, "Configuracion de BD cargada.");
        } catch (IOException ex) {
            LogsError.fatal(ConexionBD.class, "Error critico al cargar config.properties: " + ex.getMessage());
            throw new RuntimeException("Error critico al cargar config.properties", ex);
        }
    }

    /**
     * Obtiene una conexion a la base de datos.
     * Si no existe una conexion o esta cerrada, crea una nueva.
     * Es synchronized para evitar problemas si multiples hilos la llaman.
     * @return La conexión activa.
     * @throws SQLException Si no se puede establecer la conexion.
     */
    public static synchronized Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                // Carga explicita del driver 
                Class.forName("com.mysql.cj.jdbc.Driver"); 

                String url = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String password = props.getProperty("db.password");

                LogsError.info(ConexionBD.class, "Intentando conectar a BD: " + url);
                conexion = DriverManager.getConnection(url, user, password);
                LogsError.info(ConexionBD.class, "Conexion establecida exitosamente.");

            } catch (ClassNotFoundException ex) {
                LogsError.error(ConexionBD.class, "Driver MySQL no encontrado.", ex);
                throw new SQLException("Driver MySQL no encontrado.", ex);
            } catch (SQLException ex) {
                LogsError.error(ConexionBD.class, "Error SQL al conectar a la BD.", ex);
                throw ex; // Relanza para que la capa que llama se entere.
            }
        }
        return conexion;
    }

    /**
     * Cierra la conexiion a la BD si esta abierta.
     * Llamar al finalizar la aplicación.
     */
    public static synchronized void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    LogsError.info(ConexionBD.class, "Cerrando conexion a la BD...");
                    conexion.close();
                    LogsError.info(ConexionBD.class, "Conexion cerrada.");
                }
            } catch (SQLException ex) {
                LogsError.error(ConexionBD.class, "Error al cerrar la conexion.", ex);
            } finally {
                conexion = null; // Asegura que se cree una nueva la proxima vez.
            }
        }
    }
    
    /** Cierra un ResultSet de forma segura
        @param rs es el resultSet a cerrar
    */
    
    public static void close(ResultSet rs){
       if (rs != null){
           try{
               rs.close();
           }catch (SQLException ex){
               //Usamos la clase LogsError
               LogsError.error(ConexionBD.class, "Error al cerrar ResultSet", ex);
            }
       }  
    }
    /**
     * Cierra un PreparedStatement de forma segura.
     * @param pstmt El PreparedStatement a cerrar.
     */
    
   public static void close(PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException ex) {
                // Usamos tu clase LogsError
                LogsError.error(ConexionBD.class, "Error al cerrar PreparedStatement", ex);
            }
        }
    }
    
   /**
     * @param conn la connection a cerrar.
     */
   
    public static void close(Connection conn) {
         if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                 LogsError.error(ConexionBD.class, "Error al cerrar Connection", ex);
            }
        }
    }
}