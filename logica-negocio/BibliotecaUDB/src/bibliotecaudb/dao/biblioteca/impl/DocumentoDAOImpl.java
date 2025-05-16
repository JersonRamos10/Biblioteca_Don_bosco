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
    // Consulta para buscar documentos por diferentes terminos
    private static final String SQL_BUSCAR_POR_TERMINO_GENERAL =
        "SELECT d.id, d.titulo, d.autor, d.editorial, d.anio_publicacion, d.id_tipo_documento " +
        "FROM documentos d " +
        "WHERE d.titulo LIKE ? OR d.autor LIKE ? OR d.editorial LIKE ? OR CAST(d.anio_publicacion AS CHAR) LIKE ?";


    private TipoDocumentoDAO tipoDocumentoDAO; // Objeto para acceder a los tipos de documento

    public DocumentoDAOImpl() {
        this.tipoDocumentoDAO = new TipoDocumentoDAOImpl(); // Creamos un objeto para el tipo de documento
    }

    public DocumentoDAOImpl(TipoDocumentoDAO tipoDocumentoDAO) {
        this.tipoDocumentoDAO = tipoDocumentoDAO; // Asignamos el objeto tipoDocumentoDAO que nos pasan
    }


    @Override
    public boolean insertar(Documento documento) throws SQLException {
        Connection conn = null; // Variable para la conexion a la BD
        PreparedStatement pstmt = null; // Variable para la consulta SQL preparada
        ResultSet generatedKeys = null; // Variable para obtener las claves generadas (como el ID)
        int rowsAffected = 0; // Variable para saber cuantas filas se afectaron
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion a la base de datos
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, documento.getTitulo());
            pstmt.setString(2, documento.getAutor()); // El autor puede no venir
            pstmt.setString(3, documento.getEditorial()); // La editorial puede no venir
            if (documento.getAnioPublicacion() != null) {
                pstmt.setInt(4, documento.getAnioPublicacion());
            } else {
                pstmt.setNull(4, Types.INTEGER); // Si no hay anio, guardamos nulo
            }
            pstmt.setInt(5, documento.getIdTipoDocumento());

            LogsError.info(this.getClass(), "Ejecutando consulta para insertar: " + SQL_INSERT); // Guardamos en log la consulta
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la actualizacion
            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys(); // Obtenemos las llaves que se crearon
                if (generatedKeys.next()) {
                    documento.setId(generatedKeys.getInt(1)); // Asignamos el nuevo ID al documento
                }
                LogsError.info(this.getClass(), "Documento insertado con ID: " + documento.getId()); // Mensaje de exito en el log
            } else {
                 LogsError.warn(this.getClass(), "No se inserto el Documento."); 
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar documento: " + ex.getMessage(), ex); // Mensaje de error en el log
            throw ex; 
        } finally {
            ConexionBD.close(generatedKeys); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
            // La conexion la cerramos en otro lado, usualmente al final de la operacion completa
        }
        return rowsAffected > 0; // Devolvemos true si se afecto alguna fila, false si no
    }

    @Override
    public boolean actualizar(Documento documento) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta preparada
        int rowsAffected = 0; // Filas afectadas por la consulta
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setString(1, documento.getTitulo());
            pstmt.setString(2, documento.getAutor());
            pstmt.setString(3, documento.getEditorial());
            if (documento.getAnioPublicacion() != null) {
                pstmt.setInt(4, documento.getAnioPublicacion());
            } else {
                pstmt.setNull(4, Types.INTEGER); // Si el anio es nulo, lo ponemos como nulo en la BD
            }
            pstmt.setInt(5, documento.getIdTipoDocumento());
            pstmt.setInt(6, documento.getId()); // Este es el ID del documento que queremos actualizar

            LogsError.info(this.getClass(), "Ejecutando consulta para actualizar: " + SQL_UPDATE + " para ID: " + documento.getId());
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la actualizacion
             if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Documento actualizado. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontro Documento para actualizar con ID: " + documento.getId() + " o los valores son los mismos.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar documento: " + ex.getMessage(), ex);
            throw ex; // Relanzamos la excepcion
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Retornamos true si se actualizo algo
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta preparada
        int rowsAffected = 0; // Filas afectadas
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_DELETE);
            pstmt.setInt(1, id); // El ID del documento a eliminar

            LogsError.info(this.getClass(), "Ejecutando consulta para eliminar: " + SQL_DELETE + " para ID: " + id);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la eliminacion
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Documento eliminado. Filas afectadas: " + rowsAffected);
            } else {
                 LogsError.warn(this.getClass(), "No se encontro Documento para eliminar con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al eliminar documento: " + ex.getMessage(), ex);
            // La base de datos deberia encargarse de borrar en cascada los ejemplares.
            throw ex; // Relanzamos la excepcion
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Retornamos true si se elimino algo
    }

    // Este metodo ayuda a convertir los datos del ResultSet a un objeto Documento
    private Documento mapearResultSet(ResultSet rs) throws SQLException {
        Documento doc = new Documento(); // Creamos un nuevo objeto Documento
        doc.setId(rs.getInt("id"));
        doc.setTitulo(rs.getString("titulo"));
        doc.setAutor(rs.getString("autor"));
        doc.setEditorial(rs.getString("editorial"));

        int anioPub = rs.getInt("anio_publicacion"); // Obtenemos el anio
        if (rs.wasNull()) { // Verificamos si el anio era nulo en la BD
            doc.setAnioPublicacion(null);
        } else {
            doc.setAnioPublicacion(anioPub);
        }

        doc.setIdTipoDocumento(rs.getInt("id_tipo_documento"));

        if (this.tipoDocumentoDAO != null) { // Si tenemos el objeto para acceder a tipos de documento
            TipoDocumento tipoDoc = tipoDocumentoDAO.obtenerPorId(doc.getIdTipoDocumento()); // Buscamos el tipo de documento
            doc.setTipoDocumento(tipoDoc); // Asignamos el objeto TipoDocumento completo
        }
        return doc; // Devolvemos el documento con sus datos
    }

    @Override
    public Documento obtenerPorId(int id) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta preparada
        ResultSet rs = null; // Variable para guardar los resultados
        Documento documento = null; // Variable para el documento que vamos a devolver
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id); // El ID del documento que buscamos
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener por ID: " + SQL_SELECT_BY_ID + " con ID: " + id);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            if (rs.next()) { // Si encontramos un resultado
                documento = mapearResultSet(rs); // Convertimos el resultado a un objeto Documento
            } else {
                LogsError.warn(this.getClass(), "No se encontro Documento con ID: " + id);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener documento por ID: " + ex.getMessage(), ex);
            throw ex; // Relanzamos la excepcion
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return documento; // Devolvemos el documento encontrado o null si no se encontro
    }

    @Override
    public List<Documento> obtenerTodos() throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta 
        ResultSet rs = null; // Variable para los resultados
        List<Documento> documentos = new ArrayList<>(); // Lista para guardar todos los documentos
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener todos los documentos: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            while (rs.next()) { // Mientras haya resultados
                documentos.add(mapearResultSet(rs)); // Agregamos el documento a la lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todos los documentos: " + ex.getMessage(), ex);
            throw ex; // Relanzamos la excepcion
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return documentos; // Devolvemos la lista de documentos
    }

    @Override
    public List<Documento> buscarPorTerminoGeneral(String termino) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta preparada
        ResultSet rs = null; // Variable para los resultados
        List<Documento> documentos = new ArrayList<>(); // Lista para los documentos encontrados
        String terminoLike = "%" + termino.toLowerCase() + "%";
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion

            pstmt = conn.prepareStatement(SQL_BUSCAR_POR_TERMINO_GENERAL);
            pstmt.setString(1, terminoLike); // Para el titulo
            pstmt.setString(2, terminoLike); // Para el autor
            pstmt.setString(3, terminoLike); // Para la editorial
            pstmt.setString(4, terminoLike); // Para el anio de publicacion

            LogsError.info(this.getClass(), "Ejecutando busqueda general de documentos con termino: " + termino);
            rs = pstmt.executeQuery(); // Ejecutamos la busqueda
            while (rs.next()) { // Mientras haya resultados
                documentos.add(mapearResultSet(rs)); // Agregamos el documento a la lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al buscar documentos por termino general: " + ex.getMessage(), ex);
            throw ex; // Relanzamos la excepcion
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return documentos; // Devolvemos la lista de documentos encontrados
    }
}