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
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ConfiguracionSistema config = null;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT);
            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_SELECT);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                config = new ConfiguracionSistema();
                config.setId(rs.getInt("id"));
           
                config.setMaximoEjemplaresGlobal(rs.getObject("maximo_ejemplares", Integer.class));
                config.setMoraDiariaGlobal(rs.getBigDecimal("mora_diaria"));
            } else {
                LogsError.warn(this.getClass(), "No se encontro la fila de configuracion del sistema (ID=1).");
              
            }
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al obtener configuración del sistema: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return config;
    }

    @Override
    public boolean actualizarConfiguracion(ConfiguracionSistema config) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = 0;
        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);
            if (config.getMaximoEjemplaresGlobal() != null) {
                pstmt.setInt(1, config.getMaximoEjemplaresGlobal());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setBigDecimal(2, config.getMoraDiariaGlobal());
      

            LogsError.info(this.getClass(), "Ejecutando query: " + SQL_UPDATE);
            rowsAffected = pstmt.executeUpdate();
            LogsError.info(this.getClass(), "Configuracion del sistema actualizada. Filas afectadas: " + rowsAffected);
        } catch (SQLException ex) {
            LogsError.error(this.getClass(), "Error al actualizar configuracion del sistema: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            ConexionBD.close(pstmt);
        }
        return rowsAffected > 0;
    }
}