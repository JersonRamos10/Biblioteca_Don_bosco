package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.modelo.biblioteca.Documento;
import bibliotecaudb.modelo.biblioteca.TipoDocumento; // Necesario para el modelo

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DocumentoDAO {

    // SQL base para seleccionar documentos.
    private static final String SQL_SELECT_BASE = "SELECT d.id, d.titulo, d.autor, d.editorial, d.anio_publicacion, d.id_tipo_documento FROM documentos d";
    private static final String SQL_INSERT = "INSERT INTO documentos (titulo, autor, editorial, anio_publicacion, id_tipo_documento) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE documentos SET titulo = ?, autor = ?, editorial = ?, anio_publicacion = ?, id_tipo_documento = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM documentos WHERE id = ?";

    // Dependencia del DAO de TipoDocumento para obtener el objeto completo
    private TipoDocumentoDAO tipoDocumentoDAO;

    public DocumentoDAO() {
        this.tipoDocumentoDAO = new TipoDocumentoDAO(); // Instanciar el DAO dependiente
    }

    public Documento obtenerPorId(int id) throws SQLException {
        Documento documento = null;
        String sql = SQL_SELECT_BASE + " WHERE d.id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                documento = mapearResultSetADocumento(rs);
            } else {
                LogsError.warn(DocumentoDAO.class, "No se encontro Documento con ID: " + id);
            }
        } catch (SQLException e) {
            LogsError.error(DocumentoDAO.class, "Error al obtener documento por ID: " + id, e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return documento;
    }

    public List<Documento> obtenerTodos() throws SQLException {
        List<Documento> documentos = new ArrayList<>();
        String sql = SQL_SELECT_BASE + " ORDER BY d.titulo";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                documentos.add(mapearResultSetADocumento(rs));
            }
            LogsError.info(DocumentoDAO.class, "Se obtuvieron " + documentos.size() + " documentos.");
        } catch (SQLException e) {
            LogsError.error(DocumentoDAO.class, "Error al obtener todos los documentos", e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return documentos;
    }

    
    public List<Documento> buscarPorTituloOAutor(String criterio) throws SQLException {
        List<Documento> documentos = new ArrayList<>();
        String sql = SQL_SELECT_BASE +
                     " WHERE LOWER(d.titulo) LIKE LOWER(?) OR LOWER(d.autor) LIKE LOWER(?) " +
                     " ORDER BY d.titulo";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String criterioLike = "%" + criterio + "%";

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, criterioLike);
            pstmt.setString(2, criterioLike);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                documentos.add(mapearResultSetADocumento(rs));
            }
            LogsError.info(DocumentoDAO.class, "Busqueda por '" + criterio + "' encontro " + documentos.size() + " documentos.");
        } catch (SQLException e) {
            LogsError.error(DocumentoDAO.class, "Error al buscar documentos por criterio: " + criterio, e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return documentos;
    }


    public Documento crearDocumento(Documento documento) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        if (documento.getTipoDocumento() == null || documento.getTipoDocumento().getId() <= 0) {
             String errorMsg = "Intento de crear documento sin TipoDocumento valido: " + documento.getTitulo();
             LogsError.error(DocumentoDAO.class, errorMsg);
             throw new SQLException("Tipo de documento no especificado o invalido.");
        }

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, documento.getTitulo());
            pstmt.setString(2, documento.getAutor());
            pstmt.setString(3, documento.getEditorial());

            // Manejar posible NULL para año de publicación
            if (documento.getAnioPublicacion() != null) { // Usar el getter del modelo que devuelve Integer
                pstmt.setInt(4, documento.getAnioPublicacion());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, documento.getTipoDocumento().getId());

            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    documento.setId(generatedKeys.getInt(1)); // Actualizar el ID en el objeto original
                    LogsError.info(DocumentoDAO.class, "Documento creado con ID: " + documento.getId() + ", Título: " + documento.getTitulo());
                    // Devolvemos el objeto 'documento' que ya tiene el ID asignado
                } else {
                    throw new SQLException("Fallo al crear documento, no se obtuvo ID generado.");
                }
            } else {
                 LogsError.warn(DocumentoDAO.class, "La creación del documento no afectó filas: " + documento.getTitulo());
                 return null; // O lanzar excepción
            }
        } catch (SQLException e) {
            LogsError.error(DocumentoDAO.class, "Error SQL al crear documento: " + documento.getTitulo(), e);
            throw e;
        } finally {
            ConexionBD.close(generatedKeys);
            ConexionBD.close(pstmt);
        }
        return documento; // Devuelve el objeto con su ID
    }

    public boolean actualizarDocumento(Documento documento) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean exito = false;

        if (documento.getId() <= 0) {
             throw new SQLException("No se puede actualizar un documento sin un ID válido.");
        }
        if (documento.getTipoDocumento() == null || documento.getTipoDocumento().getId() <= 0) {
            String errorMsg = "Intento de actualizar documento ID " + documento.getId() + " sin TipoDocumento válido.";
            LogsError.error(DocumentoDAO.class, errorMsg);
            throw new SQLException("Tipo de documento no especificado o inválido para actualizar.");
        }

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);

            pstmt.setString(1, documento.getTitulo());
            pstmt.setString(2, documento.getAutor());
            pstmt.setString(3, documento.getEditorial());
            if (documento.getAnioPublicacion() != null) {
                pstmt.setInt(4, documento.getAnioPublicacion());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, documento.getTipoDocumento().getId());
            pstmt.setInt(6, documento.getId()); // Cláusula WHERE

            int filasAfectadas = pstmt.executeUpdate();
            exito = filasAfectadas > 0;

            if (exito) {
                LogsError.info(DocumentoDAO.class, "Documento actualizado con ID: " + documento.getId());
            } else {
                LogsError.warn(DocumentoDAO.class, "No se actualizo ningun documento con ID: " + documento.getId());
            }
        } catch (SQLException e) {
            LogsError.error(DocumentoDAO.class, "Error SQL al actualizar documento ID: " + documento.getId(), e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return exito;
    }
    
    public boolean eliminar(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean eliminado = false;
        String sql = SQL_DELETE; 

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                eliminado = true;
                LogsError.info(DocumentoDAO.class, "Documento eliminado: ID=" + id);
            } else {
                LogsError.warn(DocumentoDAO.class, "No se elimino ningún documento con ID: " + id);
            }
        } catch (SQLException e) {
            LogsError.error(DocumentoDAO.class, "Error SQL al eliminar documento ID: " + id, e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return eliminado;
    }


    // --- Metodo Helper Privado ---
    private Documento mapearResultSetADocumento(ResultSet rs) throws SQLException {
        Documento documento = new Documento();
        documento.setId(rs.getInt("id"));
        documento.setTitulo(rs.getString("titulo"));
        documento.setAutor(rs.getString("autor"));
        documento.setEditorial(rs.getString("editorial"));
        
        // Obtener año publicación, puede ser NULL
        int anio = rs.getInt("anio_publicacion");
        if (!rs.wasNull()) { // Verifica si el último getInt fue NULL
            documento.setAnioPublicacion(anio);
        } else {
            documento.setAnioPublicacion(null);
        }

        // Obtener el TipoDocumento completo usando TipoDocumentoDAO
        int idTipoDocumento = rs.getInt("id_tipo_documento");
        if (!rs.wasNull()) { // Solo intenta buscar si el ID no es null
            TipoDocumento tipo = tipoDocumentoDAO.obtenerPorId(idTipoDocumento);
            if (tipo != null) {
                documento.setTipoDocumento(tipo);
            } else {
                LogsError.warn(DocumentoDAO.class, "TipoDocumento no encontrado para ID: " + idTipoDocumento + " al mapear Documento ID: " + documento.getId());
                documento.setTipoDocumento(null);
            }
        } else {
            LogsError.warn(DocumentoDAO.class, "id_tipo_documento es NULL para Documento ID: " + documento.getId());
            documento.setTipoDocumento(null);
        }
        return documento;
    }
}