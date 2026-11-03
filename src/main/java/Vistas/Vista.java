package Vistas;

// Permite manejar los días de la semana (Lunes a Domingo) para validar restricciones de atención (ej. excluir fines de semana).
import java.time.DayOfWeek;

// Representa una fecha específica (año, mes, día) sin zona horaria, utilizada para la gestión de días de turnos.
import java.time.LocalDate;

// Representa una hora, minuto y segundo exactos, utilizada para controlar los horarios de inicio y fin de cada turno.
import java.time.LocalTime;

// Permite dar formato y analizar (parsear) cadenas de texto hacia objetos de fecha y hora según un patrón personalizado (ej. dd/MM/yyyy).
import java.time.format.DateTimeFormatter;

// Clase estándar de utilidad para capturar y leer las entradas de texto y números introducidas por el usuario en la consola.
import java.util.Scanner;

/**
 * Clase Vista:
 * Actúa como la capa de vista (según el patrón MVC) encargada de gestionar toda la interacción
 * visual por consola con el usuario. Proveerá tanto la validación y captura de entradas (enteros, 
 * textos, DNI, teléfonos, fechas y horarios) como la estructuración textual de los distintos menús del sistema.
 */
public class Vista {
    /**
     * Solicita y lee un número entero ingresado por el usuario.
     * Maneja excepciones en caso de que el usuario ingrese un tipo de dato incorrecto (ej. letras).
     * 
     * @param in Objeto Scanner utilizado para la lectura por consola.
     * @param mensaje Mensaje que se mostrará al usuario para pedir el dato.
     * @return El número entero ingresado y validado.
     */
    public int leerEntero(Scanner in, String mensaje) {
        while(true){
            try{
                System.out.print(mensaje);
                int numero = Integer.parseInt(in.nextLine());
                
                if(numero > -1) return numero;
                else System.out.println("Error: Debe ingresar un numero entero y mayor a -1.");
            }catch(NumberFormatException e){
                System.out.println("Error: Por favor, ingrese un numero entero valido.");
            }
        }
    }
    
    /**
     * Solicita una cadena de texto al usuario y asegura que la entrada no esté vacía ni 
     * compuesta únicamente por espacios en blanco.
     * 
     * @param in Objeto Scanner utilizado para la lectura por consola.
     * @param mensaje Mensaje que se mostrará al usuario para pedir el dato.
     * @return La cadena de texto ingresada por el usuario (sin espacios redundantes en los extremos).
     */
    public String obtenerEntradaNoVacia(Scanner in, String mensaje) {
        String entrada;
        
        do{
            System.out.print(mensaje);
            entrada = in.nextLine().trim(); // Se eliminan espacios al principio y final.
            
            if(entrada.isEmpty()){
                System.out.println("[Alerta] No Puede Estar Vacio.");
            }
        }while(entrada.isEmpty());
        
        return entrada;
    }
    
    /**
     * Método auxiliar para leer y validar un DNI.
     * Este método solicita al usuario un DNI y valida que:
     * - Tenga una longitud válida (entre 7 y 9 dígitos).
     * - Contenga únicamente números (elimina automáticamente puntos o espacios si se ingresan).
     * En caso de error, muestra un mensaje informativo y solicita nuevamente la entrada.
     * @param in Objeto Scanner para la lectura de la consola.
     * @param mensaje Mensaje personalizado a mostrar al usuario.
     * @return String con el DNI validado.
     */
    public String leerDni(Scanner in, String mensaje) {
        while(true){
            try{
                System.out.print(mensaje);
                String dniStr = in.nextLine().trim();
                
                // Limpiamos puntos o espacios por si el usuario escribe ej: "40.123.456"
                dniStr = dniStr.replace(".", "").replace(" ", "");
                
                // Validamos que sean exactamente 7 u 9 dígitos numéricos
                if(dniStr.matches("\\d{7,9}")){
                    return dniStr;
                }else{
                    System.out.println("DNI invalido. Debe contener entre 7 y 9 digitos numericos.");
                }
            }catch(Exception e){
                System.out.println("Entrada invalida. Intente nuevamente.");
            }
        }
    }
    
    /**
     * Método auxiliar para leer y validar un número de teléfono.
     * Este método solicita al usuario un teléfono y valida que:
     * - Contenga únicamente caracteres válidos (números, espacios, guiones, paréntesis y el signo +).
     * - Tenga una cantidad lógica de dígitos (entre 7 y 15 dígitos en total).
     * En caso de error, muestra un mensaje informativo y solicita nuevamente la entrada.
     * @param in Objeto Scanner para la lectura de la consola.
     * @param mensaje Mensaje personalizado a mostrar al usuario.
     * @return String con el teléfono validado.
     */
    public String leerTelefono(Scanner in, String mensaje) {
        while(true){
            try{
                System.out.print(mensaje);
                String telefonoStr = in.nextLine().trim();
                
                // Extraemos solo los dígitos numéricos para verificar la longitud real
                String soloDigitos = telefonoStr.replaceAll("\\D", "");
                
                // Validamos caracteres permitidos y longitud de dígitos (mínimo 7, máximo 15)
                if(telefonoStr.matches("^[+]?[0-9\\s\\-\\(\\)]+$") && soloDigitos.length() >= 7 && soloDigitos.length() <= 15){
                    return telefonoStr;
                }else{
                    System.out.println("Telefono invalido. Use un formato valido (ej. 11 1234-5678 o +54 9 ...).");
                }
            }catch(Exception e){
                System.out.println("Entrada invalida. Intente nuevamente.");
            }
        }
    }
    
