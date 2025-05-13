package bibliotecaudb.dao.usuario; 

import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.conexion.ConexionBD; 
import bibliotecaudb.conexion.LogsError; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del DAO para la entidad TipoUsuario.
 * Acceder directamente a la base de datos usando JDBC.
 */
public class TipoUsuarioDAO{

    private static final String SQL_SELECT_BY_ID = "SELECT id, tipo FROM tipo_usuario WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, tipo FROM tipo_usuario ORDER BY id";

    //metodo para obtener el id del tipo de usario
    public TipoUsuario obtenerPorId(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        TipoUsuario tipoUsuario = null;

        try {
            // Obtener la conexión 
            conn = ConexionBD.getConexion(); // Obtiene la conexion estatica

            // Prepara la sentencia SQL
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ID);
            pstmt.setInt(1, id);

            // Ejecuta la consulta
            rs = pstmt.executeQuery();

            // Procesar el resultado
            if (rs.next()) {
                tipoUsuario = new TipoUsuario(rs.getInt("id"), rs.getString("tipo"));
            }

        } finally {
            // Cerrar recursos usando los metodos helper de ConexionBD
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
           
        }

        return tipoUsuario;
    }

    //metodo para listar los tipos de usuario
    public List<TipoUsuario> listarTodos() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TipoUsuario> tiposUsuario = new ArrayList<>();

        try {
            // Obteniendo la conexión
            conn = ConexionBD.getConexion();

            // Prepara la sentencia SQL
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);

            // Ejecuta la consulta
            rs = pstmt.executeQuery();

            // Procesar los resultados
            while (rs.next()) {
                TipoUsuario tipoUsuario = new TipoUsuario(rs.getInt("id"), rs.getString("tipo"));
                tiposUsuario.add(tipoUsuario);
            }

        } finally {
            // 5. Cerrar recursos usando los metodos helper de ConexionBD
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
           
        }

        return tiposUsuario;
    }
}