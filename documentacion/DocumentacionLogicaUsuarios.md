# Implementación de la Lógica de Negocio: Gestión de Usuarios (Parte 1)


La arquitectura sigue una separación básica de responsabilidades:
* **Modelos:** Representan los datos (POJOs).
* **Conexión:** Gestiona la conexión a la base de datos.
* **Logging:** Maneja el registro de eventos y errores.
* **DAO (Data Access Object):** Encapsula el acceso a la base de datos para cada entidad.
* **Servicio:** Contiene la lógica de negocio y coordina las operaciones.

## 2. Estructura de Paquetes (Sugerida)

Se sugiere organizar las clases en los siguientes paquetes para mantener el orden:

* `bibliotecaudb.modelo.usuario`: Contiene las clases `Usuario.java` y `TipoUsuario.java`.
* `bibliotecaudb.conexion`: Contiene `ConexionBD.java`, `LogsError.java` y `config.properties`.
* `bibliotecaudb.dao.usuario`: Contiene `UsuarioDAOImpl.java` y `TipoUsuarioDAOImpl.java`.
* `bibliotecaudb.servicio.usuario`: Contiene `UsuarioService.java`.
* `bibliotecaudb.excepciones` (Opcional): Para excepciones personalizadas si se necesitaran.

## 3. Configuración (`config.properties`)

Para evitar tener los datos de conexión directamente en el código Java, se utiliza un archivo `config.properties`.

* **Propósito:** Centralizar los parámetros de conexión a la base de datos (URL, usuario, contraseña).
* **Ubicación:** Debe estar en el mismo paquete que `ConexionBD.java` (ej: `bibliotecaudb.conexion`) para que `getResourceAsStream` lo encuentre fácilmente.
* **Contenido (`config.properties`):**
    ```properties
    db.url=jdbc:mysql://localhost:3306/biblioteca
    db.user=root
    db.password=sudo123 
    ```
    *(Nota: Reemplazar `contraseña` con la contraseña real de MySQL o cualquier herramienta usada)*

## 4. Logging (`LogsError.java`)

Se implementó una clase de ayuda estática para simplificar el uso de Log4j.

* **Propósito:** Facilitar el registro de mensajes (INFO, WARN, ERROR, FATAL, DEBUG) desde cualquier parte de la aplicación sin necesidad de obtener una instancia de `Logger` en cada clase.
* **Implementación (`LogsError.java` - Fragmento):**
    ```java
    package bibliotecaudb.conexion;
    import org.apache.log4j.Logger;

    public class LogsError {
        public static void info(Class<?> clazz, String mensaje) {
            Logger.getLogger(clazz).info(mensaje);
        }

        public static void error(Class<?> clazz, String mensaje) {
            Logger.getLogger(clazz).error(mensaje);
        }
         
        public static void error(Class<?> clazz, String mensaje, Throwable t) {
            Logger.getLogger(clazz).error(mensaje, t);
        }
        // ... (métodos para otros niveles: warn, fatal, debug)
    }
    ```
* **Uso:** Se llama usando `LogsError.info(MiClase.class, "Este es un mensaje informativo.");`

## 5. Conexión a Base de Datos (`ConexionBD.java`)

Esta clase centraliza la gestión de la conexión JDBC.

* **Propósito:** Obtener y (eventualmente) cerrar la conexión a la base de datos, y proporcionar métodos de ayuda para cerrar recursos JDBC (`ResultSet`, `PreparedStatement`).
* **Características Principales:**
    * **Carga de Configuración:** Usa un bloque `static` para leer `config.properties` una sola vez al inicio.
    * **`getConexion()`:** Implementa un patrón Singleton simple para la conexión. Carga el driver JDBC (`com.mysql.cj.jdbc.Driver`) y usa `DriverManager.getConnection()` con los datos del archivo de propiedades. Es `synchronized` para seguridad básica en la creación inicial.
    * **Métodos `close()`:** Se añadieron métodos estáticos `close(ResultSet rs)` y `close(PreparedStatement pstmt)` para cerrar estos recursos de forma segura (verificando nulidad y capturando `SQLException`) desde los DAO. Esto simplifica el código en los bloques `finally` de los DAO.
    * **`cerrarConexion()`:** Cierra la conexión estática principal. Debe llamarse idealmente solo cuando la aplicación se detiene.
