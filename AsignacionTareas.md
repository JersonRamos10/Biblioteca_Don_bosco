# Distribución de Tareas: 

## Personas 1 y 2: Capa de Datos (25%)
### Responsabilidades:

Diseño completo de la base de datos
Implementación de scripts SQL

### Tareas concretas:

- Analizar requerimientos de almacenamiento para la biblioteca
- Diseñar tablas para usuarios, documentos, préstamos y configuraciones
- Crear diagrama ER completo

Implementar scripts SQL para:

- Creación de tablas con relaciones
- Inserción de datos de prueba
- Consultas básicas necesarias
- Procedimientos almacenados para operaciones comunes
- Preparación de consultas para reportes específicos (disponibilidad, préstamos activos, etc.)


### Entregables:

- Diagrama ER completo
- Scripts SQL funcionales
- Documentación básica del modelo formato readme. o documento 
--------------------------------------------------------------------------------------------

## Personas 3 y 4: Capa Lógica de Negocios (25%)

### Responsabilidades:

- Implementación de todas las clases del modelo
- Desarrollo de la lógica de negocio
- Conexión a base de datos

#### División sugerida:

Persona 3:

- Clases para Usuarios y autenticación
- Conexión JDBC básica
- Gestión de privilegios
- Manejo de excepciones para autenticación


Persona 4:

- Clases para Documentos y préstamos
- Lógica de préstamos/devoluciones
- Cálculo de moras
- Implementación de logs de errores


### Entregables conjuntos:

- Paquete completo de clases Java
- Implementación funcional de operaciones CRUD
- Sistema de autenticación y control de privilegios
- Documentación con JavaDoc o Readme 
-------------------------------------------------------------------------

## Persona 5: Frontend (10%)
### Responsabilidades:

- Desarrollo de todas las interfaces gráficas
- Conexión con la lógica de negocio
- Validaciones en formularios

### Tareas concretas:

- Interfaces de login y gestión de usuarios
- Pantallas para registro de documentos
- Pantallas de configuracións
- Interfaces de préstamos y devoluciones
- Pantallas de consulta y búsqueda
- Visualización de reportes
- Diseño consistente entre ventanas
- Implementación de mensajes de error para el usuario

### Entregables:

- Todas las interfaces gráficas funcionando
- Navegación entre pantallas
- Validaciones de entrada
- Guía de diseño visual
------------------------------------------------------------------------------
## Funcionamiento de la aplicación sin utilizar el IDE (10%)

## Persona 5:

- Crear script de compilación. 
- Empaquetar la aplicación en JAR ejecutable
- Crear instalador básico
- Verificar dependencias externas
- Probar en diferentes entornos fuera del IDE
- Documentar proceso de instalación y ejecución
- Crear archivo README con instrucciones de instalación y funcionamiento. 