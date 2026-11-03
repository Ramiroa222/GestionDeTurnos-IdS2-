package Controladores;

import Servicios.Conexion;

/**
 * Clase principal que contiene el punto de entrada de la aplicación.
 * Da inicio al Gestor de Turnos de un Consultorio Medico.
 */
public class GestionDeTurnos {
    
    /**
     * Método principal (main) que arranca el programa.
     * @param args Argumentos de la línea de comandos (no se utilizan en este proyecto).
     */
    public static void main(String[] args) {
        ControladorBase cB = new ControladorBase();
        Conexion c = new Conexion();
        
        //Se ejecuta este metodo para configurar la conexion a la base de datos en MySQL.
        c.verificarYConfigurarCredenciales();
        
        //Inicia el flujo principal de la aplicación.
        cB.iniciar();
    }
}
