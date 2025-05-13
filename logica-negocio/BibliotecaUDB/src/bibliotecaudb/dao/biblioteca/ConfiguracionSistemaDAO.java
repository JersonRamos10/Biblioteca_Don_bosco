package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.modelo.biblioteca.ConfiguracionSistema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal; // Importar BigDecimal

public class ConfiguracionSistemaDAO {

    private static final String SQL_SELECT = "SELECT id, maximo_ejemplares, mora_diaria FROM configuracion_sistema WHERE id = ?"; // Asumimos ID=1 o el único que exista
    private static final String SQL_UPDATE = "UPDATE configuracion_sistema SET maximo_ejemplares = ?, mora_diaria = ? WHERE id = ?";

    public ConfiguracionSistemaDAO() {
        // Constructor vacío
    }

    public ConfiguracionSistema obtenerConfiguracion(int idConfig) throws SQLException { // Pasar ID por si acaso
        ConfiguracionSistema configuracion = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_SELECT);
            pstmt.setInt(1, idConfig); // Usar el ID proporcionado
            rs = pstmt.executeQuery();

            if (rs.next()) {
                configuracion = mapearResultSetAConfiguracion(rs);
            } else {
                LogsError.warn(ConfiguracionSistemaDAO.class, "No se encontró configuración del sistema con ID: " + idConfig);
            }
        } catch (SQLException e) {
            LogsError.error(ConfiguracionSistemaDAO.class, "Error al obtener la configuración del sistema con ID: " + idConfig, e);
            throw e;
        } finally {
            ConexionBD.close(rs);
            ConexionBD.close(pstmt);
        }
        return configuracion;
    }

    public boolean actualizarConfiguracion(ConfiguracionSistema configuracion) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int filasAfectadas = 0;

        if (configuracion.getId() <= 0) {
            throw new SQLException("ID de configuración no válido para actualizar.");
        }
        if (configuracion.getMaximoEjemplares() == null || configuracion.getMaximoEjemplares() < 0) {
             throw new SQLException("Máximo de ejemplares no puede ser nulo o negativo.");
        }
        if (configuracion.getMoraDiaria() == null || configuracion.getMoraDiaria().compareTo(BigDecimal.ZERO) < 0) {
             throw new SQLException("Mora diaria no puede ser nula o negativa.");
        }


        try {
            conn = ConexionBD.getConexion();
            pstmt = conn.prepareStatement(SQL_UPDATE);
            pstmt.setInt(1, configuracion.getMaximoEjemplares());
            pstmt.setBigDecimal(2, configuracion.getMoraDiaria()); // Usar BigDecimal
            pstmt.setInt(3, configuracion.getId());
            
            filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                LogsError.info(ConfiguracionSistemaDAO.class, "Configuración del sistema actualizada para ID: " + configuracion.getId());
            } else {
                LogsError.warn(ConfiguracionSistemaDAO.class, "No se actualizo la configuración del sistema (ID: " + configuracion.getId() + "). ¿Existe el registro?");
            }
        } catch (SQLException e) {
            LogsError.error(ConfiguracionSistemaDAO.class, "Error al actualizar la configuracion del sistema para ID: " + configuracion.getId(), e);
            throw e;
        } finally {
            ConexionBD.close(pstmt);
        }
        return filasAfectadas > 0;
    }

    private ConfiguracionSistema mapearResultSetAConfiguracion(ResultSet rs) throws SQLException {
        ConfiguracionSistema configuracion = new ConfiguracionSistema();
        configuracion.setId(rs.getInt("id"));
        configuracion.setMaximoEjemplares(rs.getInt("maximo_ejemplares"));
        // Manejar posible NULL para maximo_ejemplares si la BD lo permite (aunque el modelo tiene Integer)
        if (rs.wasNull()) {
            configuracion.setMaximoEjemplares(null);
        }

        configuracion.setMoraDiaria(rs.getBigDecimal("mora_diaria")); // Usar BigDecimal
         if (rs.wasNull()) {
            configuracion.setMoraDiaria(null); // O un valor por defecto como BigDecimal.ZERO
        }
        return configuracion;
    }
}