package bibliotecaudb.dao.biblioteca.impl;

import bibliotecaudb.modelo.biblioteca.ConfiguracionSistema;
import bibliotecaudb.dao.biblioteca.ConfiguracionSistemaDAO;
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class ConfiguracionSistemaDAOImpl implements ConfiguracionSistemaDAO {


    private static final String SQL_SELECT = "SELECT id, maximo_ejemplares, mora_diaria FROM configuracion_sistema WHERE id = 1";
    private static final String SQL_UPDATE = "UPDATE configuracion_sistema SET maximo_ejemplares = ?, mora_diaria = ? WHERE id = 1";

    @Override
    public ConfiguracionSistema obtenerConfiguracion() throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        ResultSet rs = null; // Para los resultados
        ConfiguracionSistema config = null; // Objeto para guardar la configuracion
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_SELECT);
            LogsError.info(this.getClass(), "Ejecutando consulta para obtener configuracion: " + SQL_SELECT);
            rs = pstmt.executeQuery(); // Ejecutamos la consulta
            if (rs.next()) { // Si hay resultado
                config = new ConfiguracionSistema(); // Creamos el objeto de configuracion
                config.setId(rs.getInt("id"));

                config.setMaximoEjemplaresGlobal(rs.getObject("maximo_ejemplares", Integer.class)); // Obtenemos el maximo de ejemplares
                config.setMoraDiariaGlobal(rs.getBigDecimal("mora_diaria")); // Obtenemos la mora diaria
            } else {
                LogsError.warn(this.getClass(), "No se encontro la fila de configuracion del sistema (ID=1).");
                // Si no hay configuracion, podriamos crear una por defecto o lanzar un error
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener configuracion del sistema: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(rs); // Cerramos el ResultSet
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return config; // Devolvemos la configuracion
    }

    @Override
    public boolean actualizarConfiguracion(ConfiguracionSistema config) throws SQLException {
        Connection conn = null; // Variable para la conexion
        PreparedStatement pstmt = null; // Variable para la consulta
        int rowsAffected = 0; // Filas afectadas
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            pstmt = conn.prepareStatement(SQL_UPDATE);
            if (config.getMaximoEjemplaresGlobal() != null) {
                pstmt.setInt(1, config.getMaximoEjemplaresGlobal());
            } else {
                pstmt.setNull(1, Types.INTEGER); // Si es nulo, lo guardamos como nulo
            }
            pstmt.setBigDecimal(2, config.getMoraDiariaGlobal());


            LogsError.info(this.getClass(), "Ejecutando consulta para actualizar configuracion: " + SQL_UPDATE);
            rowsAffected = pstmt.executeUpdate(); // Ejecutamos la actualizacion
            LogsError.info(this.getClass(), "Configuracion del sistema actualizada. Filas afectadas: " + rowsAffected);
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar configuracion del sistema: " + ex.getMessage(), ex);
            throw ex; // Dejamos que el error suba
        } finally {
            ConexionBD.close(pstmt); // Cerramos el PreparedStatement
        }
        return rowsAffected > 0; // Devolvemos true si se actualizo algo
    }
}