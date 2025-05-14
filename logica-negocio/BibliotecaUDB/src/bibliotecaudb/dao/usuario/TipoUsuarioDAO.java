package bibliotecaudb.dao.usuario;

import bibliotecaudb.modelo.usuario.TipoUsuario;

import java.sql.SQLException;
import java.util.List;

public interface TipoUsuarioDAO {
    TipoUsuario obtenerPorId(int id) throws SQLException;
    List<TipoUsuario> obtenerTodos() throws SQLException;
    TipoUsuario obtenerPorNombre(String nombreTipo) throws SQLException;
}