* **Implementación (`ConexionBD.java` - Fragmento `getConexion` y `close` helpers):**
    ```java
    // ... (imports y carga de props) ...
    public class ConexionBD {
        private static Connection conexion = null;
        // ... (static block) ...

        public static synchronized Connection getConexion() throws SQLException {
            if (conexion == null || conexion.isClosed()) {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver"); // Driver MySQL moderno
                    String url = props.getProperty("db.url");
                    String user = props.getProperty("db.user");
                    String password = props.getProperty("db.password");
                    LogsError.info(ConexionBD.class, "Intentando conectar a BD: " + url);
                    conexion = DriverManager.getConnection(url, user, password);
                    LogsError.info(ConexionBD.class, "Conexion establecida exitosamente.");
                } catch (ClassNotFoundException | SQLException ex) {
                    LogsError.error(ConexionBD.class, "Error al conectar/cargar driver.", ex);
                    throw new SQLException("Error al conectar a la BD: " + ex.getMessage(), ex);
                }
            }
            return conexion;
        }

        // ... (cerrarConexion) ...

        public static void close(ResultSet rs) {
            if (rs != null) { try { rs.close(); } catch (SQLException e) { LogsError.error(ConexionBD.class, "Error cerrando ResultSet", e); } }
        }

        public static void close(PreparedStatement pstmt) {
             if (pstmt != null) { try { pstmt.close(); } catch (SQLException e) { LogsError.error(ConexionBD.class, "Error cerrando PreparedStatement", e); } }
        }
        
        public static void close(Connection conn) {
             if (conn != null) { try { conn.close(); } catch (SQLException e) { LogsError.error(ConexionBD.class, "Error cerrando Connection", e); } }
        }
    }
    ```

## 6. Modelos (`TipoUsuario.java`, `Usuario.java`)

Son clases simples (POJOs) que representan las tablas de la base de datos.

* **Propósito:** Contener los datos de un tipo de usuario o un usuario. Facilitan el transporte de datos entre capas.
* **`TipoUsuario.java`:** Contiene `int id` y `String tipo`.
* **`Usuario.java`:** Contiene `int id`, `String nombre`, `String correo`, `String contrasena`, `boolean estado` y, muy importante, un objeto `TipoUsuario tipoUsuario` para representar la relación con la tabla `tipo_usuario`.
* **Implementación:** Incluyen constructores, getters y setters para todos sus atributos.

    ```java
    // --- Usuario.java (Atributos clave) ---
    public class Usuario {
        private int id;
        private String nombre;
        private String correo;
        private String contrasena;
        private TipoUsuario tipoUsuario; // Objeto para la relación
        private boolean estado;
        // ... (constructores, getters, setters) ...
    }
    ```

## 7. DAO (Data Access Objects)

Encapsulan toda la interacción con la base de datos para cada entidad. Se implementaron directamente las clases (sin interfaces previas) para mantener la simplicidad solicitada.

* **`TipoUsuarioDAOImpl.java`:**
    * **Propósito:** Leer datos de la tabla `tipo_usuario`.
    * **Métodos:** `obtenerPorId(int id)` y `listarTodos()`.
    * **Funcionamiento:** Obtienen conexión (`ConexionBD.getConexion`), preparan y ejecutan `PreparedStatement`, mapean el `ResultSet` a objetos `TipoUsuario`, y cierran `ResultSet` y `PreparedStatement` usando los helpers `ConexionBD.close()`.

        ```java
        // --- TipoUsuarioDAOImpl.java (Fragmento listarTodos) ---
        public List<TipoUsuario> listarTodos() throws SQLException {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            List<TipoUsuario> tiposUsuario = new ArrayList<>();
            try {
                conn = ConexionBD.getConexion();
                pstmt = conn.prepareStatement("SELECT id, tipo FROM tipo_usuario ORDER BY id");
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    tiposUsuario.add(new TipoUsuario(rs.getInt("id"), rs.getString("tipo")));
                }
            } finally {
                ConexionBD.close(rs);
                ConexionBD.close(pstmt);
                // NO cerramos conn aquí (es la estática)
            }
            return tiposUsuario;
        }
        ```

