package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.modelo.biblioteca.Documento;
import java.sql.SQLException;
import java.util.List;

public interface DocumentoDAO {
    boolean insertar(Documento documento) throws SQLException;
    boolean actualizar(Documento documento) throws SQLException;
    boolean eliminar(int id) throws SQLException;
    Documento obtenerPorId(int id) throws SQLException;
    List<Documento> obtenerTodos() throws SQLException;
    List<Documento> buscarPorTerminoGeneral(String termino) throws SQLException; // Busqueda general
}