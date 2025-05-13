package bibliotecaudb.pruebas;

import bibliotecaudb.conexion.ConexionBD;
import bibliotecaudb.conexion.LogsError;
import bibliotecaudb.modelo.biblioteca.*;
import bibliotecaudb.modelo.usuario.Usuario;
import bibliotecaudb.modelo.usuario.TipoUsuario;
import bibliotecaudb.servicio.biblioteca.GestionDocumentosServicio;
import bibliotecaudb.servicio.biblioteca.GestionPrestamosServicio;
import bibliotecaudb.servicio.biblioteca.ConfiguracionBibliotecaServicio;
import bibliotecaudb.servicio.ServicioUsuario;

// DAOs que podríamos necesitar para setup/verificación directa en pruebas
import bibliotecaudb.dao.biblioteca.EjemplarDAO;
import bibliotecaudb.dao.biblioteca.PrestamoDAO;
import bibliotecaudb.dao.usuario.TipoUsuarioDAO;

import bibliotecaudb.dao.biblioteca.TipoDocumentoDAO; // Si necesitas crear tipos para prueba

import java.math.BigDecimal;
import java.sql.Connection; // <--- IMPORTAR CONNECTION
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class PruebasModuloBiblioteca {

    // Instancias de DAO que podrías necesitar para setup directo en pruebas
    // (Los servicios ya usan sus propias instancias internas de DAO)
    private static EjemplarDAO ejemplarDAO_test = new EjemplarDAO();
    private static PrestamoDAO prestamoDAO_test = new PrestamoDAO();
    private static TipoDocumentoDAO tipoDocumentoDAO_test = new TipoDocumentoDAO();


    public static void main(String[] args) {
        // Instanciar Servicios
        ServicioUsuario usuarioService = new ServicioUsuario();
        GestionDocumentosServicio docServicio = new GestionDocumentosServicio();
        GestionPrestamosServicio prestamoServicio = new GestionPrestamosServicio();
        ConfiguracionBibliotecaServicio configServicio = new ConfiguracionBibliotecaServicio();

        Usuario admin = null;
        Usuario profesor = null;
        Usuario alumno = null;

        System.out.println("--- INICIANDO PRUEBAS MÓDULO BIBLIOTECA ---");

        try {
            Connection connTest = ConexionBD.getConexion(); // Ahora Connection es reconocido
            if (connTest == null || connTest.isClosed()) {
                System.err.println("FALLO CRÍTICO: No se pudo conectar a la BD. Terminando pruebas.");
                return;
            }
            System.out.println("Conexión a BD establecida para pruebas de biblioteca.");

            admin = usuarioService.login("admin@udb.com", "AdminUdb2025.");
            profesor = usuarioService.login("profesor1@udb.com", "ProfesorUDB2025.");
            // Asumiendo que la contraseña de alumno1 fue cambiada en pruebas anteriores
            alumno = usuarioService.login("alumno1@udb.com", "PassCambiado123");


            if (admin == null) {
                System.err.println("ERROR: Usuario Admin no encontrado. Verifica credenciales/existencia.");
                // Podrías decidir detener las pruebas si el admin es crucial
            }
            if (profesor == null) {
                System.err.println("ERROR: Usuario Profesor no encontrado.");
            }
            if (alumno == null) {
                System.err.println("ERROR: Usuario Alumno no encontrado.");
            }

            System.out.println("Usuarios de prueba obtenidos (o intentados).");


            // --- PRUEBAS GestionDocumentosServicio ---
            System.out.println("\n--- Pruebas GestionDocumentosServicio ---");
            Documento docPruebaServicios = null; // Renombrar para evitar confusión con variables de DAO
            Ejemplar ejemPruebaServicios = null;

            if (admin != null) {
                System.out.println("\n[A. Registrar Nuevo Documento con Ejemplares (Admin)]");
                Documento nuevoDoc = new Documento();
                nuevoDoc.setTitulo("Microservicios con Spring Boot");
                nuevoDoc.setAutor("Carlos Santana");
                nuevoDoc.setEditorial("Alfaomega Ra-Ma");
                nuevoDoc.setAnioPublicacion(LocalDate.now().getYear());
                // Asumimos tipoDocumentoId = 1 (Libro) existe
                TipoDocumento tipoLibro = tipoDocumentoDAO_test.obtenerPorId(1); // Obtener para asegurar que existe
                if (tipoLibro != null) {
                    try {
                        // Pasamos el ID del tipo, el servicio lo buscará internamente
                        docPruebaServicios = docServicio.registrarNuevoDocumentoConEjemplares(nuevoDoc, tipoLibro.getId(), 2, "Estante M1", admin);
                        System.out.println("  -> Documento CREADO: " + docPruebaServicios.getTitulo() + " (ID: " + docPruebaServicios.getId() + ") con 2 ejemplares.");

                        List<Ejemplar> ejemplaresDelDoc = docServicio.obtenerEjemplaresDeDocumento(docPruebaServicios.getId());
                        System.out.println("  -> Ejemplares encontrados para Doc ID " + docPruebaServicios.getId() + ": " + ejemplaresDelDoc.size());
                        if (!ejemplaresDelDoc.isEmpty()) {
                            ejemPruebaServicios = ejemplaresDelDoc.get(0);
                            System.out.println("     Primer ejemplar ID: " + ejemPruebaServicios.getId() + ", Ubicación: " + ejemPruebaServicios.getUbicacion() + ", Estado: " + ejemPruebaServicios.getEstado());
                        }
                    } catch (Exception e) {
                        System.err.println("  ERROR al registrar documento: " + e.getMessage());
                    }
                } else {
                    System.err.println("  ERROR: TipoDocumento con ID 1 (Libro) no encontrado para prueba A.");
                }
            } else { System.out.println("  SALTANDO prueba A: Usuario Admin no disponible."); }

            System.out.println("\n[B. Buscar Documentos]");
            try {
                List<Documento> docsEncontrados = docServicio.buscarDocumentos("Java"); // Usar criterio existente
                System.out.println("  Documentos encontrados con 'Java' (" + docsEncontrados.size() + "):");
                for(Documento d : docsEncontrados) {
                     System.out.println("    - " + d.getTitulo() + " (ID: " + d.getId() + ")");
                }
            } catch (Exception e) { System.err.println("  ERROR al buscar documentos: " + e.getMessage()); }


            // --- PRUEBAS ConfiguracionBibliotecaServicio ---
            System.out.println("\n--- Pruebas ConfiguracionBibliotecaServicio ---");
            if (admin != null) {
                System.out.println("\n[C. Configuración del Sistema (Admin)]");
                try {
                    ConfiguracionSistema config = configServicio.obtenerConfiguracionActual();
                    System.out.println("  Configuración actual: MaxEjemplaresGeneral=" + config.getMaximoEjemplares() + ", MoraDiariaGeneral=" + config.getMoraDiaria());

                    // Usaremos los límites fijos definidos en GestionPrestamosServicio para las pruebas de préstamo,
                    // pero podemos probar actualizar la mora general del sistema.
                    int anioActual = LocalDate.now().getYear();
                    boolean actualizoMoraAnio = configServicio.establecerOActualizarMoraAnual(anioActual, new BigDecimal("0.30"), admin); // 0.30
                    System.out.println("  Actualización Mora para año " + anioActual + " a 0.30: " + (actualizoMoraAnio ? "ÉXITO" : "FALLO"));

                    MoraAnual moraTestAnio = configServicio.obtenerMoraAnual(anioActual);
                    System.out.println("  Mora para año " + anioActual + " (leída): " + (moraTestAnio != null ? moraTestAnio.getMoraDiaria() : "No definida"));

                } catch (Exception e) { System.err.println("  ERROR en pruebas de configuración: " + e.getMessage()); }
            } else { System.out.println("  SALTANDO prueba C: Usuario Admin no disponible."); }


            // --- PRUEBAS GestionPrestamosServicio ---
            System.out.println("\n--- Pruebas GestionPrestamosServicio ---");
            Prestamo prestamoActivoAlumnoPrueba = null;

            // D. Escenario Éxito Préstamo (Alumno)
            if (alumno != null && ejemPruebaServicios != null) {
                System.out.println("\n[D. Préstamo Exitoso Alumno]");
                try {
                    // Asegurarnos que el ejemplar esté disponible (el servicio lo hace, pero aquí forzamos para la prueba)
                    ejemplarDAO_test.actualizarEstado(ejemPruebaServicios.getId(), Ejemplar.EstadoEjemplar.DISPONIBLE);
                    System.out.println("  Intentando prestar Ejemplar ID: " + ejemPruebaServicios.getId() + " a Alumno ID: " + alumno.getId());
                    prestamoActivoAlumnoPrueba = prestamoServicio.realizarPrestamo(alumno.getId(), ejemPruebaServicios.getId());
                    System.out.println("  -> Préstamo REALIZADO: ID " + prestamoActivoAlumnoPrueba.getId() + ", Fecha Límite: " + prestamoActivoAlumnoPrueba.getFechaLimite());
                    Ejemplar ejemDespuesPrestamo = ejemplarDAO_test.obtenerPorId(ejemPruebaServicios.getId());
                    System.out.println("     Estado del ejemplar después del préstamo: " + (ejemDespuesPrestamo != null ? ejemDespuesPrestamo.getEstado() : "No encontrado"));
                } catch (Exception e) { System.err.println("  ERROR en préstamo exitoso alumno: " + e.getMessage()); e.printStackTrace(); }
            } else { System.out.println("  SALTANDO prueba D: Alumno o Ejemplar de prueba no disponibles."); }

            // E. Escenario Límite de Préstamos (Alumno)
            // Para probar esto, necesitaríamos prestar N-1 ejemplares al alumno y luego intentar el N-ésimo,
            // donde N es el límite para Alumno (MAX_EJEMPLARES_ALUMNO = 5 en el servicio).
            // Esta prueba se vuelve más compleja de automatizar sin un setup de BD más controlado.
            System.out.println("\n[E. Prueba Límite Préstamos Alumno (Conceptual)]");
            System.out.println("  (Para probar el límite, se necesitaría hacer múltiples préstamos al alumno " +
                               "hasta alcanzar el límite definido en GestionPrestamosServicio y luego intentar uno más).");
            // Si ya tiene un préstamo de la prueba D, intentamos prestarle más
            if (alumno != null && prestamoActivoAlumnoPrueba != null && docPruebaServicios != null) {
                int prestamosHechos = 1;
                int limiteAlumno = 5; // Suponiendo el valor de la constante en el servicio
                System.out.println("  Alumno ya tiene " + prestamosHechos + " préstamo. Límite para Alumno: " + limiteAlumno);

                List<Ejemplar> otrosEjemplares = docServicio.obtenerEjemplaresDeDocumento(docPruebaServicios.getId());
                for (int i = prestamosHechos; i < limiteAlumno; i++) { // Intentar prestar hasta un poco antes del límite
                    Ejemplar ejemplarAdicional = null;
                    for (Ejemplar ej : otrosEjemplares) { // Buscar otro ejemplar disponible
                        if (ej.getId() != ejemPruebaServicios.getId() && ej.getEstado() == Ejemplar.EstadoEjemplar.DISPONIBLE) {
                            boolean yaPrestadoEnEstaPrueba = false;
                            List<Prestamo> activos = prestamoServicio.consultarPrestamosActivosUsuario(alumno.getId());
                            for(Prestamo pa : activos) { if(pa.getEjemplar().getId() == ej.getId()) yaPrestadoEnEstaPrueba = true;}
                            if(!yaPrestadoEnEstaPrueba) {
                                ejemplarAdicional = ej;
                                break;
                            }
                        }
                    }
                    if (ejemplarAdicional != null) {
                        try {
                            System.out.println("  Intentando préstamo adicional ("+(i+1)+") para Alumno ID: " + alumno.getId() + " con Ejemplar ID: " + ejemplarAdicional.getId());
                            prestamoServicio.realizarPrestamo(alumno.getId(), ejemplarAdicional.getId());
                            System.out.println("    -> Préstamo adicional ("+(i+1)+") realizado.");
                        } catch (Exception e) {
                            System.err.println("    ERROR en préstamo adicional ("+(i+1)+") para alumno: " + e.getMessage());
                            break; // Salir si falla un préstamo
                        }
                    } else {
                        System.out.println("  No hay más ejemplares distintos disponibles para probar el límite del alumno.");
                        break;
                    }
                }
                // Intentar el préstamo que excedería el límite
                Ejemplar ejemplarParaExceder = null;
                 for (Ejemplar ej : otrosEjemplares) {
                     if (ej.getEstado() == Ejemplar.EstadoEjemplar.DISPONIBLE) {
                        boolean yaPrestadoEnEstaPrueba = false;
                        List<Prestamo> activos = prestamoServicio.consultarPrestamosActivosUsuario(alumno.getId());
                        for(Prestamo pa : activos) { if(pa.getEjemplar().getId() == ej.getId()) yaPrestadoEnEstaPrueba = true;}
                        if(!yaPrestadoEnEstaPrueba) {
                            ejemplarParaExceder = ej;
                            break;
                        }
                     }
                 }
                if (ejemplarParaExceder != null) {
                     try {
                         System.out.println("  Intentando préstamo que EXCEDERÁ el límite para Alumno ID: " + alumno.getId() + " con Ejemplar ID: " + ejemplarParaExceder.getId());
                         prestamoServicio.realizarPrestamo(alumno.getId(), ejemplarParaExceder.getId());
                         System.err.println("  ERROR: Se permitió prestar más allá del límite del alumno.");
                     } catch (Exception e) {
                         System.out.println("  -> ÉXITO (esperado): No se permitió el préstamo. Mensaje: " + e.getMessage());
                     }
                } else {
                     System.out.println("  No hay un ejemplar disponible para probar exceder el límite del alumno.");
                }
            }


            // F. Escenario Préstamo (Admin)
            // ... (similar a la versión anterior, asegúrate de que haya un ejemplar disponible) ...

            // G. Escenario Devolución (Sin Mora)
            if (prestamoActivoAlumnoPrueba != null) {
                System.out.println("\n[G. Devolución Sin Mora]");
                try {
                    Date fechaDevolucionTemprana = Date.valueOf(LocalDate.now().plusDays(1));
                    System.out.println("  Intentando devolver Préstamo ID: " + prestamoActivoAlumnoPrueba.getId() + " en fecha: " + fechaDevolucionTemprana);
                    Devolucion dev = prestamoServicio.registrarDevolucion(prestamoActivoAlumnoPrueba.getId(), fechaDevolucionTemprana);
                    System.out.println("  -> Devolución REALIZADA: ID " + dev.getId() + ", Mora Pagada: " + dev.getMoraPagada());
                    Ejemplar ejemDespuesDevolucion = ejemplarDAO_test.obtenerPorId(prestamoActivoAlumnoPrueba.getEjemplar().getId());
                    System.out.println("     Estado del ejemplar después de devolución: " + (ejemDespuesDevolucion != null ? ejemDespuesDevolucion.getEstado() : "No encontrado"));
                } catch (Exception e) { System.err.println("  ERROR en devolución sin mora: " + e.getMessage());}
            } else { System.out.println("  SALTANDO prueba G: No hay préstamo activo de alumno para devolver (prestamoActivoAlumnoPrueba es null)."); }


            // H. Escenario Devolución (Con Mora)
            System.out.println("\n[H. Devolución Con Mora]");
            Usuario alumnoParaMora = null;
            TipoUsuario tipoAl = null; // Reusar tipoUsuarioDAO_test o crear una instancia local de TipoUsuarioDAO
            if (new TipoUsuarioDAO().obtenerPorId(3) != null) { // Usar new para no depender de la estática si no la quieres
                 tipoAl = new TipoUsuarioDAO().obtenerPorId(3);
            }


            if (tipoAl != null) {
                // ... (código para crear/obtener alumnoParaMora)
                // ... (código para crear préstamo moroso usando prestamoDAO_test)
                // ... (código para intentar que usuario moroso tome otro libro)
                // ... (código para registrar devolución del préstamo moroso)
            } else { System.out.println("  SALTANDO prueba H: Tipo Alumno (ID 3) no encontrado."); }


        } catch (Exception e) {
            System.err.println("\n!!! ERROR INESPERADO GENERAL DURANTE PRUEBAS BIBLIOTECA: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("\n--- FINALIZANDO PRUEBAS BIBLIOTECA Y CERRANDO CONEXIÓN ---");
            ConexionBD.cerrarConexion();
            System.out.println("-------------------------------------------------------");
        }
    }
}