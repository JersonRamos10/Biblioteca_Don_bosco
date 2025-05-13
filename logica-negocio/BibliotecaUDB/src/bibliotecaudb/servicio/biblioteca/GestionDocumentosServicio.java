package bibliotecaudb.servicio.biblioteca; 

import bibliotecaudb.dao.biblioteca.DocumentoDAO;
import bibliotecaudb.dao.biblioteca.EjemplarDAO;
import bibliotecaudb.dao.biblioteca.TipoDocumentoDAO;
import bibliotecaudb.modelo.biblioteca.Documento;
import bibliotecaudb.modelo.biblioteca.Ejemplar;
import bibliotecaudb.modelo.biblioteca.TipoDocumento;
import bibliotecaudb.modelo.usuario.Usuario; // Para verificar permisos
import bibliotecaudb.conexion.LogsError;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;


public class GestionDocumentosServicio {


    private static final String TIPO_USUARIO_ADMIN = "Administrador";
    private static final String TIPO_USUARIO_PROFESOR = "Profesor";
    private static final String TIPO_USUARIO_ALUMNO = "Alumno";

    // Instancias de los DAOs 
    private DocumentoDAO documentoDAO;
    private EjemplarDAO ejemplarDAO;
    private TipoDocumentoDAO tipoDocumentoDAO;

    public GestionDocumentosServicio() {
        this.documentoDAO = new DocumentoDAO();
        this.ejemplarDAO = new EjemplarDAO();
        this.tipoDocumentoDAO = new TipoDocumentoDAO();
    }

    /**
     * Registra un nuevo documento y sus ejemplares iniciales.
     * Requiere que el usuario que realiza la acción sea Administrador.
     *
     * @param documento         El objeto Documento a crear (sin ID, se generara).
     * @param tipoDocumentoId   El ID del TipoDocumento.
     * @param cantidadEjemplares El número de ejemplares a crear para este documento.
     * @param ubicacion         La ubicación física de estos ejemplares.
     * @param usuarioOperador   El usuario que realiza la operación (para verificar permisos).
     * @return El Documento creado con su ID asignado.
     * @throws Exception Si el usuario no es Administrador, si los datos son inválidos,
     * o si ocurre un error de base de datos.
     */
    public Documento registrarNuevoDocumentoConEjemplares(Documento documento, int tipoDocumentoId,
                                                        int cantidadEjemplares, String ubicacion,
                                                        Usuario usuarioOperador) throws Exception {
        // Verificar Permisos
        validarPermisoAdmin(usuarioOperador, "registrar nuevo documento con ejemplares");

        // Validar Datos de Entrada
        Objects.requireNonNull(documento, "El objeto Documento no puede ser nulo.");
        Objects.requireNonNull(documento.getTitulo(), "El título del documento no puede ser nulo.");
        
        if (tipoDocumentoId <= 0) {
            throw new Exception("Debe seleccionar un Tipo de Documento válido.");
        }
        if (cantidadEjemplares <= 0) {
            throw new Exception("La cantidad de ejemplares debe ser mayor a cero.");
        }
        if (ubicacion == null || ubicacion.trim().isEmpty()) {
            throw new Exception("La ubicación de los ejemplares es requerida.");
        }

        try {
            // Obtener el TipoDocumento
            TipoDocumento tipoDoc = tipoDocumentoDAO.obtenerPorId(tipoDocumentoId);
            if (tipoDoc == null) {
                throw new Exception("El Tipo de Documento con ID " + tipoDocumentoId + " no existe.");
            }
            documento.setTipoDocumento(tipoDoc); // Asignar el objeto TipoDocumento al Documento

            // Crear el Documento en la BD
            // El método crearDocumento del DAO ya devuelve el documento con el ID asignado
            Documento documentoCreado = documentoDAO.crearDocumento(documento);
            if (documentoCreado == null || documentoCreado.getId() <= 0) {
                 throw new Exception("No se pudo crear el registro del documento en la base de datos.");
            }

            // 5. Crear los Ejemplares
            List<Ejemplar> ejemplaresCreados = new ArrayList<>();
            for (int i = 0; i < cantidadEjemplares; i++) {
                Ejemplar nuevoEjemplar = new Ejemplar();
                nuevoEjemplar.setDocumento(documentoCreado); // Asignar el Documento recién creado
                nuevoEjemplar.setUbicacion(ubicacion);
                nuevoEjemplar.setEstado(Ejemplar.EstadoEjemplar.DISPONIBLE); // Estado por defecto

                Ejemplar ejemplarGuardado = ejemplarDAO.crear(nuevoEjemplar);
                if (ejemplarGuardado != null && ejemplarGuardado.getId() > 0) {
                    ejemplaresCreados.add(ejemplarGuardado);
                } else {
                    // Manejar error: no se pudo crear un ejemplar. ¿Hacemos rollback?
                    // Por simplicidad, aquí solo logueamos y continuamos, pero en un
                    // sistema real, se necesitaría manejo de transacciones.
                    LogsError.error(GestionDocumentosServicio.class,
                            "Error al crear el ejemplar número " + (i + 1) + " para el documento ID: " + documentoCreado.getId());
                }
            }

            if (ejemplaresCreados.size() != cantidadEjemplares) {
                LogsError.warn(GestionDocumentosServicio.class,
                        "Se crearon " + ejemplaresCreados.size() + " de " + cantidadEjemplares +
                        " ejemplares solicitados para el documento ID: " + documentoCreado.getId());
                // Podrías lanzar una excepción parcial o un mensaje de advertencia.
            }

            LogsError.info(GestionDocumentosServicio.class,
                    "Documento ID " + documentoCreado.getId() + " y " + ejemplaresCreados.size() +
                    " ejemplares creados por " + usuarioOperador.getCorreo());

            return documentoCreado; // Devolvemos el documento principal creado

        } catch (SQLException e) {
            LogsError.error(GestionDocumentosServicio.class, "Error de BD al registrar nuevo documento y ejemplares: " + e.getMessage(), e);
            throw new Exception("Error en la base de datos al registrar el documento.", e);
        }
    }


