package bibliotecaudb.dao.biblioteca;

import bibliotecaudb.modelo.biblioteca.Prestamo;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface PrestamoDAO {
    boolean insertar(Prestamo prestamo) throws SQLException;
    boolean actualizar(Prestamo prestamo) throws SQLException; // Ej. para registrar fecha_devolucion y mora
    Prestamo obtenerPorId(int id) throws SQLException;
    List<Prestamo> obtenerTodos() throws SQLException;
    List<Prestamo> obtenerPorIdUsuario(int idUsuario) throws SQLException;
    List<Prestamo> obtenerActivosPorIdUsuario(int idUsuario) throws SQLException; // Prestamos no devueltos
    List<Prestamo> obtenerPrestamosActivos() throws SQLException; // Todos los prestamos no devueltos
    List<Prestamo> obtenerPrestamosConMoraPendiente() throws SQLException; // Activos y fecha_limite < HOY
    List<Prestamo> obtenerPrestamosConMoraPendientePorUsuario(int idUsuario) throws SQLException;
    int contarPrestamosActivosPorUsuario(int idUsuario) throws SQLException;
    boolean verificarUsuarioTieneMora(int idUsuario) throws SQLException;
    boolean registrarDevolucion(int idPrestamo, LocalDate fechaDevolucion, java.math.BigDecimal moraPagada) throws SQLException;
}