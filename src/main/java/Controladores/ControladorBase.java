package Controladores;

import Servicios.ActualizadorDeTurnosService;
import java.util.Scanner;

import Vistas.Vista;

public class ControladorBase {
    public void iniciar() {
        Scanner in = new Scanner(System.in);
        Vista V = new Vista();
        ActualizadorDeTurnosService actualizadorService = new ActualizadorDeTurnosService();
        ControladorTurnos c1 = new ControladorTurnos();
        // ControladorConsultas c2 = new ControladorConsultas();
        ControladorCancelacion c3 = new ControladorCancelacion();
        // ReporteTurnosPDF s1 = new ReporteTurnosPDF();
        
        int opcion = -1;
        
        do{
            // Sincroniza automáticamente los turnos pasados.
            actualizadorService.actualizarTurnosVencidos();
            
            opcion = V.leerEntero(in, V.menuPrincipal() + "Ingresar Opcion: ");
            
            switch(opcion){
                case 0:
                    System.out.println("\nSaliendo Del Programa...");

                    try{
                        Thread.sleep(1500); // Pausa el programa por 1,5 segundos.
                    }catch(InterruptedException e){
                        Thread.currentThread().interrupt();
                    }
                    
                    System.out.println("Hasta Pronto!");
                    
                    System.exit(0);
                    break;
                    
                case 1:
                    c1.iniciar(in);
                    break;
                    
                case 2:
                    //c2.iniciar();
                    System.out.println("En desarrollo");
                    break;
                    
                case 3:
                    c3.iniciar(in);
                    break;
                    
                case 4:
                    //s1.iniciar();
                    System.out.println("En desarrollo");
                    break;
                    
                default:
                    System.out.println("Opcion Invalida. Ingrese Un Numero Entero Entre 0 y 4.");
            }
        }while(opcion != 0);
        
        in.close();
    }
}