    /**
     * Agrega nuevos ejemplares a un documento que ya existe en la base de datos.
     * Requiere que el usuario que realiza la acción sea Administrador.
     *
     * @param idDocumentoExistente El ID del documento al que se agregarán ejemplares.
     * @param cantidadNuevos      El número de nuevos ejemplares a crear.
     * @param ubicacion           La ubicación de estos nuevos ejemplares.
     * @param usuarioOperador     El usuario que realiza la operación.
     * @return Lista de los nuevos ejemplares creados.
     * @throws Exception Si el usuario no es Admin, el documento no existe, datos inválidos o error de BD.
     */
    public List<Ejemplar> agregarEjemplaresADocumentoExistente(int idDocumentoExistente, int cantidadNuevos,
                                                               String ubicacion, Usuario usuarioOperador) throws Exception {
        // 1. Verificar Permisos
        validarPermisoAdmin(usuarioOperador, "agregar ejemplares a documento existente");

        // 2. Validar Datos de Entrada
        if (idDocumentoExistente <= 0) {
            throw new Exception("El ID del documento existente no es válido.");
        }
        if (cantidadNuevos <= 0) {
            throw new Exception("La cantidad de nuevos ejemplares debe ser mayor a cero.");
        }
        if (ubicacion == null || ubicacion.trim().isEmpty()) {
            throw new Exception("La ubicación de los ejemplares es requerida.");
        }

        try {
            // 3. Verificar que el Documento exista
            Documento documentoExistente = documentoDAO.obtenerPorId(idDocumentoExistente);
            if (documentoExistente == null) {
                throw new Exception("El documento con ID " + idDocumentoExistente + " no existe.");
            }

            // 4. Crear los Ejemplares
            List<Ejemplar> ejemplaresCreados = new ArrayList<>();
            for (int i = 0; i < cantidadNuevos; i++) {
                Ejemplar nuevoEjemplar = new Ejemplar();
                nuevoEjemplar.setDocumento(documentoExistente);
                nuevoEjemplar.setUbicacion(ubicacion);
                nuevoEjemplar.setEstado(Ejemplar.EstadoEjemplar.DISPONIBLE);

                Ejemplar ejemplarGuardado = ejemplarDAO.crear(nuevoEjemplar);
                if (ejemplarGuardado != null && ejemplarGuardado.getId() > 0) {
                    ejemplaresCreados.add(ejemplarGuardado);
                } else {
                    LogsError.error(GestionDocumentosServicio.class,
                            "Error al crear el ejemplar (nuevo) número " + (i + 1) + " para el documento ID: " + idDocumentoExistente);
                }
            }

            if (ejemplaresCreados.size() != cantidadNuevos) {
                LogsError.warn(GestionDocumentosServicio.class,
                        "Se crearon " + ejemplaresCreados.size() + " de " + cantidadNuevos +
                        " ejemplares solicitados para el documento ID: " + idDocumentoExistente);
            }

            LogsError.info(GestionDocumentosServicio.class,
                    ejemplaresCreados.size() + " nuevos ejemplares agregados al documento ID " + idDocumentoExistente +
                    " por " + usuarioOperador.getCorreo());

            return ejemplaresCreados;

        } catch (SQLException e) {
            LogsError.error(GestionDocumentosServicio.class, "Error de BD al agregar ejemplares: " + e.getMessage(), e);
            throw new Exception("Error en la base de datos al agregar ejemplares.", e);
        }
    }

