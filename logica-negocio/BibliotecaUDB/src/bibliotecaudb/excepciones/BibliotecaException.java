/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
*/

package bibliotecaudb.excepciones; // O edu.udb.biblioteca.excepciones

public class BibliotecaException extends Exception {

    public BibliotecaException(String message) {
        super(message);
    }

    public BibliotecaException(String message, Throwable cause) {
        super(message, cause);
    }
}