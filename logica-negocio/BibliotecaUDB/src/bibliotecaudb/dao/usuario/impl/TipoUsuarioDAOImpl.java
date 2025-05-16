package bibliotecaudb.dao.usuario.impl;

import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.dao.usuario.TipoUsuarioDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoUsuarioDAOImpl implements TipoUsuarioDAO {

    private static final String SQL_SELECT_BY_ID = "SELECT id, tipo FROM tipo_usuario WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, tipo FROM tipo_usuario ORDER BY id";
    private static final String SQL_SELECT_BY_NAME = "SELECT id, tipo FROM tipo_usuario WHERE tipo = ?";

    @Override
    public TipoUsuario obtenerPorId(int id) throws SQLException {
        // Este metodo sirve para obtener un tipo de usuario especifico usando su ID.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        ResultSet rs = null; // Para guardar el resultado de la consulta
        TipoUsuario tipoUsuario = null; // Variable para el tipo de usuario que encontraremos

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion a la base de datos
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID); // Preparamos la consulta
            pstmt.setInt(1, id); // Establecemos el ID que estamos buscando
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener tipo usuario por ID: " + SQL_SELECT_BY_ID + " con ID: " + id); // Mensaje para el log
            rs = pstmt.executeQuery(); // Ejecutamos la consulta y guardamos el resultado

            if (rs.next()) { // Si encontramos un resultado
                tipoUsuario = new TipoUsuario(); // Creamos un nuevo objeto TipoUsuario
                tipoUsuario.setId(rs.getInt("id")); // Le ponemos el ID del resultado
                tipoUsuario.setTipo(rs.getString("tipo")); // Le ponemos el nombre del tipo del resultado
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener tipo de usuario por ID: " + ex.getMessage(), ex); // Mensaje de error para el log
            throw ex; // Relanzamos la excepcion para que la maneje quien llamo al metodo
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet para liberar recursos
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
            // La conexion usualmente se cierra mas arriba, cuando termina toda la operacion.
        }
        return tipoUsuario; // Devolvemos el tipo de usuario encontrado (o null si no se encontro)
    }

    @Override
    public List<TipoUsuario> obtenerTodos() throws SQLException {
        // Este metodo sirve para obtener una lista de todos los tipos de usuario de la base de datos.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        ResultSet rs = null; // Para guardar los resultados
        List<TipoUsuario> tiposUsuario = new ArrayList<>(); // Creamos una lista vacia para guardar los tipos de usuario

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_ALL); // Preparamos la consulta para obtener todos
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener todos los tipos de usuario: " + SQL_SELECT_ALL); // Mensaje para el log
            rs = pstmt.executeQuery(); // Ejecutamos la consulta

            while (rs.next()) { // Mientras haya mas resultados en el ResultSet
                TipoUsuario tipoUsuario = new TipoUsuario(); // Creamos un objeto TipoUsuario
                tipoUsuario.setId(rs.getInt("id")); // Le ponemos el ID
                tipoUsuario.setTipo(rs.getString("tipo")); // Le ponemos el nombre del tipo
                tiposUsuario.add(tipoUsuario); // Agregamos el tipo de usuario a nuestra lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todos los tipos de usuario: " + ex.getMessage(), ex); // Mensaje de error para el log
            throw ex; // Relanzamos la excepcion
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return tiposUsuario; // Devolvemos la lista con todos los tipos de usuario
    }

    @Override
    public TipoUsuario obtenerPorNombre(String nombreTipo) throws SQLException {
        // Este metodo sirve para obtener un tipo de usuario especifico usando su nombre.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        ResultSet rs = null; // Para guardar el resultado
        TipoUsuario tipoUsuario = null; // Variable para el tipo de usuario

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_BY_NAME); // Preparamos la consulta
            pstmt.setString(1, nombreTipo); // Establecemos el nombre del tipo que buscamos
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener tipo usuario por nombre: " + SQL_SELECT_BY_NAME + " con nombre: " + nombreTipo); // Mensaje para el log
            rs = pstmt.executeQuery(); // Ejecutamos la consulta

            if (rs.next()) { // Si encontramos un resultado
                tipoUsuario = new TipoUsuario(); // Creamos el objeto
                tipoUsuario.setId(rs.getInt("id")); // Le ponemos el ID
                tipoUsuario.setTipo(rs.getString("tipo")); // Le ponemos el nombre del tipo
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener tipo de usuario por nombre: " + ex.getMessage(), ex); // Mensaje de error para el log
            throw ex; // Relanzamos la excepcion
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return tipoUsuario; // Devolvemos el tipo de usuario encontrado (o null)
    }
}