package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.modelo.biblioteca.ConfiguracionSistema;
import java.sql.SQLException;

public interface ConfiguracionSistemaDAO {
    ConfiguracionSistema obtenerConfiguracion() throws SQLException; 
    boolean actualizarConfiguracion(ConfiguracionSistema config) throws SQLException;
}