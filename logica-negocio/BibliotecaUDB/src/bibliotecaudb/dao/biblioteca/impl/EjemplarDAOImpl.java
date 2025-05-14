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


    private DocumentoDAO documentoDAO;

    public EjemplarDAOImpl() {
        this.documentoDAO = new DocumentoDAOImpl(); // Instanciacion directa
    }
    
    public EjemplarDAOImpl(DocumentoDAO documentoDAO) { // Para pruebas o DI
        this.documentoDAO = documentoDAO;
    }


    @Override
    public boolean insertar(Ejemplar ejemplar) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, ejemplar.getIdDocumento());
            pstmt.setString(2, ejemplar.getUbicacion());
            pstmt.setString(3, ejemplar.getEstado());

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_INSERT);
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    ejemplar.setId(generatedKeys.getInt(1));
                }
                LogsError.info(this.getClass(), "Ejemplar insertado con ID: " + ejemplar.getId());
            } else {
                LogsError.warn(this.getClass(), "No se inserto el Ejemplar.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar ejemplar: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(generatedKeys);
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    @Override
    public boolean actualizar(Ejemplar ejemplar) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setInt(1, ejemplar.getIdDocumento());
            pstmt.setString(2, ejemplar.getUbicacion());
            pstmt.setString(3, ejemplar.getEstado());
            pstmt.setInt(4, ejemplar.getId()); // Condicion WHERE

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_UPDATE + " para ID: " + ejemplar.getId());
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Ejemplar actualizado. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontro Ejemplar para actualizar con ID: " + ejemplar.getId() + " o los valores eran los mismos.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar ejemplar: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }
    
    @Override
    public boolean actualizarEstado(int idEjemplar, String nuevoEstado) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        
        if (!Ejemplar.ESTADO_DISPONIBLE.equals(nuevoEstado) && !Ejemplar.ESTADO_PRESTADO.equals(nuevoEstado)) {
            String errorMsg = "Intento de actualizar a estado de ejemplar inválido: " + nuevoEstado;
            LogsError.error(this.getClass(), errorMsg);
            throw new IllegalArgumentException(errorMsg); // Excepción más específica
        }
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE_ESTADO);
            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idEjemplar);

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_UPDATE_ESTADO + " para ID ejemplar: " + idEjemplar + " a estado: " + nuevoEstado);
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Estado de ejemplar actualizado. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontró Ejemplar para actualizar estado con ID: " + idEjemplar + " o el estado ya era " + nuevoEstado);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar estado de ejemplar: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }


    @Override
    public boolean eliminar(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();//abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_DELETE);
            pstmt.setInt(1, id);

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_DELETE + " para ID: " + id);
            rowsAffected = pstmt.executeUpdate();
             if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Ejemplar eliminado. Filas afectadas: " + rowsAffected);
            } else {
                 LogsError.warn(this.getClass(), "No se encontró Ejemplar para eliminar con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al eliminar ejemplar: " + ex.getMessage(), ex);
            // Considerar si esta en un prestamo activo.
         
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    private Ejemplar mapearResultSet(ResultSet rs) throws SQLException {
        Ejemplar ej = new Ejemplar();
        ej.setId(rs.getInt("id"));
        ej.setIdDocumento(rs.getInt("id_documento"));
        ej.setUbicacion(rs.getString("ubicacion"));
        ej.setEstado(rs.getString("estado")); // El setter valida

        if (this.documentoDAO != null) {
            Documento doc = documentoDAO.obtenerPorId(ej.getIdDocumento());
            ej.setDocumento(doc);
        }
        return ej;
    }

    @Override
    public Ejemplar obtenerPorId(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Ejemplar ejemplar = null;
        try {
            conn = ConexionBD.getConexion();//abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ID + " con ID: " + id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ejemplar = mapearResultSet(rs);
            } else {
                LogsError.warn(this.getClass(), "No se encontro Ejemplar con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener ejemplar por ID: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return ejemplar;
    }

    @Override
    public List<Ejemplar> obtenerTodos() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Ejemplar> ejemplares = new ArrayList<>();
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ejemplares.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todos los ejemplares: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return ejemplares;
    }

    @Override
    public List<Ejemplar> obtenerPorIdDocumento(int idDocumento) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Ejemplar> ejemplares = new ArrayList<>();
        try {
            conn = ConexionBD.getConexion();//abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID_DOCUMENTO);
            pstmt.setInt(1, idDocumento);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ID_DOCUMENTO + " para idDocumento: " + idDocumento);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ejemplares.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener ejemplares por idDocumento: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return ejemplares;
    }

    @Override
    public List<Ejemplar> obtenerDisponiblesPorIdDocumento(int idDocumento) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Ejemplar> ejemplares = new ArrayList<>();
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_DISPONIBLES_BY_ID_DOCUMENTO);
            pstmt.setInt(1, idDocumento);
            pstmt.setString(2, Ejemplar.ESTADO_DISPONIBLE);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_DISPONIBLES_BY_ID_DOCUMENTO + " para idDocumento: " + idDocumento);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ejemplares.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener ejemplares disponibles por idDocumento: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return ejemplares;
    }
    
    private int ejecutarConteo(String sql, int idDocumento, String estado) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int conteo = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idDocumento);
            if (estado != null) {
                pstmt.setString(2, estado);
            }
            LogsError.info(this.getClass(), "Ejecutando query de conteo para idDocumento: " + idDocumento + (estado != null ? " y estado: " + estado : ""));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                conteo = rs.getInt(1); // El conteo siempre está en la primera columna
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al ejecutar conteo de ejemplares: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return conteo;
    }

    @Override
    public int contarEjemplaresPorDocumento(int idDocumento) throws SQLException {
        return ejecutarConteo(SQL_COUNT_BY_ID_DOCUMENTO, idDocumento, null);
    }

    @Override
    public int contarEjemplaresDisponiblesPorDocumento(int idDocumento) throws SQLException {
        return ejecutarConteo(SQL_COUNT_DISPONIBLES_BY_ID_DOCUMENTO, idDocumento, Ejemplar.ESTADO_DISPONIBLE);
    }
}