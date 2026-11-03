package Controladores;

import Modelos.Turno; // Importa el modelo de datos que representa un turno médico.
import Servicios.Conexion; // Importa el servicio para gestionar la conexión a la base de datos.
import Vistas.Vista; // Importa la clase encargada de la interacción por consola con el usuario.

import java.util.Scanner; // Permite capturar la entrada de datos ingresados por el usuario.
import java.sql.Connection; // Representa una sesión activa de conexión con la base de datos.
import java.sql.PreparedStatement; // Permite ejecutar sentencias SQL precompiladas de forma segura.
import java.sql.ResultSet; // Almacena y recorre los registros devueltos por una consulta SQL.
import java.sql.SQLException; // Maneja las excepciones relacionadas con errores de SQL y la BD.
import java.sql.CallableStatement; // Permite invocar procedimientos almacenados en la base de datos.
import java.time.LocalDateTime; // Maneja fechas y horas combinadas de forma precisa.
import java.time.LocalDate; // Maneja fechas independientes (año, mes, día).
import java.time.LocalTime; // Maneja horas independientes (hora, minuto, segundo).
import java.time.temporal.ChronoUnit; // Utilizado para calcular la diferencia de tiempo (ej. horas) entre fechas.
import java.util.ArrayList; // Implementación de listas dinámicas.
import java.util.List; // Interfaz para el manejo de colecciones de objetos en forma de lista.

/**
 * Clase ControladorCancelacion: Actúa como controlador (según el patrón MVC). 
 * Gestiona la lógica de negocio, el menú interactivo y la validación de reglas 
 * estrictas para la cancelación de turnos médicos.
 */
public class ControladorCancelacion {
    // --- VARIABLES DE INSTANCIA --- //
    
    /** Instancia de la Vista para interactuar con la consola y mostrar u obtener datos. */
    private final Vista V = new Vista();
    
    /** Instancia del servicio de conexión para obtener accesos activos a la base de datos. */
    private final Conexion conexionDB = new Conexion();
    
    /**
     * Controla el bucle principal del menú de cancelación de turnos, mostrando las opciones
     * disponibles y delegando la ejecución según la opción seleccionada por la secretaria.
     * 
     * @param in Objeto Scanner compartido para la lectura de datos por consola.
     */
    public void iniciar(Scanner in) {
        int opcion = -1;
        
        do{            
            opcion = V.leerEntero(in, V.menuDeCancelacionDeTurnos()+ "Ingresar Opcion: ");
            
            switch(opcion){
                case 0 -> {
                    System.out.println("\nVolviendo Al Menu Principal...");

                    try{
                        Thread.sleep(700); // Pausa el programa por 700 milisegundos.
                    }catch(InterruptedException e){
                        Thread.currentThread().interrupt(); // Restaura la interrupción del hilo en caso de error.
                    } // Vuelve al menu principal.
                }
                case 1 -> cancelarTurno(in); // Ejecuta el flujo para cancelar un turno existente.
                default -> System.out.println("Opcion Invalida. Ingrese Un Numero Entero Entre 0 y 1.");
            }
        }while(opcion != 0);
    }
    
    /**
     * Metodo cancelarTurno: Solicita el DNI, busca turnos pendientes, valida si
     * hay uno o múltiples turnos, comprueba la regla de anticipación de 24 horas 
     * y efectúa la baja en la base de datos.
     * 
     * @param in Objeto Scanner para la entrada de datos por consola.
     */
    private void cancelarTurno(Scanner in) {
        System.out.println("\n--- CANCELAR TURNO ---");
        
        // 1. El usuario ingresa el DNI del paciente (validado a través de la vista).
        String dni = V.leerDni(in, "Ingrese el DNI del paciente: ");
        
        // 2. Obtiene todos los turnos pendientes asociados a ese DNI mediante una consulta a la BD.
        List<Turno> turnosPendientes = obtenerTurnosPendientesPorDni(dni);
        
        if (turnosPendientes.isEmpty()) {
            System.out.println("\n[AVISO] No existe ningún turno pendiente asociado al DNI ingresado.");
            return;
        }
        
        // 3. Selecciona el turno a cancelar (automático si es único, o manual si hay varios).
        Turno turnoSeleccionado = seleccionarTurnoACancelar(in, turnosPendientes);
        if (turnoSeleccionado == null) {
            return; // Finaliza si no se pudo determinar el turno.
        }
        
        // 4. Valida que la cancelación se realice con 24 hs o más de anticipación.
        LocalDateTime turnoDateTime = LocalDateTime.of(turnoSeleccionado.getFecha(), turnoSeleccionado.getHorarioInicio());
        LocalDateTime ahora = LocalDateTime.now();
        long horasDeAnticipacion = ChronoUnit.HOURS.between(ahora, turnoDateTime);
        
        if(horasDeAnticipacion < 24 || turnoDateTime.isBefore(ahora)){
            System.out.println("\n[ERROR] No es posible realizar la cancelación con menos de 24 hs de anticipación.");
            return;
        }
        
        // 5. Actualiza el estado a "Cancelado" y libera el bloque horario en la base de datos.
        if(ejecutarCancelacionEnBD(turnoSeleccionado.getId())){
            System.out.println("Exito. El turno ha sido cancelado y el bloque horario se encuentra liberado.\n");
        }else{
            System.out.println("Error al procesar la cancelación en la base de datos.\n");
        }
    }
    