* **`UsuarioDAO.java`:**
    * **Propósito:** Realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) en la tabla `usuarios`.
    * **Métodos:** `insertar`, `actualizar`, `eliminar`, `obtenerPorId`, `obtenerPorCorreo`, `listarTodos`, `obtenerPorCorreoYContrasena` (para login).
    * **Funcionamiento:** Similar a `TipoUsuarioDAOImpl`, pero con más operaciones.
        * Usan `PreparedStatement` para todas las consultas, previniendo inyección SQL.
        * Los métodos `insertar` y `actualizar` usan `executeUpdate()`.
        * Los métodos de selección (`obtener...`, `listar...`) usan `executeQuery()` y un método helper `mapResultSetToUsuario` para convertir los datos del `ResultSet` a un objeto `Usuario`.
        * `mapResultSetToUsuario` utiliza `TipoUsuarioDAOImpl` para obtener el objeto `TipoUsuario` asociado.
        * Se cierran `ResultSet` y `PreparedStatement` en `finally`.

        ```java
        // --- UsuarioDAO.java (Fragmento insertar) ---
        public void insertar(Usuario usuario) throws SQLException {
            Connection conn = null;
            PreparedStatement pstmt = null;
            String SQL_INSERT = "INSERT INTO usuarios (nombre, correo, contrasena, id_tipo_usuario, estado) VALUES (?, ?, ?, ?, ?)";
            try {
                conn = ConexionBD.getConexion();
                pstmt = conn.prepareStatement(SQL_INSERT);
                pstmt.setString(1, usuario.getNombre());
                pstmt.setString(2, usuario.getCorreo());
                pstmt.setString(3, usuario.getContrasena()); // Texto plano!
                pstmt.setInt(4, usuario.getTipoUsuario().getId());
                pstmt.setBoolean(5, usuario.isEstado());
                pstmt.executeUpdate();
                LogsError.info(UsuarioDAOImpl.class, "Usuario insertado: " + usuario.getCorreo());
            } finally {
                ConexionBD.close(pstmt);
                // NO cerramos conn aquí
            }
        }

        // --- UsuarioDAOImpl.java (Fragmento mapResultSetToUsuario - clave) ---
        private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
            Usuario usuario = new Usuario();
            // ... (setear id, nombre, correo, contrasena, estado desde rs) ...
            int idTipoUsuario = rs.getInt("id_tipo_usuario");
            // Llama al otro DAO para obtener el objeto TipoUsuario
            TipoUsuario tipoUsuario = tipoUsuarioDAO.obtenerPorId(idTipoUsuario); 
            usuario.setTipoUsuario(tipoUsuario);
            return usuario;
        }
        ```

## 8. Servicio (`UsuarioService.java`)

Contiene la lógica de negocio y actúa como fachada para las operaciones sobre usuarios.

* **Propósito:** Orquestar las llamadas a los DAO, aplicar reglas de negocio y validaciones, manejar excepciones y devolver resultados a la capa superior (GUI).
* **Características Principales:**
    * Tiene una instancia de `UsuarioDAO`.
    * **`login`:** Valida entrada, llama a `usuarioDAO.obtenerPorCorreoYContrasena`, verifica si el usuario existe y está activo.
    * **`crearUsuario`:** Valida datos, comprueba si el correo ya existe (regla de negocio) llamando a `usuarioDAO.obtenerPorCorreo`, y si no existe, llama a `usuarioDAO.insertar`.
    * **`restablecerContrasena`:** Implementa la **validación de privilegios**. Verifica si el `usuarioAdmin` que ejecuta la acción es de tipo "Administrador" antes de permitir la operación. Luego busca al usuario y llama a `usuarioDAO.actualizar`.
    * **Manejo de Excepciones:** Captura `SQLException` de los DAO, las registra con `LogsError`, y las vuelve a lanzar como `Exception` (o una excepción personalizada) con un mensaje más amigable.
* **Implementación (`UsuarioService.java` - Fragmento `restablecerContrasena` con validación):**

    ```java
    public class UsuarioService {
        private UsuarioDAOImpl usuarioDAO = new UsuarioDAOImpl();
        private static final String NOMBRE_TIPO_ADMINISTRADOR = "Administrador";

        public void restablecerContrasena(Usuario usuarioAdmin, String correoUsuarioARestablecer, String nuevaContrasena) throws Exception {
            try {
                // 1. Validar Permiso (¡Importante!)
                Objects.requireNonNull(usuarioAdmin, "...");
                Objects.requireNonNull(usuarioAdmin.getTipoUsuario(), "...");
                if (!NOMBRE_TIPO_ADMINISTRADOR.equals(usuarioAdmin.getTipoUsuario().getTipo())) {
                    LogsError.warn(UsuarioService.class, "Intento no autorizado...");
                    throw new Exception("No tiene permisos..."); 
                }

                // 2. Validar datos de entrada ...
                Objects.requireNonNull(correoUsuarioARestablecer, "...");
                Objects.requireNonNull(nuevaContrasena, "...");
                
                // 3. Buscar usuario a restablecer
                Usuario usuarioARestablecer = usuarioDAO.obtenerPorCorreo(correoUsuarioARestablecer);
                if (usuarioARestablecer == null) {
                    throw new Exception("No se encontró usuario...");
                }

                // 4. Actualizar y guardar
                usuarioARestablecer.setContrasena(nuevaContrasena);
                usuarioDAO.actualizar(usuarioARestablecer); 
                LogsError.info(UsuarioService.class, "Contraseña restablecida...");

            } catch (SQLException e) {
                 LogsError.error(UsuarioService.class, "Error de BD...", e);
                throw new Exception("Error en BD...", e);
             } // ... (otros catch)
        }
        // ... (otros métodos: login, crearUsuario, etc.) ...
    }
    ```

## 9. Conclusión

Con estas clases (Modelos, `ConexionBD`, `LogsError`, DAOs y `UsuarioService`), se ha establecido la lógica de backend completa para la gestión de usuarios y tipos de usuario. 