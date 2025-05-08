/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.excepciones;

/**
 *
 * @author jerson_ramos
 */
public class UsuarioException extends Exception {
    
    public UsuarioException (String message) {
        super(message);// Llama al constructor de la clase padre (Exceptio
    }

    public UsuarioException(String message, Throwable cause) {
        super(message, cause); // Llama al constructor de la clase padre (Exception)
    }
    
}
