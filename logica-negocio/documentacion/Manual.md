# PARTE 1
## Capa de Conexión:

- Desarrollar ConexionBD.java - gestión centralizada de conexiones a la base de datos
- Implementar LogsError.java - sistema de registro de errores y operaciones


## Clases de Usuario:

- Clase Usuario.java - modelo para usuarios del sistema
- Clase TipoUsuario.java - modelos para tipos de usuario (Administrador, Profesor, Alumno)


## Acceso a datos de usuarios:

- Implementar UsuarioDAO.java - operaciones CRUD para usuarios
- Implementar TipoUsuarioDAO.java - operaciones para tipos de usuario


## Lógica de autenticación:
 
### Desarrollar ServicioUsuario.java con:

- Login de usuarios
- Validación de credenciales
- Gestión de privilegios según tipo de usuario
- Restablecimiento de contraseñas
- Registro de nuevos usuarios

## Excepciones de Usuario:

- Implementar UsuarioException.java - manejo de errores específicos de autenticación
--- 
# Parte 2

## Clases de Modelo para documentos y préstamos:

- Documento.java - información de libros, revistas, etc.
- TipoDocumento.java - tipos (libro, revista, tesis, etc.)
- Ejemplar.java - instancias físicas de documentos
- Prestamo.java - registro de préstamos
- Devolucion.java - registro de devoluciones
- ConfiguracionSistema.java - parámetros configurables


## Acceso a datos (DAOs):

- DocumentoDAO.java - operaciones CRUD para documentos
- TipoDocumentoDAO.java - operaciones para tipos de documento
- EjemplarDAO.java - gestión de ejemplares físicos
- PrestamoDAO.java - registro y consulta de préstamos
- DevolucionDAO.java - registro y consulta de devoluciones
- ConfiguracionSistemaDAO.java - gestión de parámetros configurables
  
## Lógica de negocio de biblioteca:

### ServicioBiblioteca.java con:

- Gestión de préstamos y validaciones
- Cálculo de moras
- Proceso de devolución
- Consultas de disponibilidad
- Reportes de préstamos activos

## Excepciones de biblioteca:

- BibliotecaException.java - manejo de errores específicos de la lógica de préstamos

> NOTA:
> Los dos usaremos la misma clase de conexión que yo desarrollaré. No es necesario que crees tus propias conexiones.
Para verificar préstamos o moras, tu código llamará a métodos de ServicioBiblioteca que yo desarrollare.
Para verificar privilegios de usuario, utilizarás métodos de ServicioUsuario que yo implementaré.
