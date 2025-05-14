package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.modelo.biblioteca.PoliticasPrestamo;
import java.sql.SQLException;
import java.util.List;

public interface PoliticasPrestamoDAO {
    boolean insertar(PoliticasPrestamo politica) throws SQLException;
    boolean actualizarPorIdTipoUsuario(PoliticasPrestamo politica) throws SQLException; 
    PoliticasPrestamo obtenerPorIdTipoUsuario(int idTipoUsuario) throws SQLException;
    PoliticasPrestamo obtenerPorIdPolitica(int idPolitica) throws SQLException;
    List<PoliticasPrestamo> obtenerTodas() throws SQLException;
}