package bibliotecaudb.servicios;

import bibliotecaudb.modelo.biblioteca. *;
import bibliotecaudb.modelo.usuario. *;
import bibliotecaudb.excepciones.BibliotecaException; 
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;


public interface BibliotecaService {

    // --- Gestion de Catalogo (Documentos y Ejemplares) ---
    boolean registrarNuevoDocumentoConEjemplares(Documento documento, List<Ejemplar> ejemplares) throws SQLException, BibliotecaException; 
    boolean agregarEjemplarADocumentoExistente(int idDocumento, Ejemplar ejemplar) throws SQLException, BibliotecaException; 
    List<Documento> buscarDocumentos(String termino) throws SQLException;
    Map<String, Object> consultarDetalleDocumento(int idDocumento) throws SQLException, BibliotecaException; 

    // --- Gestion de Prestamos ---
    Prestamo realizarPrestamo(int idUsuario, int idEjemplar) throws SQLException, BibliotecaException; 

    // --- Gestion de Devoluciones ---
    Devolucion registrarDevolucion(int idPrestamo, LocalDate fechaDevolucionActual) throws SQLException, BibliotecaException; 

    // --- Consultas ---
    List<Prestamo> obtenerPrestamosActivosUsuario(int idUsuario) throws SQLException;
    List<Prestamo> obtenerHistorialPrestamosUsuario(int idUsuario) throws SQLException;
    List<Prestamo> obtenerTodosLosPrestamosActivos() throws SQLException;
    List<Usuario> obtenerUsuariosConMora() throws SQLException;

    // --- Administracion de Configuracion ---
    List<PoliticasPrestamo> obtenerTodasLasPoliticasPrestamo() throws SQLException;
    boolean actualizarPoliticaPrestamo(PoliticasPrestamo politica) throws SQLException, BibliotecaException; 
    
    List<MoraAnual> obtenerTodasLasMorasAnuales() throws SQLException;
    boolean guardarMoraAnual(MoraAnual moraAnual) throws SQLException, BibliotecaException; 
    
    ConfiguracionSistema obtenerConfiguracionGlobal() throws SQLException;
    boolean actualizarConfiguracionGlobal(ConfiguracionSistema config) throws SQLException, BibliotecaException; //
}