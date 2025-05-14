package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.modelo.biblioteca.Devolucion;
import java.sql.SQLException;
import java.util.List;

public interface DevolucionDAO {
    boolean insertar(Devolucion devolucion) throws SQLException;
    Devolucion obtenerPorId(int id) throws SQLException;
    List<Devolucion> obtenerPorIdPrestamo(int idPrestamo) throws SQLException; 
    List<Devolucion> obtenerTodas() throws SQLException;
}