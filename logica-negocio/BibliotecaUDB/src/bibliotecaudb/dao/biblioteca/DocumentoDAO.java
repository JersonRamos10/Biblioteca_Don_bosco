/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.dao.biblioteca;

//import de los paquetes usados 
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.modelo.biblioteca.Documento;
import bibliotecaudb.modelo.biblioteca.TipoDocumento;
/**
 *
 * @author jerson_ramos
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types; // Necesario para setNull
import java.util.ArrayList;
import java.util.List;


// DAO para gestionar operaciones CRUD de la entidad Documento
public class DocumentoDAO {
     // SQL base para seleccionar documentos con su tipo
    private static final String SELECT_DOCUMENTO_SQL =
        "SELECT d.id, d.titulo, d.autor, d.editorial, d.anio_publicacion, " +
        "td.id AS id_tipo_documento, td.tipo AS nombre_tipo_documento " +
        "FROM documentos d " +
        "INNER JOIN tipo_documento td ON d.id_tipo_documento = td.id ";

    // Obtiene un documento por su ID
    public Documento obtenerPorId(int id) throws SQLException {
        Documento documento = null;
        String sql = SELECT_DOCUMENTO_SQL + " WHERE d.id = ?";
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
                LogsError.warn(DocumentoDAO.class, "No se encontró Documento con ID: " + id);
            }
        } catch (SQLException e) {
            LogsError.error(DocumentoDAO.class, "Error al obtener documento por ID: " + id, e);
            throw e;
        } finally {
            cerrarRecursos(rs, pstmt);
        }
        return documento;
    }

    // Obtiene todos los documentos de la base de datos, ordenados por título
    public List<Documento> obtenerTodos() throws SQLException {
        List<Documento> documentos = new ArrayList<>();
        String sql = SELECT_DOCUMENTO_SQL + " ORDER BY d.titulo";
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
            cerrarRecursos(rs, pstmt);
        }
        return documentos;
    }

    // Busca documentos cuyo título o autor contengan el criterio de búsqueda (case-insensitive)
    public List<Documento> buscarPorTituloOAutor(String criterio) throws SQLException {
        List<Documento> documentos = new ArrayList<>();
        String sql = SELECT_DOCUMENTO_SQL +
                     " WHERE LOWER(d.titulo) LIKE LOWER(?) OR LOWER(d.autor) LIKE LOWER(?) " +
                     " ORDER BY d.titulo";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String criterioLike = "%" + criterio + "%"; // Añadir comodines para LIKE

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, criterioLike);
            pstmt.setString(2, criterioLike);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                documentos.add(mapearResultSetADocumento(rs));
            }
            LogsError.info(DocumentoDAO.class, "Búsqueda por '" + criterio + "' encontró " + documentos.size() + " documentos.");
        } catch (SQLException e) {
            LogsError.error(DocumentoDAO.class, "Error al buscar documentos por criterio: " + criterio, e);
            throw e;
        } finally {
            cerrarRecursos(rs, pstmt);
        }
        return documentos;
    }

    // Crea un nuevo documento en la base de datos
    public Documento crearDocumento(Documento documento) throws SQLException {
        String sql = "INSERT INTO documentos (titulo, autor, editorial, anio_publicacion, id_tipo_documento) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        if (documento.getTipoDocumento() == null || documento.getTipoDocumento().getId() <= 0) {
             String errorMsg = "Intento de crear documento sin TipoDocumento válido: " + documento.getTitulo();
             LogsError.error(DocumentoDAO.class, errorMsg);
             throw new SQLException("Tipo de documento no especificado o inválido.");
        }

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, documento.getTitulo());
            pstmt.setString(2, documento.getAutor());
            pstmt.setString(3, documento.getEditorial());
            if (documento.getAnioPublicacion() > 0) {
                pstmt.setInt(4, documento.getAnioPublicacion());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, documento.getTipoDocumento().getId());

            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        documento.setId(generatedKeys.getInt(1));
                        LogsError.info(DocumentoDAO.class, "Documento creado con ID: " + documento.getId() + ", Título: " + documento.getTitulo());
                        return documento;
                    } else {
                        throw new SQLException("Fallo al crear documento, no se obtuvo ID generado.");
                    }
                }
            }
        } catch (SQLException e) {
            LogsError.error(DocumentoDAO.class, "Error SQL al crear documento: " + documento.getTitulo(), e);
            throw e;
        } finally {
            cerrarRecursos(null, pstmt);
        }
        return null;
    }

    // Actualiza los datos de un documento existente en la base de datos
    public boolean actualizarDocumento(Documento documento) throws SQLException {
        String sql = "UPDATE documentos SET titulo = ?, autor = ?, editorial = ?, anio_publicacion = ?, id_tipo_documento = ? WHERE id = ?";
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
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, documento.getTitulo());
            pstmt.setString(2, documento.getAutor());
            pstmt.setString(3, documento.getEditorial());
            if (documento.getAnioPublicacion() > 0) {
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
                LogsError.warn(DocumentoDAO.class, "No se actualizó ningún documento con ID: " + documento.getId());
            }
        } catch (SQLException e) {
            LogsError.error(DocumentoDAO.class, "Error SQL al actualizar documento ID: " + documento.getId(), e);
            throw e;
        } finally {
            cerrarRecursos(null, pstmt);
        }
        return exito;
    }

    // --- Métodos Helper Privados ---

    // Mapea una fila del ResultSet a un objeto Documento
    private Documento mapearResultSetADocumento(ResultSet rs) throws SQLException {
        Documento documento = new Documento();
        documento.setId(rs.getInt("id"));
        documento.setTitulo(rs.getString("titulo"));
        documento.setAutor(rs.getString("autor"));
        documento.setEditorial(rs.getString("editorial"));
        int anio = rs.getInt("anio_publicacion");
        if (!rs.wasNull()) {
            documento.setAnioPublicacion(anio);
        }

        TipoDocumento tipo = new TipoDocumento();
        tipo.setId(rs.getInt("id_tipo_documento"));
        tipo.setTipo(rs.getString("nombre_tipo_documento"));
        documento.setTipoDocumento(tipo);

        return documento;
    }

     // Cierra ResultSet y PreparedStatement de forma segura
    private void cerrarRecursos(ResultSet rs, PreparedStatement pstmt) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            LogsError.warn(DocumentoDAO.class, "Error al cerrar ResultSet", e);
        }
        try {
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            LogsError.warn(DocumentoDAO.class, "Error al cerrar PreparedStatement", e);
        }
         // No cerramos la Connection aquí
    }

} // Fin de la clase DocumentoDAO
