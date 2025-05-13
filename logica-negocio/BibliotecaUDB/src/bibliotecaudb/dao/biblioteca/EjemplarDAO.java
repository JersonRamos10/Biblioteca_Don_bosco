package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.modelo.biblioteca.Ejemplar;
import bibliotecaudb.modelo.biblioteca.Documento; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EjemplarDAO {

    private static final String SQL_SELECT_BASE = "SELECT id, id_documento, ubicacion, estado FROM ejemplares";
    private static final String SQL_INSERT = "INSERT INTO ejemplares (id_documento, ubicacion, estado) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE ejemplares SET id_documento = ?, ubicacion = ?, estado = ? WHERE id = ?";
    private static final String SQL_UPDATE_ESTADO = "UPDATE ejemplares SET estado = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM ejemplares WHERE id = ?";

    // Dependencia del DAO de Documento para obtener el objeto completo
    private DocumentoDAO documentoDAO;

    public EjemplarDAO() {
        this.documentoDAO = new DocumentoDAO(); // Instanciar el DAO dependiente
    }

    public Ejemplar obtenerPorId(int id) throws SQLException {
        String sql = SQL_SELECT_BASE + " WHERE id = ?";
        Ejemplar ejemplar = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                ejemplar = mapearResultSetAEjemplar(rs);
            } else {
                 LogsError.warn(EjemplarDAO.class, "No se encontro Ejemplar con ID: " + id);
            }
        } catch (SQLException e) {
            LogsError.error(EjemplarDAO.class, "Error al obtener Ejemplar por ID: " + id, e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return ejemplar;
    }

    public List<Ejemplar> obtenerTodos() throws SQLException {
        String sql = SQL_SELECT_BASE + " ORDER BY id_documento, id";
        List<Ejemplar> ejemplares = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ejemplares.add(mapearResultSetAEjemplar(rs));
            }
             LogsError.info(EjemplarDAO.class, "Se obtuvieron " + ejemplares.size() + " ejemplares.");
        } catch (SQLException e) {
            LogsError.error(EjemplarDAO.class, "Error al obtener todos los Ejemplares", e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return ejemplares;
    }
    
    public List<Ejemplar> obtenerPorIdDocumento(int idDocumento) throws SQLException {
        String sql = SQL_SELECT_BASE + " WHERE id_documento = ? ORDER BY id";
        List<Ejemplar> ejemplares = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idDocumento);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ejemplares.add(mapearResultSetAEjemplar(rs));
            }
             LogsError.info(EjemplarDAO.class, "Se obtuvieron " + ejemplares.size() + " ejemplares para el documento ID: " + idDocumento);
        } catch (SQLException e) {
            LogsError.error(EjemplarDAO.class, "Error al obtener ejemplares por ID de Documento: " + idDocumento, e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return ejemplares;
    }

    public List<Ejemplar> obtenerDisponiblesPorIdDocumento(int idDocumento) throws SQLException {
        String sql = SQL_SELECT_BASE + " WHERE id_documento = ? AND estado = ?";
        List<Ejemplar> ejemplares = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idDocumento);
            pstmt.setString(2, Ejemplar.EstadoEjemplar.DISPONIBLE.getDbValue()); // Usar valor del enum
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Ejemplar ej = mapearResultSetAEjemplar(rs);
                 // Doble verificación, aunque el SQL ya filtra.
                if (ej != null && ej.getEstado() == Ejemplar.EstadoEjemplar.DISPONIBLE) {
                    ejemplares.add(ej);
                }
            }
            LogsError.info(EjemplarDAO.class, "Se obtuvieron " + ejemplares.size() + " ejemplares DISPONIBLES para el documento ID: " + idDocumento);
        } catch (SQLException e) {
            LogsError.error(EjemplarDAO.class, "Error al obtener ejemplares disponibles por ID de Documento: " + idDocumento, e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return ejemplares;
    }

    public Ejemplar crear(Ejemplar ejemplar) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        // Validar que el objeto Documento y su ID no sean nulos/inválidos
        if (ejemplar.getDocumento() == null || ejemplar.getDocumento().getId() <= 0) {
            throw new SQLException("Se requiere un Documento válido (con ID) para crear un Ejemplar.");
        }
         // Validar que el estado no sea nulo
        if (ejemplar.getEstado() == null) {
            LogsError.warn(EjemplarDAO.class, "Estado de ejemplar nulo al crear, se usará DISPONIBLE por defecto.");
            ejemplar.setEstado(Ejemplar.EstadoEjemplar.DISPONIBLE); // Asignar un estado por defecto
        }


        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, ejemplar.getDocumento().getId()); // Usar el ID del objeto Documento
            pstmt.setString(2, ejemplar.getUbicacion());
            pstmt.setString(3, ejemplar.getEstado().getDbValue()); // Usar el valor de BD del enum

            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    ejemplar.setId(generatedKeys.getInt(1));
                    LogsError.info(EjemplarDAO.class, "Ejemplar creado con ID: " + ejemplar.getId() + " para Documento ID: " + ejemplar.getDocumento().getId());
                } else {
                     LogsError.error(EjemplarDAO.class, "Fallo al crear Ejemplar (no se obtuvo ID) para Documento ID: " + ejemplar.getDocumento().getId());
                     throw new SQLException("No se pudo obtener el ID generado para el nuevo ejemplar.");
                }
            } else {
                LogsError.warn(EjemplarDAO.class, "La creación de Ejemplar no afectó filas para Documento ID: " + ejemplar.getDocumento().getId());
                return null;
            }
        } catch (SQLException e) {
            LogsError.error(EjemplarDAO.class, "Error al crear Ejemplar para Documento ID: " + ejemplar.getDocumento().getId(), e);
            throw e;
        } finally {
            ConexionBD.close(generatedKeys);
            ConexionBD.close(pstmt);
        }
        return ejemplar;
    }

    public boolean actualizar(Ejemplar ejemplar) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int filasAfectadas = 0;
        
        if (ejemplar.getId() <= 0) {
            throw new SQLException("Se requiere un ID válido para actualizar un Ejemplar.");
        }
        if (ejemplar.getDocumento() == null || ejemplar.getDocumento().getId() <= 0) {
            throw new SQLException("Se requiere un Documento válido (con ID) para actualizar un Ejemplar.");
        }
        if (ejemplar.getEstado() == null) {
             throw new SQLException("El Estado no puede ser nulo para actualizar un Ejemplar.");
        }

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setInt(1, ejemplar.getDocumento().getId());
            pstmt.setString(2, ejemplar.getUbicacion());
            pstmt.setString(3, ejemplar.getEstado().getDbValue());
            pstmt.setInt(4, ejemplar.getId());
            
            filasAfectadas = pstmt.executeUpdate();
             if (filasAfectadas > 0) {
                LogsError.info(EjemplarDAO.class, "Ejemplar actualizado con ID: " + ejemplar.getId());
            } else {
                LogsError.warn(EjemplarDAO.class, "No se actualizo Ejemplar (ID no encontrado?): " + ejemplar.getId());
            }
        } catch (SQLException e) {
            LogsError.error(EjemplarDAO.class, "Error al actualizar Ejemplar con ID: " + ejemplar.getId(), e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return filasAfectadas > 0;
    }

    public boolean actualizarEstado(int idEjemplar, Ejemplar.EstadoEjemplar nuevoEstado) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean actualizado = false;

        if (nuevoEstado == null) {
            throw new SQLException("El nuevo estado no puede ser nulo.");
        }

        try {
             conn = ConexionBD.getConexion();
             pstmt = conn.prepareStatement(SQL_UPDATE_ESTADO);
             pstmt.setString(1, nuevoEstado.getDbValue());
             pstmt.setInt(2, idEjemplar);

             int affectedRows = pstmt.executeUpdate();
             if (affectedRows > 0) {
                 actualizado = true;
                 LogsError.info(EjemplarDAO.class, "Estado de Ejemplar ID=" + idEjemplar + " actualizado a: " + nuevoEstado);
             } else {
                  LogsError.warn(EjemplarDAO.class, "No se actualizó estado para ejemplar con ID: " + idEjemplar);
             }
        } catch (SQLException e){
            LogsError.error(EjemplarDAO.class, "Error SQL al actualizar estado para Ejemplar ID: " + idEjemplar, e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
         return actualizado;
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = SQL_DELETE; // Ya está definido
        Connection conn = null;
        PreparedStatement pstmt = null;
        int filasAfectadas = 0;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            filasAfectadas = pstmt.executeUpdate();
             if (filasAfectadas > 0) {
                LogsError.info(EjemplarDAO.class, "Ejemplar eliminado con ID: " + id);
            } else {
                LogsError.warn(EjemplarDAO.class, "No se eliminó Ejemplar (ID no encontrado?): " + id);
            }
        } catch (SQLException e) {
            LogsError.error(EjemplarDAO.class, "Error al eliminar Ejemplar con ID: " + id, e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return filasAfectadas > 0;
    }

    private Ejemplar mapearResultSetAEjemplar(ResultSet rs) throws SQLException {
        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setId(rs.getInt("id"));
        ejemplar.setUbicacion(rs.getString("ubicacion"));
        
        // Mapear el estado usando el Enum
        String estadoDb = rs.getString("estado");
        Ejemplar.EstadoEjemplar estadoEnum = Ejemplar.EstadoEjemplar.fromDbValue(estadoDb);
        if (estadoEnum != null) {
            ejemplar.setEstado(estadoEnum);
        } else {
            LogsError.warn(EjemplarDAO.class, "Estado de DB desconocido '" + estadoDb + "' para Ejemplar ID: " + ejemplar.getId() + ". Se usará null.");
            ejemplar.setEstado(null); // O lanzar una excepción si el estado es siempre obligatorio
        }
        
        // Obtener el Documento asociado
        int idDocumento = rs.getInt("id_documento");
        if (!rs.wasNull()) {
            Documento documento = documentoDAO.obtenerPorId(idDocumento);
            if (documento != null) {
                ejemplar.setDocumento(documento);
            } else {
                LogsError.warn(EjemplarDAO.class, "Documento no encontrado para ID: " + idDocumento + " al mapear Ejemplar ID: " + ejemplar.getId());
                ejemplar.setDocumento(null); // O lanzar una excepción
            }
        } else {
            LogsError.warn(EjemplarDAO.class, "id_documento es NULL para Ejemplar ID: " + ejemplar.getId());
            ejemplar.setDocumento(null);
        }
        return ejemplar;
    }
}