package bibliotecaudb.dao.biblioteca.impl;

import bibliotecaudb.modelo.biblioteca.PoliticasPrestamo;
import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.dao.biblioteca.PoliticasPrestamoDAO;
import bibliotecaudb.dao.usuario.TipoUsuarioDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.dao.usuario.impl.TipoUsuarioDAOImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PoliticasPrestamosDAOImpl implements PoliticasPrestamoDAO {

    private static final String SQL_INSERT = "INSERT INTO politicas_prestamo (id_tipo_usuario, max_ejemplares_prestamo, dias_prestamo_default) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE_BY_ID_TIPO_USUARIO = "UPDATE politicas_prestamo SET max_ejemplares_prestamo = ?, dias_prestamo_default = ? WHERE id_tipo_usuario = ?";
    private static final String SQL_SELECT_BY_ID_TIPO_USUARIO = "SELECT id_politica, id_tipo_usuario, max_ejemplares_prestamo, dias_prestamo_default FROM politicas_prestamo WHERE id_tipo_usuario = ?";
    private static final String SQL_SELECT_BY_ID_POLITICA = "SELECT id_politica, id_tipo_usuario, max_ejemplares_prestamo, dias_prestamo_default FROM politicas_prestamo WHERE id_politica = ?";
    private static final String SQL_SELECT_ALL = "SELECT id_politica, id_tipo_usuario, max_ejemplares_prestamo, dias_prestamo_default FROM politicas_prestamo ORDER BY id_tipo_usuario";

    private TipoUsuarioDAO tipoUsuarioDAO; // Objeto para manejar los tipos de usuario

    public PoliticasPrestamosDAOImpl() {
         // para poder cargar el objeto TipoUsuario completo cuando leemos una politica
        this.tipoUsuarioDAO = new TipoUsuarioDAOImpl();
    }

    // Constructor por pasarle el manejador de TipoUsuario 
    public PoliticasPrestamosDAOImpl(TipoUsuarioDAO tipoUsuarioDAO) {
        this.tipoUsuarioDAO = tipoUsuarioDAO;
    }

    @Override
     // Este metodo sirve para guardar una nueva politica de prestamo en la base de datos.
    public boolean insertar(PoliticasPrestamo politica) throws SQLException {
       
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        ResultSet generatedKeys = null; // Para obtener el ID que se genera al insertar
        int rowsAffected = 0; // Para saber cuantas filas se modificaron
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, politica.getIdTipoUsuario());
            pstmt.setInt(2, politica.getMaxEjemplaresPrestamo());
            pstmt.setInt(3, politica.getDiasPrestamoDefault());

            LogsError.info(this.getClass(), "Ejecutando consulta para insertar politica: " + SQL_INSERT); // Guardamos un mensaje en el log
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la insercion
            if (rowsAffected > 0) { 
                generatedKeys = pstmt.getGeneratedKeys(); // Obtenemos la llave generada
                if (generatedKeys.next()) {
                    politica.setIdPolitica(generatedKeys.getInt(1)); // Asignamos el nuevo ID a la politica
                }
                LogsError.info(this.getClass(), "Politica de prestamo insertada con ID: " + politica.getIdPolitica() + " para tipo_usuario ID: " + politica.getIdTipoUsuario());
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar politica de prestamo: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error para que otra parte del sistema lo maneje
        } finally {
            ConexionBD.close(generatedKeys); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se inserto la politica
    }

    @Override
    // Este metodo sirve para actualizar una politica de prestamo existente, buscandola por el ID del tipo de usuario.
    public boolean actualizarPorIdTipoUsuario(PoliticasPrestamo politica) throws SQLException {
        
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        int rowsAffected = 0; // Para saber cuantas filas se afectaron
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_UPDATE_BY_ID_TIPO_USUARIO);
            pstmt.setInt(1, politica.getMaxEjemplaresPrestamo());
            pstmt.setInt(2, politica.getDiasPrestamoDefault());
            pstmt.setInt(3, politica.getIdTipoUsuario()); // Este es el ID del tipo de usuario para el WHERE

            LogsError.info(this.getClass(), "Actualizando politica para tipo_usuario ID: " + politica.getIdTipoUsuario());
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la actualizacion
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "Politica de prestamo actualizada. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontro politica para actualizar para tipo_usuario ID: " + politica.getIdTipoUsuario() + " o los valores eran los mismos.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar politica de prestamo por id_tipo_usuario: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se actualizo algo
    }

    // Este metodo ayuda a convertir los datos de la base de datos (ResultSet) a un objeto PoliticasPrestamo.
    private PoliticasPrestamo mapearResultSet(ResultSet rs) throws SQLException {
        PoliticasPrestamo pp = new PoliticasPrestamo(); // Creamos un objeto vacio de PoliticasPrestamo
        pp.setIdPolitica(rs.getInt("id_politica"));
        pp.setIdTipoUsuario(rs.getInt("id_tipo_usuario"));
        pp.setMaxEjemplaresPrestamo(rs.getInt("max_ejemplares_prestamo"));
        pp.setDiasPrestamoDefault(rs.getInt("dias_prestamo_default"));

        // Cargamos el objeto TipoUsuario que esta relacionado
        if (this.tipoUsuarioDAO != null) { // Verificamos que tengamos el objeto para acceder a los tipos de usuario
            TipoUsuario tu = this.tipoUsuarioDAO.obtenerPorId(pp.getIdTipoUsuario()); // Obtenemos el tipo de usuario por su ID
            pp.setTipoUsuario(tu); // Asignamos el objeto TipoUsuario completo a la politica
        }
        return pp; // Devolvemos la politica con todos sus datos
    }

    @Override
     // Este metodo busca y devuelve una politica de prestamo usando el ID del tipo de usuario.
    public PoliticasPrestamo obtenerPorIdTipoUsuario(int idTipoUsuario) throws SQLException {
       
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para guardar el resultado de la consulta
        PoliticasPrestamo politica = null; // Variable para la politica que encontraremos
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID_TIPO_USUARIO);
            pstmt.setInt(1, idTipoUsuario); // El ID del tipo de usuario que buscamos
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener politica por ID tipo usuario: " + SQL_SELECT_BY_ID_TIPO_USUARIO + " con idTipoUsuario: " + idTipoUsuario);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            if (rs.next()) { // Si encontramos una politica
                politica = mapearResultSet(rs); // Convertimos los datos a un objeto PoliticasPrestamo
            } else {
                LogsError.warn(this.getClass(), "No se encontro politica de prestamo para idTipoUsuario: " + idTipoUsuario);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener politica por idTipoUsuario: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return politica; // Devolvemos la politica encontrada (o null si no se encontro)
    }

    @Override
     // Este metodo busca y devuelve una politica de prestamo usando el ID de la politica.
    public PoliticasPrestamo obtenerPorIdPolitica(int idPolitica) throws SQLException {
       
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para guardar el resultado
        PoliticasPrestamo politica = null; // Variable para la politica
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID_POLITICA);
            pstmt.setInt(1, idPolitica); // El ID de la politica que buscamos
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener politica por ID politica: " + SQL_SELECT_BY_ID_POLITICA + " con idPolitica: " + idPolitica);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            if (rs.next()) { // Si encontramos la politica
                politica = mapearResultSet(rs); // Convertimos los datos a objeto
            } else {
                LogsError.warn(this.getClass(), "No se encontro politica de prestamo para idPolitica: " + idPolitica);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener politica por idPolitica: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return politica; // Devolvemos la politica (o null)
    }

    @Override
    // Este metodo devuelve una lista con todas las politicas de prestamo de la base de datos.
    public List<PoliticasPrestamo> obtenerTodas() throws SQLException {
        
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        List<PoliticasPrestamo> politicas = new ArrayList<>(); // Lista para guardar las politicas
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener todas las politicas: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            while (rs.next()) { 
                politicas.add(mapearResultSet(rs)); // Agregamos la politica a la lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todas las politicas de prestamo: " + ex.getMessage(), ex);
            throw ex; 
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return politicas; // Devolvemos la lista de politicas
    }
}