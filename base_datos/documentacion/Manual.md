## Contenido
Esta carpeta contiene todos los elementos relacionados con la base de datos del sistema:
- Diagramas Entidad-Relación
- Scripts SQL para creación de tablas
- Scripts para datos iniciales
- Procedimientos almacenados

## Configuración necesaria
1. Instalar MySQL 5.7 o superior
2. Configurar usuario con permisos:
   - Usuario: bibliotecauser
   - Contraseña: [definir contraseña]
   - Permisos: ALL PRIVILEGES en la base de datos 'biblioteca_db'

## Estructura de la base de datos
- **Usuarios**: Almacena información de administradores, profesores y alumnos
- **Ejemplares**: Catálogo de materiales (libros, revistas, CD, etc.)
- **Prestamos**: Registro de préstamos activos
- **Devoluciones**: Historial de devoluciones
- **Configuracion**: Parámetros del sistema (tasas de mora, límites)

## Instalación
1. Crear la base de datos ejecutando `scripts/ddl/crear_db.sql`
2. Crear las tablas con `scripts/ddl/crear_tablas.sql`
3. Cargar datos iniciales con `scripts/dml/datos_iniciales.sql`
4. Crear procedimientos almacenados con `scripts/procedimientos/procedimientos.sql`S