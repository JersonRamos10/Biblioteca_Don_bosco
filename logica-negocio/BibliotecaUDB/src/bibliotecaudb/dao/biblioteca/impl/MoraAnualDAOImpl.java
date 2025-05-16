package bibliotecaudb.dao.biblioteca.impl;

import bibliotecaudb.modelo.biblioteca.MoraAnual;
import bibliotecaudb.dao.biblioteca.MoraAnualDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MoraAnualDAOImpl implements MoraAnualDAO {

    private static final String SQL_INSERT = "INSERT INTO mora_anual (anio, mora_diaria) VALUES (?, ?)";
    private static final String SQL_UPDATE = "UPDATE mora_anual SET mora_diaria = ? WHERE anio = ?";
    private static final String SQL_SELECT_BY_ANIO = "SELECT anio, mora_diaria FROM mora_anual WHERE anio = ?";
    private static final String SQL_SELECT_ALL = "SELECT anio, mora_diaria FROM mora_anual ORDER BY anio DESC";
    private static final String SQL_DELETE_BY_ANIO = "DELETE FROM mora_anual WHERE anio = ?";

    @Override
    public boolean insertar(MoraAnual moraAnual) throws SQLException {
        // Este metodo sirve para guardar una nueva configuracion de mora anual.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta SQL
        int rowsAffected = 0; // Para saber cuantas filas se afectaron
        try {
            conn = ConexionBD.getConexion(); // Abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_INSERT);
            pstmt.setInt(1, moraAnual.getAnio()); // El anio de la mora
            pstmt.setBigDecimal(2, moraAnual.getMoraDiaria()); // El valor de la mora diaria para ese anio

            LogsError.info(this.getClass(), "Ejecutando consulta para insertar MoraAnual: " + SQL_INSERT);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la insercion
            LogsError.info(this.getClass(), "MoraAnual insertada para el anio: " + moraAnual.getAnio() + ". Filas afectadas: " + rowsAffected);
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al insertar MoraAnual: " + ex.getMessage(), ex);
            // Podria fallar si el anio ya existe (porque es llave primaria)
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se inserto algo
    }

    @Override
    public boolean actualizar(MoraAnual moraAnual) throws SQLException {
        // Este metodo sirve para actualizar el valor de la mora diaria para un anio especifico.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        int rowsAffected = 0; // Filas afectadas
        try {
            conn = ConexionBD.getConexion(); // Abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setBigDecimal(1, moraAnual.getMoraDiaria()); // El nuevo valor de la mora
            pstmt.setInt(2, moraAnual.getAnio()); // El anio que queremos actualizar (WHERE)

            LogsError.info(this.getClass(), "Actualizando MoraAnual para el anio: " + moraAnual.getAnio());
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la actualizacion
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "MoraAnual actualizada. Filas afectadas: " + rowsAffected);
            } else {
                LogsError.warn(this.getClass(), "No se encontro MoraAnual para actualizar para el anio: " + moraAnual.getAnio() + " o el valor era el mismo.");
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar MoraAnual: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se actualizo algo
    }

    // Este metodo convierte los datos de la base de datos (ResultSet) a un objeto MoraAnual.
    private MoraAnual mapearResultSet(ResultSet rs) throws SQLException {
        MoraAnual ma = new MoraAnual(); // Creamos un objeto MoraAnual vacio
        ma.setAnio(rs.getInt("anio"));
        ma.setMoraDiaria(rs.getBigDecimal("mora_diaria"));
        return ma; // Devolvemos el objeto con sus datos
    }

    @Override
    public MoraAnual obtenerPorAnio(int anio) throws SQLException {
        // Este metodo busca y devuelve la configuracion de mora para un anio especifico.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para el resultado
        MoraAnual moraAnual = null; // Variable para la mora anual
        try {
            conn = ConexionBD.getConexion(); // Abre la conexion a la BD
            pstmt = conn.prepareStatement(SQL_SELECT_BY_ANIO);
            pstmt.setInt(1, anio); // El anio que buscamos
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener MoraAnual por anio: " + SQL_SELECT_BY_ANIO + " para anio: " + anio);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            if (rs.next()) { // Si encontramos la configuracion
                moraAnual = mapearResultSet(rs); // Convertimos los datos a objeto
            } else {
                LogsError.warn(this.getClass(), "No se encontro MoraAnual para el anio: " + anio);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener MoraAnual por anio: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return moraAnual; // Devolvemos la mora (o null)
    }

    @Override
    public List<MoraAnual> obtenerTodas() throws SQLException {
        // Este metodo devuelve una lista con todas las configuraciones de mora anual.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        List<MoraAnual> morasAnuales = new ArrayList<>(); // Lista para guardar las moras
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT_ALL);
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener todas las MoraAnual: " + SQL_SELECT_ALL);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            while (rs.next()) { // Mientras haya resultados
                morasAnuales.add(mapearResultSet(rs)); // Agregamos la mora a la lista
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener todas las MoraAnual: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return morasAnuales; // Devolvemos la lista de moras
    }

    @Override
    public boolean eliminar(int anio) throws SQLException {
        // Este metodo sirve para eliminar la configuracion de mora para un anio especifico.
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        int rowsAffected = 0; // Filas afectadas
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_DELETE_BY_ANIO);
            pstmt.setInt(1, anio); // El anio de la mora a eliminar

            LogsError.info(this.getClass(), "Ejecutando consulta para eliminar MoraAnual: " + SQL_DELETE_BY_ANIO + " para anio: " + anio);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la eliminacion
            if (rowsAffected > 0) {
                LogsError.info(this.getClass(), "MoraAnual para el anio " + anio + " eliminada. Filas afectadas: " + rowsAffected);
            } else {
                 LogsError.warn(this.getClass(), "No se encontro MoraAnual para eliminar para el anio: " + anio);
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al eliminar MoraAnual: " + ex.getMessage(), ex);
            throw ex; // Relanzamos el error
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se elimino algo
    }
}