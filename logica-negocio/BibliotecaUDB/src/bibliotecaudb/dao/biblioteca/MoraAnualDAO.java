package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.modelo.biblioteca.MoraAnual;
import java.sql.SQLException;
import java.util.List;

public interface MoraAnualDAO {
    boolean insertar(MoraAnual moraAnual) throws SQLException; // Si el admin añade para nuevos años
    boolean actualizar(MoraAnual moraAnual) throws SQLException; // Si el admin modifica la mora de un año existente
    MoraAnual obtenerPorAnio(int anio) throws SQLException;
    List<MoraAnual> obtenerTodas() throws SQLException;
    boolean eliminar(int anio) throws SQLException; // Si se decide que una configuración anual puede eliminarse
}