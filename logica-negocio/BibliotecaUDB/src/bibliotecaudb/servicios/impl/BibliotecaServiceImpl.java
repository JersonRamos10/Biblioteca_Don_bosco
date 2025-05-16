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

    private final UsuarioDAO usuarioDAO; // Objeto para manejar usuarios
    private final DocumentoDAO documentoDAO; // Objeto para manejar documentos
    private final EjemplarDAO ejemplarDAO; // Objeto para manejar ejemplares
    private final PrestamoDAO prestamoDAO; // Objeto para manejar prestamos
    private final DevolucionDAO devolucionDAO; // Objeto para manejar devoluciones
    private final PoliticasPrestamoDAO politicasPrestamoDAO; // Objeto para manejar las politicas de prestamo
    private final MoraService moraService; // Servicio para la logica de calculo de mora

    public BibliotecaServiceImpl() {
        // Creamos los objetos DAO y Servicios que necesitamos.
        this.usuarioDAO = new UsuarioDAOImpl();
        this.documentoDAO = new DocumentoDAOImpl();
        this.ejemplarDAO = new EjemplarDAOImpl();
        this.prestamoDAO = new PrestamoDAOImpl();
        this.devolucionDAO = new DevolucionDAOImpl();
        this.politicasPrestamoDAO = new PoliticasPrestamosDAOImpl();
        this.moraService = new MoraServiceImpl();
    }

    // Constructor para pasarle los DAOs y Servicios 
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


    // --- Gestion de Catalogo (Documentos y Ejemplares) ---

    @Override
    public boolean registrarNuevoDocumentoConEjemplares(Documento documento, List<Ejemplar> ejemplares) throws SQLException, BibliotecaException {
        // Este metodo registra un nuevo documento y, opcionalmente, una lista de sus ejemplares.
        Connection conn = null; // Variable para la conexion a la BD
        boolean exito = false; // Para saber si todo salio bien
        if (documento == null || documento.getTitulo() == null || documento.getTitulo().trim().isEmpty()) {
            throw new BibliotecaException("El titulo del documento es obligatorio.");
        }
        if (documento.getIdTipoDocumento() <= 0) { // El ID del tipo de documento debe ser valido
            throw new BibliotecaException("El tipo de documento es obligatorio.");
        }

        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            conn.setAutoCommit(false); // Desactivamos el auto-commit para manejar la transaccion nosotros

            boolean docInsertado = documentoDAO.insertar(documento); // Insertamos el documento principal
            if (!docInsertado || documento.getId() == 0) { // Verificamos si se inserto y si se genero un ID
                throw new BibliotecaException("No se pudo insertar el documento principal.");
            }

            if (ejemplares != null) { // Si nos pasaron una lista de ejemplares
                for (Ejemplar ej : ejemplares) { // Recorremos cada ejemplar
                    ej.setIdDocumento(documento.getId()); // Le asignamos el ID del documento que acabamos de insertar
                    if (ej.getEstado() == null) ej.setEstado(Ejemplar.ESTADO_DISPONIBLE); // Si no tiene estado, lo ponemos como Disponible
                    if (!ejemplarDAO.insertar(ej)) { // Insertamos el ejemplar
                        throw new BibliotecaException("No se pudo insertar el ejemplar con ubicacion: " + ej.getUbicacion());
                    }
                }
            }

            conn.commit(); // Si todo salio bien, confirmamos los cambios en la BD
            exito = true;
            LogsError.info(this.getClass(), "Documento y " + (ejemplares != null ? ejemplares.size() : 0) + " ejemplares registrados exitosamente. ID Documento: " + documento.getId());

        } catch (SQLException | BibliotecaException e) { // Si ocurre algun error (de SQL o de nuestra logica)
            if (conn != null) {
                try {
                    conn.rollback(); // Revertimos todos los cambios hechos en esta transaccion
                    LogsError.error(this.getClass(), "Rollback realizado debido a error al registrar documento con ejemplares.", e);
                } catch (SQLException exRollback) {
                    LogsError.error(this.getClass(), "Error durante el rollback.", exRollback);
                }
            }
            // Relanzamos la excepcion original para que la maneje quien llamo al metodo
            if (e instanceof SQLException) throw (SQLException) e;
            if (e instanceof BibliotecaException) throw (BibliotecaException) e;
            throw new BibliotecaException("Error inesperado al registrar documento: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Siempre restauramos el auto-commit
                } catch (SQLException ex) {
                    LogsError.error(this.getClass(), "Error al restaurar auto-commit.", ex);
                }
                // La conexion generalmente se cierra en una capa superior o al final de la solicitud web.
            }
        }
        return exito; // Devolvemos true si todo fue exitoso
    }

    @Override
    public boolean agregarEjemplarADocumentoExistente(int idDocumento, Ejemplar ejemplar) throws SQLException, BibliotecaException {
        // Este metodo agrega un nuevo ejemplar a un documento que ya existe.
        Documento docExistente = documentoDAO.obtenerPorId(idDocumento); // Verificamos si el documento existe
        if (docExistente == null) {
            LogsError.warn(this.getClass(), "Intento de agregar ejemplar a documento no existente ID: " + idDocumento);
            throw new BibliotecaException("Documento con ID " + idDocumento + " no existe para agregarle ejemplar.");
        }
        if (ejemplar == null) {
             throw new IllegalArgumentException("El objeto ejemplar no puede ser nulo.");
        }
        ejemplar.setIdDocumento(idDocumento); // Asignamos el ID del documento al ejemplar
        if (ejemplar.getEstado() == null) { // Si no se especifica estado, se pone como Disponible
            ejemplar.setEstado(Ejemplar.ESTADO_DISPONIBLE);
        }
        if (!ejemplarDAO.insertar(ejemplar)) { // Insertamos el ejemplar
            throw new BibliotecaException("No se pudo agregar el ejemplar al documento ID: " + idDocumento);
        }
        LogsError.info(this.getClass(), "Ejemplar ID: " + ejemplar.getId() + " agregado a Documento ID: " + idDocumento);
        return true; // Si llego hasta aqui, se agrego bien
    }

    @Override
    public List<Documento> buscarDocumentos(String termino) throws SQLException {
        // Este metodo busca documentos segun un termino de busqueda.
        if (termino == null || termino.trim().isEmpty()) { // Si el termino esta vacio
            return documentoDAO.obtenerTodos(); // Devolvemos todos los documentos (o podria ser una lista vacia)
        }
        return documentoDAO.buscarPorTerminoGeneral(termino); // Usamos el DAO para buscar
    }

    @Override
    public Map<String, Object> consultarDetalleDocumento(int idDocumento) throws SQLException, BibliotecaException {
        // Este metodo obtiene los detalles de un documento, incluyendo sus ejemplares.
        Map<String, Object> resultado = new HashMap<>(); // Usamos un Mapa para devolver varios datos
        Documento doc = documentoDAO.obtenerPorId(idDocumento); // Obtenemos el documento
        if (doc == null) {
            throw new BibliotecaException("Documento con ID " + idDocumento + " no encontrado.");
        }
        resultado.put("documento", doc); // Agregamos el documento al resultado
        List<Ejemplar> ejemplares = ejemplarDAO.obtenerPorIdDocumento(idDocumento); // Obtenemos sus ejemplares
        resultado.put("ejemplares", ejemplares); // Agregamos la lista de ejemplares
        resultado.put("totalEjemplares", ejemplares != null ? ejemplares.size() : 0); // Contamos el total de ejemplares

        long disponibles = 0; // Variable para contar cuantos estan disponibles
        if (ejemplares != null) {
            // Contamos cuantos ejemplares de la lista estan en estado "DISPONIBLE"
            disponibles = ejemplares.stream().filter(e -> Ejemplar.ESTADO_DISPONIBLE.equals(e.getEstado())).count();
        }
        resultado.put("ejemplaresDisponibles", disponibles); // Agregamos la cantidad de disponibles

        return resultado; // Devolvemos el mapa con toda la informacion
    }


    // --- Gestion de Prestamos ---
    @Override
    public Prestamo realizarPrestamo(int idUsuario, int idEjemplar) throws SQLException, BibliotecaException {
        // Este metodo se encarga de toda la logica para realizar un prestamo.
        Connection conn = null; // Variable para la conexion
        Prestamo nuevoPrestamo = null; // El prestamo que vamos a crear
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            conn.setAutoCommit(false); // Desactivamos auto-commit para manejar la transaccion

            Usuario usuario = usuarioDAO.obtenerPorId(idUsuario); // Obtenemos los datos del usuario
            if (usuario == null) throw new BibliotecaException("Usuario con ID " + idUsuario + " no encontrado.");
            if (!usuario.isEstado()) throw new BibliotecaException("El usuario " + usuario.getNombre() + " no esta activo."); // El usuario debe estar activo
            if (usuario.getTipoUsuario() == null) throw new BibliotecaException("El tipo de usuario para " + usuario.getNombre() + " no esta definido.");


            Ejemplar ejemplar = ejemplarDAO.obtenerPorId(idEjemplar); // Obtenemos los datos del ejemplar
            if (ejemplar == null) throw new BibliotecaException("Ejemplar con ID " + idEjemplar + " no encontrado.");
            if (ejemplar.getDocumento() == null) throw new BibliotecaException("El ejemplar ID " + idEjemplar + " no tiene un documento asociado (error de datos).");


            if (!Ejemplar.ESTADO_DISPONIBLE.equals(ejemplar.getEstado())) { // El ejemplar debe estar disponible
                throw new BibliotecaException("El ejemplar '" + ejemplar.getDocumento().getTitulo() + "' (ID: " + idEjemplar + ") no esta disponible.");
            }

            if (prestamoDAO.verificarUsuarioTieneMora(idUsuario)) { // Verificamos si el usuario tiene moras pendientes
                throw new BibliotecaException("El usuario " + usuario.getNombre() + " tiene prestamos con mora pendiente.");
            }

            PoliticasPrestamo politicas = politicasPrestamoDAO.obtenerPorIdTipoUsuario(usuario.getIdTipoUsuario()); // Obtenemos las politicas para ese tipo de usuario
            if (politicas == null) {
                throw new BibliotecaException("No se encontraron politicas de prestamo para el tipo de usuario: " + usuario.getTipoUsuario().getTipo());
            }

            int prestamosActivosUsuario = prestamoDAO.contarPrestamosActivosPorUsuario(idUsuario); // Contamos cuantos prestamos activos ya tiene el usuario
            if (prestamosActivosUsuario >= politicas.getMaxEjemplaresPrestamo()) { // Verificamos si alcanzo el limite
                throw new BibliotecaException("El usuario " + usuario.getNombre() + " ha alcanzado el limite de " + politicas.getMaxEjemplaresPrestamo() + " prestamos activos.");
            }

            // Si todas las validaciones pasan, creamos el objeto Prestamo
            nuevoPrestamo = new Prestamo();
            nuevoPrestamo.setIdUsuario(idUsuario);
            nuevoPrestamo.setIdEjemplar(idEjemplar);
            nuevoPrestamo.setFechaPrestamo(LocalDate.now()); // La fecha de hoy
            nuevoPrestamo.setFechaLimite(LocalDate.now().plusDays(politicas.getDiasPrestamoDefault())); // Sumamos los dias de prestamo permitidos
            nuevoPrestamo.setMora(BigDecimal.ZERO); // La mora inicial es cero

            boolean prestamoInsertado = prestamoDAO.insertar(nuevoPrestamo); // Insertamos el prestamo

            if (!prestamoInsertado) { // Si no se pudo insertar
                throw new BibliotecaException("No se pudo registrar el prestamo en la base de datos.");
            }
            // Nota: El cambio de estado del ejemplar a 'PRESTADO' deberia hacerse por un trigger en la BD al insertar en 'prestamos'.

            conn.commit(); // Confirmamos la transaccion
            LogsError.info(this.getClass(), "Prestamo realizado exitosamente ID: " + nuevoPrestamo.getId() + " para usuario ID: " + idUsuario + ", ejemplar ID: " + idEjemplar);
            nuevoPrestamo = prestamoDAO.obtenerPorId(nuevoPrestamo.getId()); // Volvemos a cargar el prestamo para tener todos los objetos relacionados (Usuario, Ejemplar)

        } catch (SQLException | BibliotecaException e) { // Si hay algun error
            if (conn != null) {
                try { conn.rollback(); LogsError.error(this.getClass(), "Rollback realizado (prestamo).", e); } // Revertimos
                catch (SQLException exRollback) { LogsError.error(this.getClass(), "Error en rollback (prestamo).", exRollback); }
            }
            // Relanzamos la excepcion
            if (e instanceof SQLException) throw (SQLException) e;
            if (e instanceof BibliotecaException) throw (BibliotecaException) e;
            throw new BibliotecaException("Error inesperado al realizar prestamo: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } // Restauramos el auto-commit
                catch (SQLException ex) { LogsError.error(this.getClass(), "Error restaurando auto-commit (prestamo).", ex); }
            }
        }
        return nuevoPrestamo; // Devolvemos el prestamo creado
    }


    // --- Gestion de Devoluciones ---
    @Override
    public Devolucion registrarDevolucion(int idPrestamo, LocalDate fechaDevolucionActual) throws SQLException, BibliotecaException {
        // Este metodo registra la devolucion de un prestamo.
        Connection conn = null; // Variable para la conexion
        Devolucion nuevaDevolucion = null; // La devolucion que vamos a crear
        if (fechaDevolucionActual == null) {
            fechaDevolucionActual = LocalDate.now(); // Si no nos dan fecha, usamos la de hoy
        }
        try {
            conn = ConexionBD.getConexion(); // Obtenemos la conexion
            conn.setAutoCommit(false); // Manejamos la transaccion

            Prestamo prestamoADevolver = prestamoDAO.obtenerPorId(idPrestamo); // Obtenemos el prestamo que se va a devolver
            if (prestamoADevolver == null) {
                throw new BibliotecaException("Prestamo con ID " + idPrestamo + " no encontrado.");
            }
            if (prestamoADevolver.getFechaDevolucion() != null) { // Verificamos si ya fue devuelto antes
                throw new BibliotecaException("El prestamo ID " + idPrestamo + " ya fue devuelto el " + prestamoADevolver.getFechaDevolucion());
            }

            // Usamos el MoraService para calcular la mora (si la hay)
            BigDecimal moraCalculada = moraService.calcularMoraParaPrestamo(prestamoADevolver, fechaDevolucionActual);

            // Actualizamos el prestamo en la tabla 'prestamos' con la fecha de devolucion y la mora
            boolean prestamoActualizado = prestamoDAO.registrarDevolucion(idPrestamo, fechaDevolucionActual, moraCalculada);
            if (!prestamoActualizado) {
                throw new BibliotecaException("No se pudo actualizar el prestamo ID: " + idPrestamo + " con la informacion de devolucion.");
            }

            // Creamos el objeto Devolucion para guardarlo en la tabla 'devoluciones'
            nuevaDevolucion = new Devolucion();
            nuevaDevolucion.setIdPrestamo(idPrestamo);
            nuevaDevolucion.setFechaDevolucion(fechaDevolucionActual);
            nuevaDevolucion.setMoraPagada(moraCalculada); // Asumimos que la mora calculada es la que se paga

            boolean devolucionInsertada = devolucionDAO.insertar(nuevaDevolucion); // Insertamos en 'devoluciones'

            if (!devolucionInsertada) { // Si no se pudo insertar
                 throw new BibliotecaException("No se pudo registrar la devolucion en la tabla devoluciones para prestamo ID: " + idPrestamo);
            }
            // Nota: El cambio de estado del ejemplar a 'DISPONIBLE' deberia ocurrir por un trigger en la BD al insertar en 'devoluciones'.

            conn.commit(); // Confirmamos la transaccion
            LogsError.info(this.getClass(), "Devolucion registrada exitosamente ID: " + nuevaDevolucion.getId() + " para prestamo ID: " + idPrestamo);
            // Volvemos a cargar la devolucion para tener todos sus datos y objetos relacionados
            nuevaDevolucion = devolucionDAO.obtenerPorId(nuevaDevolucion.getId());

        } catch (SQLException | BibliotecaException e) { // Si hay algun error
             if (conn != null) {
                try { conn.rollback(); LogsError.error(this.getClass(), "Rollback realizado (devolucion).", e); } // Revertimos
                catch (SQLException exRollback) { LogsError.error(this.getClass(), "Error en rollback (devolucion).", exRollback); }
            }
            // Relanzamos la excepcion
            if (e instanceof SQLException) throw (SQLException) e;
            if (e instanceof BibliotecaException) throw (BibliotecaException) e;
            throw new BibliotecaException("Error inesperado durante el registro de devolucion: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } // Restauramos auto-commit
                catch (SQLException ex) { LogsError.error(this.getClass(), "Error restaurando auto-commit (devolucion).", ex); }
            }
        }
        return nuevaDevolucion; // Devolvemos el objeto Devolucion creado
    }

    // --- Consultas ---
    @Override
    public List<Prestamo> obtenerPrestamosActivosUsuario(int idUsuario) throws SQLException {
        // Este metodo devuelve los prestamos que un usuario tiene actualmente sin devolver.
        return prestamoDAO.obtenerActivosPorIdUsuario(idUsuario);
    }

    @Override
    public List<Prestamo> obtenerHistorialPrestamosUsuario(int idUsuario) throws SQLException {
        // Este metodo devuelve todos los prestamos (activos y devueltos) de un usuario.
        return prestamoDAO.obtenerPorIdUsuario(idUsuario);
    }

    @Override
    public List<Prestamo> obtenerTodosLosPrestamosActivos() throws SQLException {
        // Este metodo devuelve todos los prestamos que estan activos en el sistema.
        return prestamoDAO.obtenerPrestamosActivos();
    }

    @Override
    public List<Usuario> obtenerUsuariosConMora() throws SQLException {
        // Este metodo busca todos los prestamos con mora y devuelve una lista de los usuarios que tienen mora.
        List<Prestamo> prestamosConMora = prestamoDAO.obtenerPrestamosConMoraPendiente(); // Obtenemos los prestamos con mora
        List<Usuario> usuariosConMora = new ArrayList<>(); // Lista para los usuarios
        List<Integer> idsUsuariosYaAgregados = new ArrayList<>(); // Para no agregar usuarios repetidos

        for (Prestamo p : prestamosConMora) { // Recorremos los prestamos con mora
            if (!idsUsuariosYaAgregados.contains(p.getIdUsuario())) { // Si no hemos agregado a este usuario aun
                Usuario u = p.getUsuario(); // El DAO de prestamo ya deberia haber cargado el usuario
                if (u == null) { // Si por alguna razon no lo cargo, lo buscamos nosotros
                    u = usuarioDAO.obtenerPorId(p.getIdUsuario());
                }
                if (u != null) { // Si tenemos el usuario
                     usuariosConMora.add(u); // Lo agregamos a la lista
                     idsUsuariosYaAgregados.add(p.getIdUsuario()); // Marcamos que ya lo agregamos
                }
            }
        }
        return usuariosConMora; // Devolvemos la lista de usuarios con mora
    }


    // --- Administracion de Configuracion (se delega en parte a MoraService) ---
    @Override
    public List<PoliticasPrestamo> obtenerTodasLasPoliticasPrestamo() throws SQLException {
        // Este metodo devuelve todas las politicas de prestamo configuradas.
        return politicasPrestamoDAO.obtenerTodas();
    }

    @Override
    public boolean actualizarPoliticaPrestamo(PoliticasPrestamo politica) throws SQLException, BibliotecaException {
        // Este metodo actualiza una politica de prestamo existente.
        if (politica == null) throw new IllegalArgumentException("El objeto PoliticasPrestamo no puede ser nulo.");
        if (politica.getMaxEjemplaresPrestamo() <= 0 || politica.getDiasPrestamoDefault() <= 0) { // Validamos los valores
            LogsError.warn(this.getClass(), "Intento de actualizar politica con valores no positivos: MaxEjemplares=" + politica.getMaxEjemplaresPrestamo() + ", DiasDefault=" + politica.getDiasPrestamoDefault());
            throw new BibliotecaException("Los valores de la politica de prestamo (maximo de ejemplares y dias) deben ser positivos.");
        }
        // El DAO se encarga de actualizar usando el id_tipo_usuario de la politica.
        return politicasPrestamoDAO.actualizarPorIdTipoUsuario(politica);
    }

    @Override
    public List<MoraAnual> obtenerTodasLasMorasAnuales() throws SQLException {
        // Este metodo obtiene todas las configuraciones de mora anual. Delega al MoraService.
        return moraService.obtenerTodasLasMorasAnuales();
    }

    @Override
    public boolean guardarMoraAnual(MoraAnual moraAnual) throws SQLException, BibliotecaException {
        // Este metodo guarda o actualiza una configuracion de mora anual. Delega al MoraService.
        return moraService.guardarMoraAnual(moraAnual);
    }

    @Override
    public ConfiguracionSistema obtenerConfiguracionGlobal() throws SQLException {
        // Este metodo obtiene la configuracion global del sistema. Delega al MoraService.
        return moraService.obtenerConfiguracionGlobalSistema();
    }

    @Override
    public boolean actualizarConfiguracionGlobal(ConfiguracionSistema config) throws SQLException, BibliotecaException {
        // Este metodo actualiza la configuracion global del sistema. Delega al MoraService.
        return moraService.actualizarConfiguracionGlobalSistema(config);
    }
}