    /**
     * Actualiza la información de un Documento.
     * No actualiza sus ejemplares, solo los datos del documento en sí.
     * Requiere que el usuario que realiza la acción sea Administrador.
     *
     * @param documento         El objeto Documento con los datos actualizados (debe tener un ID válido).
     * @param tipoDocumentoId   El ID del TipoDocumento (puede haber cambiado).
     * @param usuarioOperador   El usuario que realiza la operación.
     * @return true si la actualización fue exitosa, false en caso contrario.
     * @throws Exception Si el usuario no es Admin, datos inválidos o error de BD.
     */
    public boolean actualizarInformacionDocumento(Documento documento, int tipoDocumentoId, Usuario usuarioOperador) throws Exception {
        // 1. Verificar Permisos
        validarPermisoAdmin(usuarioOperador, "actualizar información de documento");

        // 2. Validar Datos de Entrada
        Objects.requireNonNull(documento, "El objeto Documento no puede ser nulo.");
        Objects.requireNonNull(documento.getTitulo(), "El título del documento no puede ser nulo.");
        if (documento.getId() <= 0) {
            throw new Exception("El ID del documento a actualizar no es válido.");
        }
        if (tipoDocumentoId <= 0) {
            throw new Exception("Debe seleccionar un Tipo de Documento válido.");
        }

        try {
            // 3. Obtener y asignar el TipoDocumento
            TipoDocumento tipoDoc = tipoDocumentoDAO.obtenerPorId(tipoDocumentoId);
            if (tipoDoc == null) {
                throw new Exception("El Tipo de Documento con ID " + tipoDocumentoId + " no existe.");
            }
            documento.setTipoDocumento(tipoDoc);

            // 4. Llamar al DAO para actualizar
            boolean exito = documentoDAO.actualizarDocumento(documento);
            if(exito) {
                LogsError.info(GestionDocumentosServicio.class, "Información del documento ID " + documento.getId() + " actualizada por " + usuarioOperador.getCorreo());
            } else {
                 LogsError.warn(GestionDocumentosServicio.class, "No se pudo actualizar el documento ID " + documento.getId());
            }
            return exito;

        } catch (SQLException e) {
            LogsError.error(GestionDocumentosServicio.class, "Error de BD al actualizar documento: " + e.getMessage(), e);
            throw new Exception("Error en la base de datos al actualizar el documento.", e);
        }
    }


    /**
     * Busca documentos según un criterio (título o autor).
     * Esta función puede ser accesible para varios tipos de usuarios.
     *
     * @param criterioBusqueda El texto a buscar.
     * @return Lista de documentos que coinciden con el criterio.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<Documento> buscarDocumentos(String criterioBusqueda) throws Exception {
        if (criterioBusqueda == null || criterioBusqueda.trim().isEmpty()) {
            // Podríamos devolver todos o una lista vacía si el criterio es vacío.
            // Por ahora, devolvemos todos si el criterio es vacío.
            // O lanzar new Exception("El criterio de búsqueda no puede estar vacío.");
            try {
                 return documentoDAO.obtenerTodos();
            } catch (SQLException e) {
                LogsError.error(GestionDocumentosServicio.class, "Error de BD al listar todos los documentos (búsqueda vacía): " + e.getMessage(), e);
                throw new Exception("Error en la base de datos al buscar documentos.", e);
            }
        }
        try {
            return documentoDAO.buscarPorTituloOAutor(criterioBusqueda);
        } catch (SQLException e) {
            LogsError.error(GestionDocumentosServicio.class, "Error de BD al buscar documentos con criterio '" + criterioBusqueda + "': " + e.getMessage(), e);
            throw new Exception("Error en la base de datos al buscar documentos.", e);
        }
    }

    /**
     * Obtiene todos los ejemplares de un documento específico.
     *
     * @param idDocumento El ID del documento.
     * @return Lista de ejemplares.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<Ejemplar> obtenerEjemplaresDeDocumento(int idDocumento) throws Exception {
        if (idDocumento <= 0) {
            throw new Exception("ID de documento no válido.");
        }
        try {
            return ejemplarDAO.obtenerPorIdDocumento(idDocumento);
        } catch (SQLException e) {
            LogsError.error(GestionDocumentosServicio.class, "Error de BD al obtener ejemplares para documento ID " + idDocumento + ": " + e.getMessage(), e);
            throw new Exception("Error en la base de datos al obtener ejemplares.", e);
        }
    }
    
    /**
     * Obtiene los detalles de un ejemplar específico por su ID.
     *
     * @param idEjemplar El ID del ejemplar.
     * @return El objeto Ejemplar.
     * @throws Exception Si ocurre un error de base de datos o no se encuentra.
     */
    public Ejemplar obtenerDetallesEjemplar(int idEjemplar) throws Exception {
        if (idEjemplar <= 0) {
            throw new Exception("ID de ejemplar no válido.");
        }
        try {
            Ejemplar ejemplar = ejemplarDAO.obtenerPorId(idEjemplar);
            if (ejemplar == null) {
                LogsError.warn(GestionDocumentosServicio.class, "No se encontró ejemplar con ID: " + idEjemplar);
                // Podrías lanzar una excepción específica "EjemplarNoEncontradoException"
            }
            return ejemplar;
        } catch (SQLException e) {
            LogsError.error(GestionDocumentosServicio.class, "Error de BD al obtener detalles de ejemplar ID " + idEjemplar + ": " + e.getMessage(), e);
            throw new Exception("Error en la base de datos al obtener detalles del ejemplar.", e);
        }
    }


