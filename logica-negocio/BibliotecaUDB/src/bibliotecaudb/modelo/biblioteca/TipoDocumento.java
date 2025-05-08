/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bibliotecaudb.modelo.biblioteca;

/**
 *
 * @author jerson_ramos
 */
//modelo para la tabla 'tipo_documento'
public class TipoDocumento {
    
    private int id;
    private String tipo;
    
    //constructor vacio
    public TipoDocumento(){
        
    }
    
    //constructor con parametros
     public TipoDocumento (int id, String tipo){
         this.id = id;
         this.tipo = tipo;
     }
     
     //getters y setters
     public int getId(){
         return id; 
           
     }
     public String getTipo(){
         return tipo;
     }
     
     public void setTipo(String tipo){
         this.tipo = tipo;
     }
     
     @Override
     public String toString(){
           return "TipoDocumento{" + "id=" + id + ", tipo='" + tipo + '\'' + '}'; 
     }

    public void setId(int aInt) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}