    /**
     * Método auxiliar para leer una fecha con validación.
     * Valida formato dd/MM/yyyy y que sea de Lunes a Viernes.
     * @param in Objeto Scanner para la lectura de la consola.
     * @param mensaje Mensaje personalizado a mostrar al usuario.
     * @return LocalDate Objeto de fecha validado de lunes a viernes.
     */
    public LocalDate leerFecha(Scanner in, String mensaje) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        while(true){
            try{
                System.out.print(mensaje);
                String fechaStr = in.nextLine();
                LocalDate fecha = LocalDate.parse(fechaStr, formatter);
                
                // Validación: Debe ser de Lunes a Viernes.
                if(fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY){
                    System.out.println("Dia invalido. Los turnos solo se otorgan de Lunes a Viernes. Intente nuevamente.");
                    continue;
                }
                
                return fecha;
            }catch(Exception e){
                System.out.println("Formato invalido. Use el formato dd/MM/aaaa (ej. 15/10/1990).");
            }
        }
    }
    
    /**
     * Método auxiliar para leer un horario con validación.
     * Valida formato HH:mm y rango entre 08:00 y 19:20 hs.
     * @param in Objeto Scanner para la lectura de la consola.
     * @param mensaje Mensaje personalizado a mostrar al usuario.
     * @return LocalTime Objeto de hora validado dentro del rango horario permitido.
     */
    public LocalTime leerHora(Scanner in, String mensaje) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime horaMinima = LocalTime.of(8, 0);
        LocalTime horaMaxima = LocalTime.of(19, 20);
        
        while(true){
            try{
                System.out.print(mensaje);
                String horaStr = in.nextLine();
                LocalTime horarioInicio = LocalTime.parse(horaStr, formatter);
                
                // Validación: El horario debe estar entre las 08:00 y las 19:20 hs.
                if(horarioInicio.isBefore(horaMinima) || horarioInicio.isAfter(horaMaxima)){
                    System.out.println("Horario invalido. Debe estar comprendido entre las 08:00 y las 19:20 hs.");
                    continue;
                }
                
                return horarioInicio;
            }catch(Exception e){
                System.out.println("Formato invalido. Use el formato HH:mm (ej. 14:30).");
            }
        }
    }    
    
    /**
     * Genera y retorna la estructura en texto formateado del menú principal de la aplicación,
     * listando todas las opciones generales de navegación para el operador.
     * 
     * @return String Bloque de texto con el diseño del menú principal.
     */
    public String menuPrincipal() {
        return """
               
               ================================ GESTOR DE TURNOS ================================
               Bienvenido Al Gestor De Turnos.
               
               Seleccione Una Opcion Para Continuar:
               [0] Salir Del Programa.
               [1] Gestionar Turnos (Contiene Registrar Y Modificar Turnos).
               [2] Consultar Turnos y Pacientes (Acceder A Las Diferentes Opciones De Consulta).
               [3] Cancelar Un Turno Registrado.
               [4] Generar Reporte En PDF.
               ==================================================================================
               """;
    }
    
    /**
     * Genera y retorna la estructura en texto formateado del submenú de gestión de turnos,
     * ofreciendo las opciones para registrar o modificar turnos existentes.
     * 
     * @return String Bloque de texto con el diseño del menú de turnos.
     */
    public String menuDeTurnos() {
        return """
               
               ================= GESTION DE TURNOS =================
               Seleccione Una Opcion Para Continuar:
               [0] Volver Al Menu Principal.
               [1] Registrar Turno.
               [2] Modificar Un Turno Registrado.
               =====================================================
               """;
    }
    
    /**
     * Genera y retorna la estructura en texto formateado del submenú de consultas,
     * mostrando las alternativas para revisar agendas por fecha, pendientes,
     * turnos de pacientes, todos los turnos existentes o revisar 
     * pacientes (todos o 1 en especifico).
     * 
     * @return String Bloque de texto con el diseño del menú de consultas.
     */
    public String menuDeConsultas() {
        return """
               
               =================== CONSULTAS ===================
               Seleccione Una Opcion Para Continuar:
               [0] Volver Al Menu Principal.
               [1] Consultar Agenda.
               [2] Consultar Turnos Pendientes.
               [3] Consultar Turnos De Un Paciente.
               [4] Consultar Todos Turnos Existentes.
               [5] Consultar Pacientes.
               [6] Buscar Paciente Especifico (Por DNI).
               =================================================
               """;
    }
    
    /**
     * Genera y retorna la estructura en texto formateado del submenú de cancelación de turnos,
     * presentando la opción para proceder con la baja de un turno agendado.
     * 
     * @return String Bloque de texto con el diseño del menú de cancelación.
     */
    public String menuDeCancelacionDeTurnos() {
        return """
               
               ================ CANCELACION DE TURNOS ================
               Seleccione Una Opcion Para Continuar:
               [0] Volver Al Menu Principal.
               [1] Cancelar Un Turno Registrado.
               =======================================================
               """;
    }
}