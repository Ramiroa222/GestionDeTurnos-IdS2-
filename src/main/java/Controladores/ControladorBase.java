package Controladores;

import Servicios.ActualizadorDeTurnosService; // Servicio para sincronizar y actualizar automáticamente el estado de turnos vencidos.
import Servicios.ReportePdfService;           // Servicio encargado de exportar y generar reportes en formato PDF (ej. turnos del día).
import Vistas.Vista;                          // Clase de la capa de vista que gestiona la interacción por consola y los menús.

import java.util.Scanner;                     // Utilidad estándar para capturar las entradas de texto y opciones numéricas del usuario.

/**
 * Clase ControladorBase:
 * Actúa como el controlador principal de la aplicación (siguiendo el patrón MVC). 
 * Se encarga de inicializar los recursos globales del sistema, orquestar el menú principal 
 * de navegación y delegar las operaciones a los controladores específicos de gestión, 
 * consultas, cancelaciones y generación de reportes en PDF.
 */
public class ControladorBase {
    /**
     * Método iniciar:
     * Controla el bucle de ejecución principal del sistema. En cada iteración:
     * 1. Sincroniza automáticamente los turnos vencidos mediante el servicio correspondiente.
     * 2. Despliega el menú principal en consola y captura la opción elegida por el operador.
     * 3. Degela la ejecución hacia el controlador o servicio respectivo (Gestión de turnos, 
     *    Consultas, Cancelaciones o Generación de Reporte PDF de los turnos del día).
     * 4. Gestiona la salida segura del programa al recibir la opción 0.
     */
    public void iniciar() {
        // Inicialización del objeto Scanner para la lectura unificada por consola.
        Scanner in = new Scanner(System.in);
        
        // Instanciación de vistas, servicios y subcontroladores del sistema.
        Vista V = new Vista();
        ActualizadorDeTurnosService actualizadorService = new ActualizadorDeTurnosService();
        ControladorTurnos c1 = new ControladorTurnos();
        ControladorConsultas c2 = new ControladorConsultas();
        ControladorCancelacion c3 = new ControladorCancelacion();
        ReportePdfService reportePdfService = new ReportePdfService();
        
        int opcion = -1;
        
        do{
            // Sincroniza automáticamente los turnos pasados.
            actualizadorService.actualizarTurnosVencidos();
            
            // Muestra el menú principal y lee la opción ingresada por el usuario.
            opcion = V.leerEntero(in, V.menuPrincipal() + "Ingresar Opcion: ");
            
            switch(opcion){
                case 0 -> {
                    System.out.println("\nSaliendo Del Programa...");

                    try{
                        Thread.sleep(1500); // Pausa el hilo de ejecución por 1,5 segundos antes de salir.
                    }catch(InterruptedException e){
                        Thread.currentThread().interrupt(); // Restaura el estado de interrupción en caso de error.
                    }
                    
                    System.out.println("Hasta Pronto!");
                    
                    System.exit(0); // Finaliza la ejecución de la aplicación de manera limpia.
                }
                case 1 -> c1.iniciar(in); // Delega la ejecución al subcontrolador de Gestión de Turnos.
                case 2 -> c2.iniciar(in); // Delega la ejecución al subcontrolador de Consultas.
                case 3 -> c3.iniciar(in); // Delega la ejecución al subcontrolador de Cancelación de Turnos.
                case 4 -> reportePdfService.generarPdfTurnosDelDia(); // Genera directamente el reporte PDF de los turnos del día actual
                default -> System.out.println("Opcion Invalida. Ingrese Un Numero Entero Entre 0 y 4.");
            }
        }while(opcion != 0);
        
        in.close(); // Cierre del flujo de entrada de la consola al finalizar el programa.
    }
}