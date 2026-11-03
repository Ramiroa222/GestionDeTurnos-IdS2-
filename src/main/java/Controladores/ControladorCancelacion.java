package Controladores;

import Modelos.Turno;
import Servicios.Conexion;
import Vistas.Vista;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ControladorCancelacion {
    private final Vista V = new Vista();
    private final Conexion conexionDB = new Conexion();
    private Turno t = new Turno();
    
    public void iniciar(Scanner in) {
        int opcion = -1;
        
        do{            
            opcion = V.leerEntero(in, V.menuDeCancelacionDeTurnos()+ "Ingresar Opcion: ");
            
            switch(opcion){
                case 0:
                    System.out.println("\nVolviendo Al Menu Principal...");

                    try{
                        Thread.sleep(700); // Pausa el programa por 700 milisegundos.
                    }catch(InterruptedException e){
                        Thread.currentThread().interrupt();
                    }
                    
                    break;
                    
                case 1:
                    cancelarTurno(in);
                    break;
                       
                default:
                    System.out.println("Opcion Invalida. Ingrese Un Numero Entero Entre 0 y 1.");
            }
            
            // Vacia la clase Turno.
            t = new Turno();
        }while(opcion != 0);
    }
    
    private void cancelarTurno(Scanner in) {
        System.out.println("\n--- CANCELAR TURNO ---");
        
        // 1. El actor ingresa el DNI del paciente (validado a través de la vista)
        String dni = V.leerDni(in, "Ingrese el DNI del paciente: ");
        
        // 2. Obtener todos los turnos pendientes de ese paciente
        List<Turno> turnosPendientes = obtenerTurnosPendientesPorDni(dni);
        
        if (turnosPendientes.isEmpty()) {
            System.out.println("\n[AVISO] No existe ningún turno pendiente asociado al DNI ingresado.");
            System.out.println("Caso de uso finalizado.\n");
            return;
        }
        
        // 3. Evaluar la cantidad de turnos pendientes
        if (turnosPendientes.size() == 1) {
            t = turnosPendientes.get(0);
            System.out.println("Turno unico encontrado para este paciente.");
        } else {
            // Si tiene más de 1 turno, se le muestra la lista y se le pide especificar cuál cancelar
            System.out.println("\nEl paciente tiene " + turnosPendientes.size() + " turnos pendientes:");
            for (int i = 0; i < turnosPendientes.size(); i++) {
                Turno tu = turnosPendientes.get(i);
                System.out.println((i + 1) + ". Fecha: " + tu.getFecha() + " | Horario: " + tu.getHorarioInicio() + " - " + tu.getHorarioFin() + " | Motivo: " + tu.getMotivo());
            }
            
            System.out.println("\nPor favor, indique los datos del turno que desea cancelar:");
            LocalDate fechaBuscada = V.leerFecha(in, "Ingrese la fecha del turno (dd/MM/yyyy): ");
            LocalTime horaBuscada = V.leerHora(in, "Ingrese el horario de inicio del turno (HH:mm): ");
            
            Turno turnoSeleccionado = null;
            for (Turno tu : turnosPendientes) {
                if (tu.getFecha().equals(fechaBuscada) && tu.getHorarioInicio().equals(horaBuscada)) {
                    turnoSeleccionado = tu;
                    break;
                }
            }
            
            if (turnoSeleccionado == null) {
                System.out.println("\n[ERROR] No se encontro ningun turno pendiente con esa fecha y horario para el DNI ingresado.");
                System.out.println("Caso de uso finalizado.\n");
                return;
            }
            
            t = turnoSeleccionado;
        }
        
        // 4. Valida que la cancelación se realice con 24 hs o más de anticipación
        LocalDateTime turnoDateTime = LocalDateTime.of(t.getFecha(), t.getHorarioInicio());
        LocalDateTime ahora = LocalDateTime.now();
        
        long horasDeAnticipacion = ChronoUnit.HOURS.between(ahora, turnoDateTime);
        
        if (horasDeAnticipacion < 24 || turnoDateTime.isBefore(ahora)) {
            System.out.println("\n[ERROR] No es posible realizar la cancelación con menos de 24 hs de anticipación.");
            System.out.println("Caso de uso finalizado.\n");
            return;
        }
        
        // 5. Actualiza el estado a "Cancelado" y libera el bloque horario
        if (ejecutarCancelacionEnBD(t.getId())) {
            System.out.println("Exito. El turno ha sido cancelado y el bloque horario se encuentra liberado.\n");
        } else {
            System.out.println("Error al procesar la cancelación en la base de datos.\n");
        }
    }
    
    /**
     * Consulta y devuelve una lista con todos los turnos pendientes asociados a un DNI.
     */
    private List<Turno> obtenerTurnosPendientesPorDni(String dni) {
        List<Turno> listaTurnos = new ArrayList<>();
        String sql = "SELECT t.id, t.fecha, t.horario_inicio, t.horario_fin, t.motivo " +
                     "FROM Turno t " +
                     "JOIN Paciente p ON t.paciente_id = p.id " +
                     "JOIN HistorialDeTurnosPendientes h ON t.id = h.turno_id " +
                     "WHERE p.dni = ? AND t.estado = 'Pendiente' " +
                     "ORDER BY t.fecha ASC, t.horario_inicio ASC";
        
        try (Connection con = conexionDB.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, dni);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Turno turn = new Turno();
                    turn.setId(rs.getInt("id"));
                    turn.setFecha(rs.getObject("fecha", LocalDate.class));
                    turn.setHorarioInicio(rs.getObject("horario_inicio", LocalTime.class));
                    turn.setHorarioFin(rs.getObject("horario_fin", LocalTime.class));
                    turn.setMotivo(rs.getString("motivo"));
                    listaTurnos.add(turn);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener los turnos pendientes: " + e.getMessage());
        }
        return listaTurnos;
    }
    
    /**
     * Ejecuta el procedimiento almacenado para cancelar el turno y retirarlo del historial.
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