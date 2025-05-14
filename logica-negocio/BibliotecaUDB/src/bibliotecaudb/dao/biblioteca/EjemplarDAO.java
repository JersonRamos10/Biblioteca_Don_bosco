package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.modelo.biblioteca.Ejemplar;
import java.sql.SQLException;
import java.util.List;

public interface EjemplarDAO {
    boolean insertar(Ejemplar ejemplar) throws SQLException;
    boolean actualizar(Ejemplar ejemplar) throws SQLException;
    boolean actualizarEstado(int idEjemplar, String nuevoEstado) throws SQLException; // Especifico para cambiar solo estado
    boolean eliminar(int id) throws SQLException;
    Ejemplar obtenerPorId(int id) throws SQLException;
    List<Ejemplar> obtenerTodos() throws SQLException;
    List<Ejemplar> obtenerPorIdDocumento(int idDocumento) throws SQLException;
    List<Ejemplar> obtenerDisponiblesPorIdDocumento(int idDocumento) throws SQLException;
    int contarEjemplaresPorDocumento(int idDocumento) throws SQLException;
    int contarEjemplaresDisponiblesPorDocumento(int idDocumento) throws SQLException;
}