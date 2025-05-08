# Guía para GUI: Cómo Usar la Lógica de Usuarios (`ServicioUsuario`)

## La Clase Clave: `ServicioUsuario`

Toda la funcionalidad relacionada con usuarios (iniciar sesión, crear cuentas, etc.) se maneja a través de una única clase: `ServicioUsuario`.

* **Ubicación:** `edu.udb.biblioteca.servicio.ServicioUsuario` (o `bibliotecaudb.servicio.ServicioUsuario` si ese es el paquete base).

## Obtener una Instancia del Servicio

En el código de sus ventanas , lo primero que necesitan es crear un objeto de tipo `ServicioUsuario`:

```java
// No olviden el import al principio de su archivo .java:

// import bibliotecaudb.servicio.ServicioUsuario; 
// import bibliotecaudb.modelo.usuario.Usuario; // Para recibir el resultado del login/registro
// import bibliotecaudb.excepciones.UsuarioException; // Para manejar errores
// import bibliotecaudb.conexion.LogsError; // Para loguear errores inesperados
// import javax.swing.JOptionPane; // Para mostrar mensajes

ServicioUsuario servicio = new ServicioUsuario(); 