package bibliotecaudb.dao.biblioteca.impl;

import bibliotecaudb.modelo.biblioteca.Ejemplar;
import bibliotecaudb.modelo.biblioteca.Documento;
import bibliotecaudb.dao.biblioteca.EjemplarDAO;
import bibliotecaudb.dao.biblioteca.DocumentoDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EjemplarDAOImpl implements EjemplarDAO {

    private static final String SQL_INSERT = "INSERT INTO ejemplares (id_documento, ubicacion, estado) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE ejemplares SET id_documento = ?, ubicacion = ?, estado = ? WHERE id = ?";
    private static final String SQL_UPDATE_ESTADO = "UPDATE ejemplares SET estado = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM ejemplares WHERE id = ?";
    private static final String SQL_SELECT_BY_ID = "SELECT id, id_documento, ubicacion, estado FROM ejemplares WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, id_documento, ubicacion, estado FROM ejemplares ORDER BY id_documento, id";
    private static final String SQL_SELECT_BY_ID_DOCUMENTO = "SELECT id, id_documento, ubicacion, estado FROM ejemplares WHERE id_documento = ? ORDER BY id";
    private static final String SQL_SELECT_DISPONIBLES_BY_ID_DOCUMENTO = "SELECT id, id_documento, ubicacion, estado FROM ejemplares WHERE id_documento = ? AND estado = ? ORDER BY id";
    private static final String SQL_COUNT_BY_ID_DOCUMENTO = "SELECT COUNT(*) FROM ejemplares WHERE id_documento = ?";
    private static final String SQL_COUNT_DISPONIBLES_BY_ID_DOCUMENTO = "SELECT COUNT(*) FROM ejemplares WHERE id_documento = ? AND estado = ?";


    private DocumentoDAO documentoDAO; // Objeto para acceder a los datos del documento

    public EjemplarDAOImpl() {
        this.documentoDAO = new DocumentoDAOImpl(); // Creamos un objeto para manejar documentos
    }

    public EjemplarDAOImpl(DocumentoDAO documentoDAO) { // Constructor por si necesitamos pasarle el manejador de documentos
        this.documentoDAO = documentoDAO;
    }


    @Override
    public boolean insertar(Ejemplar ejemplar) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        ResultSet generatedKeys = null; // Para obtener el ID generado
        int rowsAffected = 0; // Para saber cuantas filas se afectaron
        try {
            conn = ConexionBD.getConexion(); // Abrimos la conexion a la BD
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, ejemplar.getIdDocumento());
            pstmt.setString(2, ejemplar.getUbicacion());
            pstmt.setString(3, ejemplar.getEstado());

            LogsError.info(this.getClass(), "Ejecutando consulta para insertar ejemplar: " + SQL_INSERT);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la insercion
            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys(); // Obtenemos el ID
                if (generatedKeys.next()) {
                    ejemplar.setId(generatedKeys.getInt(1)); // Asignamos el ID al objeto ejemplar
                }
                LogsError.info(this.getClass(), "Ejemplar insertado con ID: " + ejemplar.getId());
            } else {
                LogsError.warn(this.getClass(), "No se inserto el Ejemplar.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar ejemplar: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(generatedKeys); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se inserto algo
    }

    @Override
    public boolean actualizar(Ejemplar ejemplar) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        int rowsAffected = 0; // Filas afectadas
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setInt(1, ejemplar.getIdDocumento());
            pstmt.setString(2, ejemplar.getUbicacion());
            pstmt.setString(3, ejemplar.getEstado());
            pstmt.setInt(4, ejemplar.getId()); // El ID del ejemplar a actualizar

            LogsError.info(this.getClass(), "Ejecutando consulta para actualizar ejemplar: " + SQL_UPDATE + " para ID: " + ejemplar.getId());
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la actualizacion
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Ejemplar actualizado. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontro Ejemplar para actualizar con ID: " + ejemplar.getId() + " o los valores eran los mismos.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar ejemplar: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se actualizo algo
    }

    @Override
    public boolean actualizarEstado(int idEjemplar, String nuevoEstado) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        int rowsAffected = 0; // Filas afectadas

        // Verificamos que el estado sea uno de los permitidos
        if (!Ejemplar.ESTADO_DISPONIBLE.equals(nuevoEstado) && !Ejemplar.ESTADO_PRESTADO.equals(nuevoEstado)) {
            String errorMsg = "Intento de actualizar a estado de ejemplar invalido: " + nuevoEstado;
            LogsError.error(this.getClass(), errorMsg);
            throw new IllegalArgumentException(errorMsg); // Lanzamos un error especifico si el estado no es valido
        }
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_UPDATE_ESTADO);
            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idEjemplar);

            LogsError.info(this.getClass(), "Ejecutando consulta para actualizar estado de ejemplar: " + SQL_UPDATE_ESTADO + " para ID ejemplar: " + idEjemplar + " a estado: " + nuevoEstado);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la actualizacion
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Estado de ejemplar actualizado. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontro Ejemplar para actualizar estado con ID: " + idEjemplar + " o el estado ya era " + nuevoEstado);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar estado de ejemplar: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se actualizo algo
    }


    @Override
    public boolean eliminar(int id) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        int rowsAffected = 0; // Filas afectadas
        try {
            conn = ConexionBD.getConexion(); // Abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_DELETE);
            pstmt.setInt(1, id); // El ID del ejemplar a eliminar

            LogsError.info(this.getClass(), "Ejecutando consulta para eliminar ejemplar: " + SQL_DELETE + " para ID: " + id);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la eliminacion
             if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Ejemplar eliminado. Filas afectadas: " + rowsAffected);
            } else {
                 LogsError.warn(this.getClass(), "No se encontro Ejemplar para eliminar con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al eliminar ejemplar: " + ex.getMessage(), ex);
            // Hay que tener cuidado si el ejemplar esta en un prestamo activo.
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se elimino algo
    }

    // Este metodo convierte los datos de un ResultSet a un objeto Ejemplar
    private Ejemplar mapearResultSet(ResultSet rs) throws SQLException {
        Ejemplar ej = new Ejemplar(); // Creamos un nuevo objeto Ejemplar
        ej.setId(rs.getInt("id"));
        ej.setIdDocumento(rs.getInt("id_documento"));
        ej.setUbicacion(rs.getString("ubicacion"));
        ej.setEstado(rs.getString("estado")); // El metodo setEstado ya valida el estado

        if (this.documentoDAO != null) { // Si tenemos el objeto para manejar documentos
            Documento doc = documentoDAO.obtenerPorId(ej.getIdDocumento()); // Buscamos el documento asociado
            ej.setDocumento(doc); // Asignamos el objeto Documento completo al ejemplar
        }
        return ej; // Devolvemos el ejemplar
    }

    @Override
    public Ejemplar obtenerPorId(int id) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para guardar los resultados
        Ejemplar ejemplar = null; // El ejemplar que devolveremos
        try {
            conn = ConexionBD.getConexion(); // Abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id); // El ID del ejemplar que buscamos
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener ejemplar por ID: " + SQL_SELECT_BY_ID + " con ID: " + id);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            if (rs.next()) { // Si hay resultado
                ejemplar = mapearResultSet(rs); // Convertimos el resultado a objeto Ejemplar
            } else {
                LogsError.warn(this.getClass(), "No se encontro Ejemplar con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener ejemplar por ID: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return ejemplar; // Devolvemos el ejemplar o null si no se encontro
    }

    @Override
    public List<Ejemplar> obtenerTodos() throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        List<Ejemplar> ejemplares = new ArrayList<>(); // Lista para todos los ejemplares
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener todos los ejemplares: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            while (rs.next()) { // Mientras haya resultados
                ejemplares.add(mapearResultSet(rs)); // Agregamos el ejemplar a la lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todos los ejemplares: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return ejemplares; // Devolvemos la lista de ejemplares
    }

    @Override
    public List<Ejemplar> obtenerPorIdDocumento(int idDocumento) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        List<Ejemplar> ejemplares = new ArrayList<>(); // Lista para los ejemplares del documento
        try {
            conn = ConexionBD.getConexion(); // Abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID_DOCUMENTO);
            pstmt.setInt(1, idDocumento); // El ID del documento para el que buscamos ejemplares
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener ejemplares por idDocumento: " + SQL_SELECT_BY_ID_DOCUMENTO + " para idDocumento: " + idDocumento);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            while (rs.next()) { // Mientras haya resultados
                ejemplares.add(mapearResultSet(rs)); // Agregamos el ejemplar a la lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener ejemplares por idDocumento: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return ejemplares; // Devolvemos la lista de ejemplares
    }

    @Override
    public List<Ejemplar> obtenerDisponiblesPorIdDocumento(int idDocumento) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        List<Ejemplar> ejemplares = new ArrayList<>(); // Lista para los ejemplares disponibles
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_DISPONIBLES_BY_ID_DOCUMENTO);
            pstmt.setInt(1, idDocumento);
            pstmt.setString(2, Ejemplar.ESTADO_DISPONIBLE); // Buscamos solo los que estan disponibles
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener ejemplares disponibles por idDocumento: " + SQL_SELECT_DISPONIBLES_BY_ID_DOCUMENTO + " para idDocumento: " + idDocumento);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            while (rs.next()) { // Mientras haya resultados
                ejemplares.add(mapearResultSet(rs)); // Agregamos el ejemplar a la lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener ejemplares disponibles por idDocumento: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return ejemplares; // Devolvemos la lista de ejemplares disponibles
    }

    // Metodo privado para contar ejemplares, sirve para no repetir codigo
    private int ejecutarConteo(String sql, int idDocumento, String estado) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        int conteo = 0; // Variable para guardar el conteo
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idDocumento);
            if (estado != null) { // Si nos pasaron un estado, lo agregamos a la consulta
                pstmt.setString(2, estado);
            }
            LogsError.info(this.getClass(), "Ejecutando consulta de conteo para idDocumento: " + idDocumento + (estado != null ? " y estado: " + estado : ""));
            rs = pstmt.executeQuery(); // Ejecutamos la consulta de conteo
            if (rs.next()) { // Si hay resultado
                conteo = rs.getInt(1); // El conteo esta en la primera columna del resultado
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al ejecutar conteo de ejemplares: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return conteo; // Devolvemos el total contado
    }

    @Override
    public int contarEjemplaresPorDocumento(int idDocumento) throws SQLException {
        // Usamos el metodo privado para contar todos los ejemplares de un documento
        return ejecutarConteo(SQL_COUNT_BY_ID_DOCUMENTO, idDocumento, null);
    }

    @Override
    public int contarEjemplaresDisponiblesPorDocumento(int idDocumento) throws SQLException {
        // Usamos el metodo privado para contar solo los ejemplares disponibles de un documento
        return ejecutarConteo(SQL_COUNT_DISPONIBLES_BY_ID_DOCUMENTO, idDocumento, Ejemplar.ESTADO_DISPONIBLE);
    }
}