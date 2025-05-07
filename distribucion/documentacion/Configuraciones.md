# Documentacion de Configuraciones 
## Instalaciones de programas necesarios: 

* ** Java JDK**
* **MySQL**
* **IDE:** NetBeans.

## Pasos para correr el programa

## Paso 1: Preparar la de Base de Datos `biblioteca`
1.  **Crear la BD:** Abrir MySQL (Workbench, phpMyAdmin, etc.) y crear una base de datos vacía llamada `biblioteca`.

    ```sql
    CREATE DATABASE IF NOT EXISTS biblioteca;
    ```
    
3.  **Importar el Script Principal:** Buscar el archivo `biblioteca.sql` en el proyecto. Este archivo tiene TODAS las tablas (`usuarios`, `tipo_usuario`, `documentos`, `prestamos`, etc.) y datos iniciales.
    * **¡Acción Clave!:** Ejecutar **todo** el script `biblioteca.sql` en la base de datos `biblioteca`. Usar la opción "Importar" de la herramienta utilizada o ejecútarlo como script.
    * *(El archivo `ConsultasComunes.sql` es solo para ver ejemplos de SQL, no lo ejecutes para configurar).*

## Paso 2: La Conexión Personal (`config.properties`)

Para que el código Java se conecte a **Nuestra** base de datos local, necesitamos decirle cuál es el usuario y contraseña de MySQL o herramienta utilizada.

1.  **Buscar el Archivo:** Dentro del código fuente, ir a `src/bibliotecaudb/conexion/config.properties`.
2.  **Edítalor:** Abrir ese archivo. se vera esto:

     ```properties
    db.url=jdbc:mysql://localhost:3306/biblioteca
    db.user=root 
    db.password=""
    ```

     * **¡Acción Clave!:** Cambia `cambiar las comillas dobles ""` por **la contraseña real** de MySQL o la herramienta usada.
    * Si  se usa un usuario MySQL diferente a `root`, cámbiarlo también en `db.user`.
    * El `db.url` normalmente no se toca.
4.  **¡¡IMPORTANTE!!** Este archivo es **local**. **NO subir ni compartir las contraseñas solo usarlos mientras se trabaje el proyecto.** El código Java está hecho para leer las credenciales desde aquí, **no modifiques el código Java en ninguna clases ** para poner contraseñas.

## Paso 3: Las Librerías y Logs (¡Ya están en el proyecto!)

* **JARs (MySQL Connector y Log4j):** Las librerías que usa el proyecto **ya están incluidas** en la carpeta `lib/`. No hay que descargarlas. el IDE (NetBeans) debería reconocerlas automáticamente al abrir el proyecto.
* **Logs (`log4j.properties`):** Este archivo está en `src/` y controla los mensajes que la aplicación muestra (en consola y en `biblioteca_app.log`). **No se necesita modificarlo.**

Con la base de datos creada desde `biblioteca.sql` y el `config.properties` editado con los datos, ya se puede:
* Compilar el proyecto (desde el IDE).
* Ejecutar las clases de prueba (como `PruebasModuloUsuario.java`) para verificar que todo conecta bien. 
