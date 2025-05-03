## Contenido
Esta carpeta contiene todas las clases Java que implementan la lógica de negocio:
- Modelos de datos
- Clases DAO (Data Access Objects)
- Servicios de negocio
- Utilidades

## Dependencias
- Java JDK 8 o superior
- MySQL Connector/J 8.0.x
- [Otras bibliotecas necesarias]

## Estructura de paquetes
- `modelos`: Clases que representan las entidades del sistema
- `dao`: Clases para acceso a datos
- `servicios`: Implementación de la lógica de negocio
- `utilidades`: Clases de apoyo

## Configuración
1. Añadir el conector JDBC al classpath
2. Configurar el archivo `conexion.properties` con los parámetros: