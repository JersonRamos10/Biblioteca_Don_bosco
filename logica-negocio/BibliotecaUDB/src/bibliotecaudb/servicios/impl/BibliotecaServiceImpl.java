package bibliotecaudb.servicios.impl;

import bibliotecaudb.modelo.biblioteca.*;
import bibliotecaudb.modelo.usuario.*;
import bibliotecaudb.dao.biblioteca.*;
import bibliotecaudb.dao.usuario.*;
import bibliotecaudb.dao.biblioteca.impl.*;
import bibliotecaudb.dao.usuario.impl.*;
import bibliotecaudb.servicios.BibliotecaService;
import bibliotecaudb.servicios.MoraService; 
import bibliotecaudb.excepciones.BibliotecaException; 
import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.math.BigDecimal;

public class BibliotecaServiceImpl implements BibliotecaService {

    private final UsuarioDAO usuarioDAO;
    private final DocumentoDAO documentoDAO;
    private final EjemplarDAO ejemplarDAO;
    private final PrestamoDAO prestamoDAO;
    private final DevolucionDAO devolucionDAO;
    private final PoliticasPrestamoDAO politicasPrestamoDAO;
    private final MoraService moraService; // servicio para la logica de mora


    public BibliotecaServiceImpl() {
        // Instanciacion directa de DAOs y Servicios.
        this.usuarioDAO = new UsuarioDAOImpl(); 
        this.documentoDAO = new DocumentoDAOImpl(); 
        this.ejemplarDAO = new EjemplarDAOImpl(); 
        this.prestamoDAO = new PrestamoDAOImpl();   
        this.devolucionDAO = new DevolucionDAOImpl(); 
        this.politicasPrestamoDAO = new PoliticasPrestamosDAOImpl(); 
        this.moraService = new MoraServiceImpl(); 
    }

    // Constructor para Inyeccion de Dependencias
    public BibliotecaServiceImpl(UsuarioDAO usuarioDAO, DocumentoDAO documentoDAO,
                               EjemplarDAO ejemplarDAO, PrestamoDAO prestamoDAO, DevolucionDAO devolucionDAO,
                               PoliticasPrestamoDAO politicasPrestamoDAO, MoraService moraService) {
        this.usuarioDAO = usuarioDAO;
        this.documentoDAO = documentoDAO;
        this.ejemplarDAO = ejemplarDAO;
        this.prestamoDAO = prestamoDAO;
        this.devolucionDAO = devolucionDAO;
        this.politicasPrestamoDAO = politicasPrestamoDAO;
        this.moraService = moraService;
    }


    // --- Gestión de Catálogo (Documentos y Ejemplares) ---

