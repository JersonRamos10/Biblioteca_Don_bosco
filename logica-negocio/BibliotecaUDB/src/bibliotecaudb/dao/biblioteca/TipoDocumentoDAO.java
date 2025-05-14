package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.modelo.biblioteca.TipoDocumento;
import java.sql.SQLException;
import java.util.List;

public interface TipoDocumentoDAO {
    boolean insertar(TipoDocumento tipoDocumento) throws SQLException;
    boolean actualizar(TipoDocumento tipoDocumento) throws SQLException;
    boolean eliminar(int id) throws SQLException;
    TipoDocumento obtenerPorId(int id) throws SQLException;
    TipoDocumento obtenerPorNombre(String nombreTipo) throws SQLException;
    List<TipoDocumento> obtenerTodos() throws SQLException;
}