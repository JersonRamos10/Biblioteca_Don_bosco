package bibliotecaudb.dao.biblioteca.impl;

import bibliotecaudb.modelo.biblioteca.Documento;
import bibliotecaudb.modelo.biblioteca.TipoDocumento;
import bibliotecaudb.dao.biblioteca.DocumentoDAO;
import bibliotecaudb.dao.biblioteca.TipoDocumentoDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types; 
import java.util.ArrayList;
import java.util.List;

public class DocumentoDAOImpl implements DocumentoDAO {

    private static final String SQL_INSERT = "INSERT INTO documentos (titulo, autor, editorial, anio_publicacion, id_tipo_documento) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE documentos SET titulo = ?, autor = ?, editorial = ?, anio_publicacion = ?, id_tipo_documento = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM documentos WHERE id = ?";
    private static final String SQL_SELECT_BY_ID = "SELECT id, titulo, autor, editorial, anio_publicacion, id_tipo_documento FROM documentos WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, titulo, autor, editorial, anio_publicacion, id_tipo_documento FROM documentos ORDER BY titulo";
    // Consulta de búsqueda general, ajustada de ConsultasComunes.sql
    private static final String SQL_BUSCAR_POR_TERMINO_GENERAL = 
        "SELECT d.id, d.titulo, d.autor, d.editorial, d.anio_publicacion, d.id_tipo_documento " +
        "FROM documentos d " +
        "WHERE d.titulo LIKE ? OR d.autor LIKE ? OR d.editorial LIKE ? OR CAST(d.anio_publicacion AS CHAR) LIKE ?";


    private TipoDocumentoDAO tipoDocumentoDAO;

    public DocumentoDAOImpl() {
        this.tipoDocumentoDAO = new TipoDocumentoDAOImpl(); // Instanciación directa
    }
    
    public DocumentoDAOImpl(TipoDocumentoDAO tipoDocumentoDAO) { 
        this.tipoDocumentoDAO = tipoDocumentoDAO;
    }


    @Override
    public boolean insertar(Documento documento) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, documento.getTitulo());
            pstmt.setString(2, documento.getAutor()); // Puede ser null
            pstmt.setString(3, documento.getEditorial()); // Puede ser null
            if (documento.getAnioPublicacion() != null) {
                pstmt.setInt(4, documento.getAnioPublicacion());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, documento.getIdTipoDocumento());

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_INSERT);
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    documento.setId(generatedKeys.getInt(1));
                }
                LogsError.info(this.getClass(), "Documento insertado con ID: " + documento.getId());
            } else {
                 LogsError.warn(this.getClass(), "No se insertó el Documento.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar documento: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(generatedKeys);
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    @Override
    public boolean actualizar(Documento documento) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
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
            pstmt.setInt(5, documento.getIdTipoDocumento());
            pstmt.setInt(6, documento.getId()); // Condición WHERE

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_UPDATE + " para ID: " + documento.getId());
            rowsAffected = pstmt.executeUpdate();
             if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Documento actualizado. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontro Documento para actualizar con ID: " + documento.getId() + " o los valores son los mismos.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar documento: " + ex.getMessage(), ex);
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
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_DELETE);
            pstmt.setInt(1, id);

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_DELETE + " para ID: " + id);
            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Documento eliminado. Filas afectadas: " + rowsAffected);
            } else {
                 LogsError.warn(this.getClass(), "No se encontró Documento para eliminar con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al eliminar documento: " + ex.getMessage(), ex);
            // La BD tiene ON DELETE CASCADE para ejemplares referenciando este documento.
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }

    private Documento mapearResultSet(ResultSet rs) throws SQLException {
        Documento doc = new Documento();
        doc.setId(rs.getInt("id"));
        doc.setTitulo(rs.getString("titulo"));
        doc.setAutor(rs.getString("autor"));
        doc.setEditorial(rs.getString("editorial"));
        
        int anioPub = rs.getInt("anio_publicacion");
        if (rs.wasNull()) { 
            doc.setAnioPublicacion(null);
        } else {
            doc.setAnioPublicacion(anioPub);
        }
        
        doc.setIdTipoDocumento(rs.getInt("id_tipo_documento"));

        if (this.tipoDocumentoDAO != null) {
            TipoDocumento tipoDoc = tipoDocumentoDAO.obtenerPorId(doc.getIdTipoDocumento());
            doc.setTipoDocumento(tipoDoc);
        }
        return doc;
    }

    @Override
    public Documento obtenerPorId(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Documento documento = null;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_BY_ID + " con ID: " + id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                documento = mapearResultSet(rs);
            } else {
                LogsError.warn(this.getClass(), "No se encontro Documento con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener documento por ID: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return documento;
    }

    @Override
    public List<Documento> obtenerTodos() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Documento> documentos = new ArrayList<>();
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                documentos.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todos los documentos: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return documentos;
    }

    @Override
    public List<Documento> buscarPorTerminoGeneral(String termino) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Documento> documentos = new ArrayList<>();
        String terminoLike = "%" + termino.toLowerCase() + "%"; // Convertir a minusculas para busqueda insensible a mayusculas/minusculas
        try {
            conn = ConexionBD.getConexion();
         
            pstmt = conn.prepareStatement(SQL_BUSCAR_POR_TERMINO_GENERAL);
            pstmt.setString(1, terminoLike); 
            pstmt.setString(2, terminoLike); 
            pstmt.setString(3, terminoLike);
            pstmt.setString(4, terminoLike); // Para año de publicación

            LogsError.info(this.getClass(), "Ejecutando busqueda general de documentos con termino: " + termino);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                documentos.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al buscar documentos por termino general: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return documentos;
    }
}