    @Override
    public boolean registrarNuevoDocumentoConEjemplares(Documento documento, List<Ejemplar> ejemplares) throws SQLException, BibliotecaException {
        Connection conn = null;
        boolean exito = false;
        if (documento == null || documento.getTitulo() == null || documento.getTitulo().trim().isEmpty()) {
            throw new BibliotecaException("El título del documento es obligatorio.");
        }
        if (documento.getIdTipoDocumento() <= 0) {
            throw new BibliotecaException("El tipo de documento es obligatorio.");
        }

        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            boolean docInsertado = documentoDAO.insertar(documento);
            if (!docInsertado || documento.getId() == 0) { // Verificar si el ID se generó
                throw new BibliotecaException("No se pudo insertar el documento principal.");
            }

            if (ejemplares != null) {
                for (Ejemplar ej : ejemplares) {
                    ej.setIdDocumento(documento.getId());
                    if (ej.getEstado() == null) ej.setEstado(Ejemplar.ESTADO_DISPONIBLE); // Default
                    if (!ejemplarDAO.insertar(ej)) {
                        throw new BibliotecaException("No se pudo insertar el ejemplar con ubicación: " + ej.getUbicacion());
                    }
                }
            }

            conn.commit();
            exito = true;
            LogsError.info(this.getClass(), "Documento y " + (ejemplares != null ? ejemplares.size() : 0) + " ejemplares registrados exitosamente. ID Documento: " + documento.getId());

        } catch (SQLException | BibliotecaException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LogsError.error(this.getClass(), "Rollback realizado debido a error al registrar documento con ejemplares.", e);
                } catch (SQLException exRollback) {
                    LogsError.error(this.getClass(), "Error durante el rollback.", exRollback);
                }
            }
            if (e instanceof SQLException) throw (SQLException) e;
            if (e instanceof BibliotecaException) throw (BibliotecaException) e;
            throw new BibliotecaException("Error inesperado al registrar documento: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    LogsError.error(this.getClass(), "Error al restaurar auto-commit.", ex);
                }
            }
        }
        return exito;
    }

    @Override
    public boolean agregarEjemplarADocumentoExistente(int idDocumento, Ejemplar ejemplar) throws SQLException, BibliotecaException {
        Documento docExistente = documentoDAO.obtenerPorId(idDocumento);
        if (docExistente == null) {
            LogsError.warn(this.getClass(), "Intento de agregar ejemplar a documento no existente ID: " + idDocumento);
            throw new BibliotecaException("Documento con ID " + idDocumento + " no existe para agregarle ejemplar.");
        }
        if (ejemplar == null) {
             throw new IllegalArgumentException("El objeto ejemplar no puede ser nulo.");
        }
        ejemplar.setIdDocumento(idDocumento);
        if (ejemplar.getEstado() == null) { // Set default estado si no viene
            ejemplar.setEstado(Ejemplar.ESTADO_DISPONIBLE);
        }
        if (!ejemplarDAO.insertar(ejemplar)) {
            throw new BibliotecaException("No se pudo agregar el ejemplar al documento ID: " + idDocumento);
        }
        LogsError.info(this.getClass(), "Ejemplar ID: " + ejemplar.getId() + " agregado a Documento ID: " + idDocumento);
        return true;
    }

    @Override
    public List<Documento> buscarDocumentos(String termino) throws SQLException {
        if (termino == null || termino.trim().isEmpty()) {
            return documentoDAO.obtenerTodos(); // O devolver lista vacía/error
        }
        return documentoDAO.buscarPorTerminoGeneral(termino);
    }

    @Override
    public Map<String, Object> consultarDetalleDocumento(int idDocumento) throws SQLException, BibliotecaException {
        Map<String, Object> resultado = new HashMap<>();
        Documento doc = documentoDAO.obtenerPorId(idDocumento);
        if (doc == null) {
            throw new BibliotecaException("Documento con ID " + idDocumento + " no encontrado.");
        }
        resultado.put("documento", doc);
        List<Ejemplar> ejemplares = ejemplarDAO.obtenerPorIdDocumento(idDocumento);
        resultado.put("ejemplares", ejemplares);
        resultado.put("totalEjemplares", ejemplares != null ? ejemplares.size() : 0);
        long disponibles = 0;
        if (ejemplares != null) {
            disponibles = ejemplares.stream().filter(e -> Ejemplar.ESTADO_DISPONIBLE.equals(e.getEstado())).count();
        }
        resultado.put("ejemplaresDisponibles", disponibles);

        return resultado;
    }


    // --- Gestion de Prestamos ---
    @Override
    public Prestamo realizarPrestamo(int idUsuario, int idEjemplar) throws SQLException, BibliotecaException {
        Connection conn = null;
        Prestamo nuevoPrestamo = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            Usuario usuario = usuarioDAO.obtenerPorId(idUsuario);
            if (usuario == null) throw new BibliotecaException("Usuario con ID " + idUsuario + " no encontrado.");
            if (!usuario.isEstado()) throw new BibliotecaException("El usuario " + usuario.getNombre() + " no está activo.");
            if (usuario.getTipoUsuario() == null) throw new BibliotecaException("El tipo de usuario para " + usuario.getNombre() + " no está definido.");


            Ejemplar ejemplar = ejemplarDAO.obtenerPorId(idEjemplar);
            if (ejemplar == null) throw new BibliotecaException("Ejemplar con ID " + idEjemplar + " no encontrado.");
            if (ejemplar.getDocumento() == null) throw new BibliotecaException("El ejemplar ID " + idEjemplar + " no tiene un documento asociado (error de datos).");


            if (!Ejemplar.ESTADO_DISPONIBLE.equals(ejemplar.getEstado())) {
                throw new BibliotecaException("El ejemplar '" + ejemplar.getDocumento().getTitulo() + "' (ID: " + idEjemplar + ") no esta disponible.");
            }

            if (prestamoDAO.verificarUsuarioTieneMora(idUsuario)) {
                throw new BibliotecaException("El usuario " + usuario.getNombre() + " tiene prestamos con mora pendiente.");
            }

            PoliticasPrestamo politicas = politicasPrestamoDAO.obtenerPorIdTipoUsuario(usuario.getIdTipoUsuario());
            if (politicas == null) {
                throw new BibliotecaException("No se encontraron politicas de prestamo para el tipo de usuario: " + usuario.getTipoUsuario().getTipo());
            }

            int prestamosActivosUsuario = prestamoDAO.contarPrestamosActivosPorUsuario(idUsuario);
            if (prestamosActivosUsuario >= politicas.getMaxEjemplaresPrestamo()) {
                throw new BibliotecaException("El usuario " + usuario.getNombre() + " ha alcanzado el limite de " + politicas.getMaxEjemplaresPrestamo() + " préstamos activos.");
            }

            nuevoPrestamo = new Prestamo();
            nuevoPrestamo.setIdUsuario(idUsuario);
            nuevoPrestamo.setIdEjemplar(idEjemplar);
            nuevoPrestamo.setFechaPrestamo(LocalDate.now());
            nuevoPrestamo.setFechaLimite(LocalDate.now().plusDays(politicas.getDiasPrestamoDefault()));
            nuevoPrestamo.setMora(BigDecimal.ZERO);

            boolean prestamoInsertado = prestamoDAO.insertar(nuevoPrestamo);

            if (!prestamoInsertado) {
                throw new BibliotecaException("No se pudo registrar el préstamo en la base de datos.");
            }

            conn.commit();
            LogsError.info(this.getClass(), "Prestamo realizado exitosamente ID: " + nuevoPrestamo.getId() + " para usuario ID: " + idUsuario + ", ejemplar ID: " + idEjemplar);
            nuevoPrestamo = prestamoDAO.obtenerPorId(nuevoPrestamo.getId()); // Recargar para obtener objetos anidados

        } catch (SQLException | BibliotecaException e) {
            if (conn != null) {
                try { conn.rollback(); LogsError.error(this.getClass(), "Rollback realizado (prestamo).", e); }
                catch (SQLException exRollback) { LogsError.error(this.getClass(), "Error en rollback (préstamo).", exRollback); }
            }
            if (e instanceof SQLException) throw (SQLException) e;
            if (e instanceof BibliotecaException) throw (BibliotecaException) e;
            throw new BibliotecaException("Error inesperado al realizar prestamo: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); }
                catch (SQLException ex) { LogsError.error(this.getClass(), "Error restaurando auto-commit (prestamo).", ex); }
            }
        }
        return nuevoPrestamo;
    }


    // --- Gestión de Devoluciones ---
    @Override
    public Devolucion registrarDevolucion(int idPrestamo, LocalDate fechaDevolucionActual) throws SQLException, BibliotecaException {
        Connection conn = null;
        Devolucion nuevaDevolucion = null;
        if (fechaDevolucionActual == null) {
            fechaDevolucionActual = LocalDate.now(); // Usar fecha actual si no se provee
        }
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            Prestamo prestamoADevolver = prestamoDAO.obtenerPorId(idPrestamo);
            if (prestamoADevolver == null) {
                throw new BibliotecaException("Préstamo con ID " + idPrestamo + " no encontrado.");
            }
            if (prestamoADevolver.getFechaDevolucion() != null) {
                throw new BibliotecaException("El préstamo ID " + idPrestamo + " ya fue devuelto el " + prestamoADevolver.getFechaDevolucion());
            }

            // Usa MoraService para calcular la mora
            BigDecimal moraCalculada = moraService.calcularMoraParaPrestamo(prestamoADevolver, fechaDevolucionActual);

            boolean prestamoActualizado = prestamoDAO.registrarDevolucion(idPrestamo, fechaDevolucionActual, moraCalculada);
            if (!prestamoActualizado) {
                throw new BibliotecaException("No se pudo actualizar el préstamo ID: " + idPrestamo + " con la información de devolución.");
            }

            nuevaDevolucion = new Devolucion();
            nuevaDevolucion.setIdPrestamo(idPrestamo);
            nuevaDevolucion.setFechaDevolucion(fechaDevolucionActual);
            nuevaDevolucion.setMoraPagada(moraCalculada);

            boolean devolucionInsertada = devolucionDAO.insertar(nuevaDevolucion);

            if (!devolucionInsertada) {
                 throw new BibliotecaException("No se pudo registrar la devolución en la tabla devoluciones para préstamo ID: " + idPrestamo);
            }

            conn.commit();
            LogsError.info(this.getClass(), "Devolución registrada exitosamente ID: " + nuevaDevolucion.getId() + " para préstamo ID: " + idPrestamo);
            // Recargar para obtener el objeto completo con sus relaciones
            nuevaDevolucion = devolucionDAO.obtenerPorId(nuevaDevolucion.getId());

        } catch (SQLException | BibliotecaException e) {
             if (conn != null) {
                try { conn.rollback(); LogsError.error(this.getClass(), "Rollback realizado (devolución).", e); }
                catch (SQLException exRollback) { LogsError.error(this.getClass(), "Error en rollback (devolución).", exRollback); }
            }
            if (e instanceof SQLException) throw (SQLException) e;
            if (e instanceof BibliotecaException) throw (BibliotecaException) e;
            throw new BibliotecaException("Error inesperado durante el registro de devolución: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); }
                catch (SQLException ex) { LogsError.error(this.getClass(), "Error restaurando auto-commit (devolución).", ex); }
            }
        }
        return nuevaDevolucion;
    }

    // --- Consultas ---
    @Override
    public List<Prestamo> obtenerPrestamosActivosUsuario(int idUsuario) throws SQLException {
        return prestamoDAO.obtenerActivosPorIdUsuario(idUsuario);
    }

    @Override
    public List<Prestamo> obtenerHistorialPrestamosUsuario(int idUsuario) throws SQLException {
        return prestamoDAO.obtenerPorIdUsuario(idUsuario);
    }

    @Override
    public List<Prestamo> obtenerTodosLosPrestamosActivos() throws SQLException {
        return prestamoDAO.obtenerPrestamosActivos();
    }

    @Override
    public List<Usuario> obtenerUsuariosConMora() throws SQLException {
        List<Prestamo> prestamosConMora = prestamoDAO.obtenerPrestamosConMoraPendiente();
        List<Usuario> usuariosConMora = new ArrayList<>();
        List<Integer> idsUsuariosYaAgregados = new ArrayList<>();

        for (Prestamo p : prestamosConMora) {
            if (!idsUsuariosYaAgregados.contains(p.getIdUsuario())) {
                Usuario u = p.getUsuario(); // El DAO de prestamo debería haber cargado el usuario
                if (u == null) { // Fallback si no se cargo
                    u = usuarioDAO.obtenerPorId(p.getIdUsuario());
                }
                if (u != null) {
                     usuariosConMora.add(u);
                     idsUsuariosYaAgregados.add(p.getIdUsuario());
                }
            }
        }
        return usuariosConMora;
    }


    // --- Administracion de Configuracion (via MoraService ) ---
    @Override
    public List<PoliticasPrestamo> obtenerTodasLasPoliticasPrestamo() throws SQLException {
        return politicasPrestamoDAO.obtenerTodas();
    }

    @Override
    public boolean actualizarPoliticaPrestamo(PoliticasPrestamo politica) throws SQLException, BibliotecaException {
        if (politica == null) throw new IllegalArgumentException("El objeto PoliticasPrestamo no puede ser nulo.");
        if (politica.getMaxEjemplaresPrestamo() <= 0 || politica.getDiasPrestamoDefault() <= 0) {
            LogsError.warn(this.getClass(), "Intento de actualizar política con valores no positivos: MaxEjemplares=" + politica.getMaxEjemplaresPrestamo() + ", DiasDefault=" + politica.getDiasPrestamoDefault());
            throw new BibliotecaException("Los valores de la política de préstamo (máximo de ejemplares y días) deben ser positivos.");
        }
      
        // El DAO actualizara basado en ese id_tipo_usuario.
        return politicasPrestamoDAO.actualizarPorIdTipoUsuario(politica);
    }

    @Override
    public List<MoraAnual> obtenerTodasLasMorasAnuales() throws SQLException {
        return moraService.obtenerTodasLasMorasAnuales(); // Delegar a MoraService
    }

    @Override
    public boolean guardarMoraAnual(MoraAnual moraAnual) throws SQLException, BibliotecaException {
        return moraService.guardarMoraAnual(moraAnual); // Delegar a MoraService
    }

    @Override
    public ConfiguracionSistema obtenerConfiguracionGlobal() throws SQLException {
       
        return moraService.obtenerConfiguracionGlobalSistema();
    }

    @Override
    public boolean actualizarConfiguracionGlobal(ConfiguracionSistema config) throws SQLException, BibliotecaException {
        return moraService.actualizarConfiguracionGlobalSistema(config); 
    }
}