    /**
     * Método seleccionarTurnoACancelar: Sirve para seleccionar un turno cuando el 
     * paciente posee múltiples turnos pendientes.
     * 
     * @param in Objeto Scanner para la lectura de la consola.
     * @param turnosPendientes Lista de turnos pendientes encontrados para el paciente.
     * @return Turno El turno seleccionado por el operador, o null si la selección falla.
     */
    private Turno seleccionarTurnoACancelar(Scanner in, List<Turno> turnosPendientes) {
        if(turnosPendientes.size() == 1){
            System.out.println("Turno único encontrado para este paciente.");
            return turnosPendientes.get(0);
        }
        
        System.out.println("\nEl paciente tiene " + turnosPendientes.size() + " turnos pendientes:");
        for(int i = 0; i < turnosPendientes.size(); i++){
            Turno tu = turnosPendientes.get(i);
            System.out.println((i + 1) + ". Fecha: " + tu.getFecha() + " | Horario: " + tu.getHorarioInicio() + " - " + tu.getHorarioFin() + " | Motivo: " + tu.getMotivo());
        }
        
        System.out.println("\nPor favor, indique los datos del turno que desea cancelar:");
        LocalDate fechaBuscada = V.leerFecha(in, "Ingrese la fecha del turno (dd/MM/yyyy): ");
        LocalTime horaBuscada = V.leerHora(in, "Ingrese el horario de inicio del turno (HH:mm): ");
        
        for(Turno tu : turnosPendientes){
            if(tu.getFecha().equals(fechaBuscada) && tu.getHorarioInicio().equals(horaBuscada)){
                return tu;
            }
        }
        
        System.out.println("\n[ERROR] No se encontró ningún turno pendiente con esa fecha y horario para el DNI ingresado.");
        System.out.println("Caso de uso finalizado.\n");
        return null;
    }
    
    /**
     * Metodo obtenerTurnosPendientesPorDni: Consulta y devuelve una lista con todos 
     * los turnos pendientes asociados a un DNI de paciente, realizando un JOIN entre 
     * las tablas Paciente, Turno y HistorialDeTurnosPendientes.
     * 
     * @param dni Número de documento de identidad del paciente a consultar.
     * @return List<Turno> Lista de objetos Turno encontrados en estado pendiente.
     */
    private List<Turno> obtenerTurnosPendientesPorDni(String dni) {
        List<Turno> listaTurnos = new ArrayList<>();
        
        String sql = "SELECT t.id, t.fecha, t.horario_inicio, t.horario_fin, t.motivo " +
                     "FROM Turno t " +
                     "JOIN Paciente p ON t.paciente_id = p.id " +
                     "JOIN HistorialDeTurnosPendientes h ON t.id = h.turno_id " +
                     "WHERE p.dni = ? AND t.estado = 'Pendiente' " +
                     "ORDER BY t.fecha ASC, t.horario_inicio ASC";
        
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
            
            pstmt.setString(1, dni);
            
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    Turno turn = new Turno();
                    turn.setId(rs.getInt("id"));
                    turn.setFecha(rs.getObject("fecha", LocalDate.class));
                    turn.setHorarioInicio(rs.getObject("horario_inicio", LocalTime.class));
                    turn.setHorarioFin(rs.getObject("horario_fin", LocalTime.class));
                    turn.setMotivo(rs.getString("motivo"));
                    listaTurnos.add(turn);
                }
            }
        }catch(SQLException e){
            System.out.println("Error al obtener los turnos pendientes: " + e.getMessage());
        }
        
        return listaTurnos;
    }
    
    /**
     * Metodo ejecutarCancelacionEnBD: Ejecuta el procedimiento almacenado en 
     * MySQL para cancelar formalmente un turno (cambiando su estado) y 
     * retirarlo de la tabla de historial para liberar el bloque horario.
     * 
     * @param turnoId ID único del turno que se desea cancelar.
     * @return boolean Retorna true si la operación se ejecutó correctamente, o false si ocurrió un error.
     */
    private boolean ejecutarCancelacionEnBD(int turnoId) {
        String sql = "{CALL sp_cancelar_turno(?)}";
        
        try(Connection con = conexionDB.getConnection();
            CallableStatement cstmt = con.prepareCall(sql)){
            
            cstmt.setInt(1, turnoId);
            cstmt.executeUpdate();
            
            return true;
        }catch(SQLException e){
            System.out.println("Error en la base de datos al cancelar el turno: " + e.getMessage());
            return false;
        }
    }
}