    /**
     * Actualiza la ubicación de un ejemplar específico.
     * Requiere que el usuario que realiza la acción sea Administrador.
     *
     * @param idEjemplar      El ID del ejemplar a actualizar.
     * @param nuevaUbicacion  La nueva ubicación.
     * @param usuarioOperador El usuario que realiza la operación.
     * @return true si la actualización fue exitosa, false en caso contrario.
     * @throws Exception Si el usuario no es Admin, datos inválidos o error de BD.
     */
    public boolean actualizarUbicacionEjemplar(int idEjemplar, String nuevaUbicacion, Usuario usuarioOperador) throws Exception {
        // 1. Verificar Permisos
        validarPermisoAdmin(usuarioOperador, "actualizar ubicación de ejemplar");

        // 2. Validar Datos
        if (idEjemplar <= 0) {
            throw new Exception("ID de ejemplar no válido.");
        }
        if (nuevaUbicacion == null || nuevaUbicacion.trim().isEmpty()) {
            throw new Exception("La nueva ubicación no puede estar vacía.");
        }

        try {
            // 3. Obtener el ejemplar para asegurar que existe y luego actualizar solo su ubicación
            Ejemplar ejemplar = ejemplarDAO.obtenerPorId(idEjemplar);
            if (ejemplar == null) {
                throw new Exception("El ejemplar con ID " + idEjemplar + " no existe.");
            }

            ejemplar.setUbicacion(nuevaUbicacion); // Actualiza el objeto
            
            // 4. Llamar al DAO para actualizar (el DAO actualizar completo podría usarse aquí,
            // o un método DAO específico para actualizar solo ubicación si fuera muy frecuente).
            // Por ahora usamos el actualizar completo.
            boolean exito = ejemplarDAO.actualizar(ejemplar); 
             if(exito) {
                LogsError.info(GestionDocumentosServicio.class, "Ubicación del ejemplar ID " + idEjemplar + " actualizada por " + usuarioOperador.getCorreo());
            } else {
                 LogsError.warn(GestionDocumentosServicio.class, "No se pudo actualizar la ubicación del ejemplar ID " + idEjemplar);
            }
            return exito;

        } catch (SQLException e) {
            LogsError.error(GestionDocumentosServicio.class, "Error de BD al actualizar ubicación de ejemplar: " + e.getMessage(), e);
            throw new Exception("Error en la base de datos al actualizar la ubicación.", e);
        }
    }

    // --- Metodos de ayuda privados ---

    /**
     * Verifica si el usuario operador es un Administrador.
     * Lanza una excepción si no lo es.
     * @param usuarioOperador El usuario que realiza la operación.
     * @param accion La acción que se intenta realizar (para el mensaje de error).
     * @throws Exception Si el usuario no es Administrador.
     */
    private void validarPermisoAdmin(Usuario usuarioOperador, String accion) throws Exception {
        Objects.requireNonNull(usuarioOperador, "El usuario operador no puede ser nulo para verificar permisos.");
        Objects.requireNonNull(usuarioOperador.getTipoUsuario(), "El tipo de usuario del operador no puede ser nulo.");

        if (!TIPO_USUARIO_ADMIN.equals(usuarioOperador.getTipoUsuario().getTipo())) {
            LogsError.warn(GestionDocumentosServicio.class, "Intento no autorizado de '" + accion + "' por usuario: "
                    + usuarioOperador.getCorreo() + " (Tipo: " + usuarioOperador.getTipoUsuario().getTipo() + ")");
            throw new Exception("No tiene permisos para realizar la acción: " + accion + ". Se requiere ser Administrador.");
        }
    }
}