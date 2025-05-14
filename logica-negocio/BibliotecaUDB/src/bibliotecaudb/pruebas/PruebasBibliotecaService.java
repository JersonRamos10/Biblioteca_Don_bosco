package bibliotecaudb.pruebas;

import bibliotecaudb.modelo.usuario. *;
import bibliotecaudb.modelo.biblioteca. *;
import bibliotecaudb.servicios.*;
import bibliotecaudb.servicios.impl.*;
import bibliotecaudb.dao.biblioteca.impl. *;
import bibliotecaudb.excepciones.UsuarioException;
import bibliotecaudb.excepciones.BibliotecaException;
import bibliotecaudb.conexion.LogsError;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public class PruebasBibliotecaService {

    public static void main(String[] args) {
        UsuarioService usuarioService = new UsuarioServiceImpl();
        BibliotecaService bibliotecaService = new BibliotecaServiceImpl();
        
        Usuario admin = null;
        Usuario alumnoPrueba = null; // Usaremos un alumno existente o crearemos uno
        Documento libroPrueba = null;
        Ejemplar ejemplarPrueba1 = null;
        int idEjemplarPrestado = -1;
        int idPrestamoRealizado = -1;

        LogsError.info(PruebasBibliotecaService.class, "=== INICIO DE PRUEBAS DEL SERVICIO DE BIBLIOTECA ===");

        try {
            // --- Configuración Inicial: Autenticar usuarios ---
            LogsError.info(PruebasBibliotecaService.class, "\n--- Configuración: Autenticando Usuarios ---");
            try {
                admin = usuarioService.autenticarUsuario("admin@udb.com", "AdminUdb2025.");
                LogsError.info(PruebasBibliotecaService.class, "Admin autenticado: " + admin.getNombre());
                
                // Intentar obtener el alumno1@udb.com o crear uno si no existe para pruebas
                alumnoPrueba = usuarioService.obtenerUsuarioPorCorreo("alumno1@udb.com");
                if (alumnoPrueba == null) { // Si no existe, lo creamos (requiere que esta lógica esté probada y funcione)
                    LogsError.warn(PruebasBibliotecaService.class, "alumno1@udb.com no encontrado, intentando crear uno para pruebas.");
                    Usuario nuevoAlumnoParaPrueba = new Usuario();
                    nuevoAlumnoParaPrueba.setNombre("Alumno Prueba Biblioteca");
                    nuevoAlumnoParaPrueba.setCorreo("alumnoprueba_biblio@udb.com"); // Correo único
                    nuevoAlumnoParaPrueba.setContrasena("AlumnoPass123");
                    nuevoAlumnoParaPrueba.setIdTipoUsuario(3); // Alumno
                    nuevoAlumnoParaPrueba.setEstado(true);
                    if(usuarioService.registrarNuevoUsuario(nuevoAlumnoParaPrueba, admin)){
                        alumnoPrueba = usuarioService.obtenerUsuarioPorCorreo(nuevoAlumnoParaPrueba.getCorreo());
                         LogsError.info(PruebasBibliotecaService.class, "Alumno de prueba creado y obtenido: " + alumnoPrueba.getNombre());
                    } else {
                        LogsError.error(PruebasBibliotecaService.class, "No se pudo crear el alumno de prueba.");
                        return;
                    }
                } else {
                    LogsError.info(PruebasBibliotecaService.class, "Alumno de prueba ('alumno1@udb.com') obtenido: " + alumnoPrueba.getNombre());
                }

            } catch (UsuarioException | SQLException e) {
                LogsError.error(PruebasBibliotecaService.class, "Error en configuración inicial de usuarios: " + e.getMessage(), e);
                return; // No continuar
            }
            
            if (admin == null || alumnoPrueba == null) {
                LogsError.error(PruebasBibliotecaService.class, "No se pudieron obtener los usuarios necesarios para las pruebas.");
                return;
            }

            // --- Pruebas de Catálogo ---
            LogsError.info(PruebasBibliotecaService.class, "\n--- Pruebas de Catálogo ---");
            // 1. Registrar Nuevo Documento con Ejemplares
            libroPrueba = new Documento();
            libroPrueba.setTitulo("Aventuras en Java Service");
            libroPrueba.setAutor("Dev UDB");
            libroPrueba.setEditorial("Ediciones Código");
            libroPrueba.setAnioPublicacion(LocalDate.now().getYear());
            libroPrueba.setIdTipoDocumento(1); // Libro

            List<Ejemplar> ejemplaresNuevos = new ArrayList<>();
            ejemplarPrueba1 = new Ejemplar();
            ejemplarPrueba1.setUbicacion("SERV-A1");
            ejemplarPrueba1.setEstado(Ejemplar.ESTADO_DISPONIBLE);
            ejemplaresNuevos.add(ejemplarPrueba1);
            
            Ejemplar ejTemp2 = new Ejemplar();
            ejTemp2.setUbicacion("SERV-A2");
            ejTemp2.setEstado(Ejemplar.ESTADO_DISPONIBLE);
            ejemplaresNuevos.add(ejTemp2);

            try {
                if (bibliotecaService.registrarNuevoDocumentoConEjemplares(libroPrueba, ejemplaresNuevos)) {
                    LogsError.info(PruebasBibliotecaService.class, "ÉXITO: Documento y ejemplares registrados. ID Documento: " + libroPrueba.getId());
                    // Asumimos que los IDs de ejemplares se actualizan en los objetos dentro de la lista
                    if (ejemplarPrueba1.getId() > 0) {
                        idEjemplarPrestado = ejemplarPrueba1.getId(); // Guardar ID para préstamo
                         LogsError.info(PruebasBibliotecaService.class, "ID del primer ejemplar para prestar: " + idEjemplarPrestado);
                    } else {
                        // Si el ID no se actualizó, buscarlo (esto es un fallback)
                        Map<String,Object> detalle = bibliotecaService.consultarDetalleDocumento(libroPrueba.getId());
                        List<Ejemplar> listaEjs = (List<Ejemplar>) detalle.get("ejemplares");
                        if(!listaEjs.isEmpty()) idEjemplarPrestado = listaEjs.get(0).getId();
                        LogsError.warn(PruebasBibliotecaService.class, "ID del ejemplar obtenido por consulta: " + idEjemplarPrestado);
                    }
                } else {
                    LogsError.error(PruebasBibliotecaService.class, "FALLO: No se registró el documento con ejemplares.");
                }
            } catch (BibliotecaException | SQLException e) {
                LogsError.error(PruebasBibliotecaService.class, "Error registrando documento: " + e.getMessage(), e);
            }

            // 2. Buscar Documentos
            LogsError.info(PruebasBibliotecaService.class, "\n[Prueba Catálogo 2: Buscar Documentos ('Java Service')]");
            try {
                List<Documento> docsEncontrados = bibliotecaService.buscarDocumentos("Java Service");
                LogsError.info(PruebasBibliotecaService.class, "Documentos encontrados: " + docsEncontrados.size());
                // docsEncontrados.forEach(d -> LogsError.info(PruebasBibliotecaService.class, d.toString()));
            } catch (SQLException e) {
                 LogsError.error(PruebasBibliotecaService.class, "Error buscando documentos: " + e.getMessage(), e);
            }

            // --- Pruebas de Préstamos ---
            if (idEjemplarPrestado > 0) {
                LogsError.info(PruebasBibliotecaService.class, "\n--- Pruebas de Préstamos (Usuario: " + alumnoPrueba.getNombre() + ", Ejemplar ID: " + idEjemplarPrestado + ") ---");
                // 3. Realizar Préstamo Exitoso
                LogsError.info(PruebasBibliotecaService.class, "\n[Prueba Préstamo 3: Realizar Préstamo Exitoso]");
                try {
                    Prestamo prestamo = bibliotecaService.realizarPrestamo(alumnoPrueba.getId(), idEjemplarPrestado);
                    idPrestamoRealizado = prestamo.getId(); // Guardar para devolución
                    LogsError.info(PruebasBibliotecaService.class, "ÉXITO: Préstamo realizado. ID: " + prestamo.getId() + ", Límite: " + prestamo.getFechaLimite());
                } catch (BibliotecaException | SQLException e) {
                    LogsError.error(PruebasBibliotecaService.class, "Error realizando préstamo: " + e.getMessage(), e);
                }

                // 4. Intentar Prestar Mismo Ejemplar (Debería Fallar)
                LogsError.info(PruebasBibliotecaService.class, "\n[Prueba Préstamo 4: Intentar Prestar Mismo Ejemplar de Nuevo]");
                 try {
                    bibliotecaService.realizarPrestamo(alumnoPrueba.getId(), idEjemplarPrestado);
                    LogsError.error(PruebasBibliotecaService.class, "FALLO: Se prestó un ejemplar que ya estaba prestado.");
                } catch (BibliotecaException e) {
                    LogsError.info(PruebasBibliotecaService.class, "ÉXITO: No se pudo prestar ejemplar no disponible: " + e.getMessage());
                } catch (SQLException e) {
                    LogsError.error(PruebasBibliotecaService.class, "Error SQL en Prueba Préstamo 4: " + e.getMessage(), e);
                }
            } else {
                LogsError.warn(PruebasBibliotecaService.class, "Saltando pruebas de préstamo porque no se obtuvo un ID de ejemplar válido.");
            }
            
            // --- Pruebas de Devoluciones ---
            if (idPrestamoRealizado > 0) {
                LogsError.info(PruebasBibliotecaService.class, "\n--- Pruebas de Devoluciones (Préstamo ID: " + idPrestamoRealizado + ") ---");
                // 5. Registrar Devolución (simular con mora)
                LogsError.info(PruebasBibliotecaService.class, "\n[Prueba Devolución 5: Registrar Devolución con Mora Simulada]");
                Prestamo prestamoParaDevolver = (new PrestamoDAOImpl()).obtenerPorId(idPrestamoRealizado); // Obtener datos frescos del préstamo
                if(prestamoParaDevolver != null) {
                    LocalDate fechaDevolucionConMora = prestamoParaDevolver.getFechaLimite().plusDays(5); // 5 días después del límite
                    try {
                        Devolucion devolucion = bibliotecaService.registrarDevolucion(idPrestamoRealizado, fechaDevolucionConMora);
                        LogsError.info(PruebasBibliotecaService.class, "ÉXITO: Devolución registrada. ID: " + devolucion.getId() + ", Mora Pagada: " + devolucion.getMoraPagada());
                        
                        // Verificar que el ejemplar ahora está disponible
                        Ejemplar ejemplarDevuelto = (new EjemplarDAOImpl()).obtenerPorId(idEjemplarPrestado);
                        if (ejemplarDevuelto != null && Ejemplar.ESTADO_DISPONIBLE.equals(ejemplarDevuelto.getEstado())) {
                            LogsError.info(PruebasBibliotecaService.class, "Verificación: Ejemplar ID " + idEjemplarPrestado + " está ahora DISPONIBLE.");
                        } else {
                            LogsError.error(PruebasBibliotecaService.class, "Verificación FALLIDA: Ejemplar ID " + idEjemplarPrestado + " NO está disponible después de la devolución.");
                        }

                    } catch (BibliotecaException | SQLException e) {
                        LogsError.error(PruebasBibliotecaService.class, "Error registrando devolución: " + e.getMessage(), e);
                    }
                } else {
                    LogsError.error(PruebasBibliotecaService.class, "No se pudo obtener el préstamo ID " + idPrestamoRealizado + " para la prueba de devolución.");
                }
            } else {
                LogsError.warn(PruebasBibliotecaService.class, "Saltando pruebas de devolución porque no se realizó un préstamo válido.");
            }

            // --- Pruebas de Configuración (Admin) ---
            if (admin != null) {
                 LogsError.info(PruebasBibliotecaService.class, "\n--- Pruebas de Configuración (Admin) ---");
                 // 6. Actualizar Política de Préstamo para Alumnos
                 LogsError.info(PruebasBibliotecaService.class, "\n[Prueba Config 6: Actualizar Política Alumnos]");
                 PoliticasPrestamo politicaAlumnos = bibliotecaService.obtenerTodasLasPoliticasPrestamo().stream()
                                                    .filter(p -> p.getIdTipoUsuario() == 3).findFirst().orElse(null);
                 if (politicaAlumnos != null) {
                     int maxOriginal = politicaAlumnos.getMaxEjemplaresPrestamo();
                     politicaAlumnos.setMaxEjemplaresPrestamo(maxOriginal + 1); // Aumentar en 1
                     try {
                         bibliotecaService.actualizarPoliticaPrestamo(politicaAlumnos);
                         PoliticasPrestamo actualizada = bibliotecaService.obtenerTodasLasPoliticasPrestamo().stream()
                                                    .filter(p -> p.getIdTipoUsuario() == 3).findFirst().orElse(null);
                         if (actualizada != null && actualizada.getMaxEjemplaresPrestamo() == maxOriginal + 1) {
                            LogsError.info(PruebasBibliotecaService.class, "ÉXITO: Política de alumnos actualizada. Nuevo max: " + actualizada.getMaxEjemplaresPrestamo());
                            // Restaurar
                            politicaAlumnos.setMaxEjemplaresPrestamo(maxOriginal);
                            bibliotecaService.actualizarPoliticaPrestamo(politicaAlumnos);
                         } else {
                            LogsError.error(PruebasBibliotecaService.class, "FALLO: No se actualizó la política de alumnos correctamente.");
                         }
                     } catch (BibliotecaException | SQLException e) {
                        LogsError.error(PruebasBibliotecaService.class, "Error actualizando política: " + e.getMessage(), e);
                     }
                 }
            }


        } catch (Exception e) { // Captura general para errores no esperados
            LogsError.fatal(PruebasBibliotecaService.class, "ERROR INESPERADO EN PRUEBAS DE BIBLIOTECA: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            LogsError.info(PruebasBibliotecaService.class, "\n=== FIN DE PRUEBAS DEL SERVICIO DE BIBLIOTECA ===");
        }
    }
}