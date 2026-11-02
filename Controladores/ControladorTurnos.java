package Controladores;

import Modelos.Turno;
import Vistas.Vista;
import Servicios.Conexion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Scanner;

public class ControladorTurnos {
    private final Vista V = new Vista();
    private final Conexion conexionDB = new Conexion(); // Instancia para usar el método getConnection().
    
    private Turno t = new Turno();
    
    public void iniciar(Scanner in) {
        int opcion = -1;
        
        do{
            opcion = V.leerEntero(in, V.menuDeTurnos() + "Ingresar Opcion: ");
            
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
                    registrarTurno(in);
                    break;
                    
                case 2:
                    modificarTurno(in);
                    break;
                    
                default:
                    System.out.println("Opcion Invalida. Ingrese Un Numero Entero Entre 0 y 2.");
            }
            
            // Vacia la clase Turno.
            t = new Turno();
        }while(opcion != 0);
    }
    
    /*
     * ========================================================================
     *                     METODOS PARA REGISTRAR UN TURNO
     * ========================================================================
     */
    private void registrarTurno(Scanner in) {
        System.out.println("\n--- REGISTRO DE NUEVO TURNO ---");
        
        ingresarDatos(in);
        
        // 1. Buscar si el paciente ya existe en la base de datos.
        t.getPaciente().setId(buscarPacientePorDni());
        
        // 2. Si no existe, darlo de alta mediante procedimiento.
        if(t.getPaciente().getId() == null){
            t.getPaciente().setId(registrarPaciente());
        }else{
            actualizarPaciente();
        }
        
        // 3. Registrar el turno vinculado al ID del paciente con estado "Pendiente".
        registrarTurnoEnBD();
    }
    
    // Metodo Auxiliar para registrarTurno.
    private void ingresarDatos(Scanner in) {
        // Datos Del Turno.
        t.setMotivo(V.obtenerEntradaNoVacia(in, "Ingresar Motivo: "));
        
        // Bucle para repetir fecha y horario si se superponen con otro turno.
        while(true){
            LocalDate fecha = V.leerFecha(in, "Ingresar Fecha (dd/MM/yyyy): ");
            LocalTime horarioInicio = V.leerHora(in, "Ingresar Horario de Inicio (HH:mm): ");
            LocalTime horarioFin = horarioInicio.plusMinutes(40);
            
            if(existeSuperposicionTurno(fecha, horarioInicio, horarioFin)){
                System.out.println("\n[AVISO] Ya existe un turno activo en ese horario o se superpone con otro.");
                System.out.println("Por favor, ingrese otra fecha y horario de inicio.\n");
            }else{
                t.setFecha(fecha);
                t.setHorarioInicio(horarioInicio);
                t.setHorarioFin(horarioFin);
                break; // Horario disponible, salimos del bucle.
            }
        }
        
        // Datos Del Paciente.
        t.getPaciente().setNombreCompleto(V.obtenerEntradaNoVacia(in, "Ingresar Nombre Completo Del Paciente: "));
        t.getPaciente().setDni(V.leerDni(in, "Ingresar DNI: "));
        t.getPaciente().setNumeroDeTelefono(V.leerTelefono(in, "Ingresar Numero De Telefono: "));
        t.getPaciente().setObraSocial(V.obtenerEntradaNoVacia(in, "Ingresar Obra Social: "));
    }
    
    /**
     * Metodo Auxiliar para ingresarDatos.
     */
    private boolean existeSuperposicionTurno(LocalDate fecha, LocalTime inicio, LocalTime fin) {
        String sql = "SELECT f_verificar_superposicion_turno(?, ?, ?)";
    
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
        
            pstmt.setObject(1, fecha);
            pstmt.setObject(2, inicio);
            pstmt.setObject(3, fin);
        
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    // getBoolean maneja automáticamente el 1 como true y el 0 como false.
                    return rs.getBoolean(1); 
                }
            }
            
            return true; // Si por alguna razón el ResultSet viene vacío, bloqueamos por seguridad.
        }catch(SQLException e){
            System.out.println("Error al verificar superposición: " + e.getMessage());
            return true; // Tu lógica de seguridad original ante fallos de base de datos.
        }
    }
    
    /**
     * Metodo Auxiliar para registrarTurno.
     */
    private Integer buscarPacientePorDni() {
        String sql = "SELECT f_buscar_paciente_por_dni(?)";
    
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
        
            pstmt.setString(1, t.getPaciente().getDni());
        
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    int id = rs.getInt(1);
                    return rs.wasNull() ? null : id;
                }
            }
            
            return null;
        }catch(SQLException e){
            System.out.println("Error al buscar paciente: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Metodo Secundario para registrarTurno.
     */
    private Integer registrarPaciente() {
        String sql = "{CALL sp_registrar_paciente(?, ?, ?, ?, ?)}";
        
        try(Connection con = conexionDB.getConnection();
            CallableStatement cstmt = con.prepareCall(sql)){
            
            cstmt.setString(1, t.getPaciente().getNombreCompleto());
            cstmt.setString(2, t.getPaciente().getDni());
            cstmt.setString(3, t.getPaciente().getNumeroDeTelefono());
            cstmt.setString(4, t.getPaciente().getObraSocial());
            cstmt.registerOutParameter(5, Types.INTEGER); // Parámetro OUT p_nuevo_id.
            
            cstmt.executeUpdate();
            
            return cstmt.getInt(5); // Retorna el ID generado.
            
        }catch(SQLException e){
            System.out.println("Error al registrar paciente: " + e.getMessage());
            return null;
        }
    }
    
    // Metodo Auxiliar para registrarTurno.
    private void actualizarPaciente() {
        String sql = "{CALL sp_actualizar_paciente(?, ?, ?, ?)}";
        
        try(Connection con = conexionDB.getConnection();
            CallableStatement cstmt = con.prepareCall(sql)){
            
            cstmt.setInt(1, t.getPaciente().getId());
            cstmt.setString(2, t.getPaciente().getNombreCompleto());
            cstmt.setString(3, t.getPaciente().getNumeroDeTelefono());
            cstmt.setString(4, t.getPaciente().getObraSocial());
            
            cstmt.executeUpdate();
            
        }catch(SQLException e){
            System.out.println("Error al actualizar los datos del paciente: " + e.getMessage());
        }
    }
    
    // Metodo Auxiliar para registrarTurno.
    private void registrarTurnoEnBD() {
        String sql = "{CALL sp_registrar_turno(?, ?, ?, ?, ?)}";
        
        try(Connection con = conexionDB.getConnection();
            CallableStatement cstmt = con.prepareCall(sql)){
            
            cstmt.setString(1, t.getMotivo());
            cstmt.setObject(2, t.getFecha());
            cstmt.setObject(3, t.getHorarioInicio());
            cstmt.setObject(4, t.getHorarioFin());
            cstmt.setInt(5, t.getPaciente().getId());

            cstmt.executeUpdate();
            System.out.println("¡Operación exitosa! Turno agendado correctamente.\n");

        }catch(SQLException e){
            System.out.println("Error al registrar el turno en la base de datos: " + e.getMessage());
        }
    }
    
    /*
     * ========================================================================
     *                     METODOS PARA MODIFICAR UN TURNO
     * ========================================================================
     */
    private void modificarTurno(Scanner in) {
        System.out.println("\n--- MODIFICAR TURNO ---");
        
        // 1. Solicitar fecha y hora para identificar el turno a modificar.
        LocalDate fechaBusqueda;
        LocalTime horaBusqueda;
        
        // Bucle para validar que la fecha y hora no sean anteriores a la actual
        while(true){
            fechaBusqueda = V.leerFecha(in, "Ingrese la fecha actual del turno a modificar (dd/MM/yyyy): ");
            horaBusqueda = V.leerHora(in, "Ingrese el horario de inicio actual del turno (HH:mm): ");
            
            LocalDateTime turnoDateTime = LocalDateTime.of(fechaBusqueda, horaBusqueda);
            
            if (turnoDateTime.isBefore(LocalDateTime.now())) {
                System.out.println("\n[ERROR] La fecha y horario ingresados son anteriores al momento actual.");
                System.out.println("Por favor, ingrese una fecha y horario válidos (futuros).\n");
            } else {
                break; // Fecha y hora válidas, salimos del bucle.
            }
        }
        
        // Buscar el ID del turno en la base de datos
        Integer turnoId = buscarTurnoPorFechaYHora(fechaBusqueda, horaBusqueda);
        
        if (turnoId == null) {
            System.out.println("No se encontró ningún turno pendiente con esa fecha y horario.\n");
            return;
        }
        
        System.out.println("¡Turno encontrado con éxito!\n");
        
        // 2. Preguntar interactivamente por la modificación del Motivo.
        while(true){
            String respuestaMotivo = V.obtenerEntradaNoVacia(in, "¿Quiere Modificar el Motivo del Turno? (Si/No): ");
            
            if("Si".equalsIgnoreCase(respuestaMotivo)){
                String nuevoMotivo = V.obtenerEntradaNoVacia(in, "Ingresar Nuevo Motivo: ");
                modificarMotivoEnBD(turnoId, nuevoMotivo);
                break;
            }else if("No".equalsIgnoreCase(respuestaMotivo)){
                break;
            }else{
                System.out.println("Entrada Inválida. Ingrese Si o No.");
            }
        }
        
        // 3. Preguntar interactivamente por la modificación de Fecha y Horario
        while(true){
            String respuestaFechaHora = V.obtenerEntradaNoVacia(in, "¿Quiere Modificar la Fecha y Horario del Turno? (Si/No): ");
            
            if("Si".equalsIgnoreCase(respuestaFechaHora)){
                LocalDate nuevaFecha;
                LocalTime nuevoHorarioInicio;
                LocalTime nuevoHorarioFin;
                
                // Bucle de validación de superposición (excluyendo el turno actual)
                while(true){
                    nuevaFecha = V.leerFecha(in, "Ingresar Nueva Fecha (dd/MM/yyyy): ");
                    nuevoHorarioInicio = V.leerHora(in, "Ingresar Nuevo Horario de Inicio (HH:mm): ");
                    nuevoHorarioFin = nuevoHorarioInicio.plusMinutes(40);
                    
                    if(existeSuperposicionTurnoExcluyendo(nuevaFecha, nuevoHorarioInicio, nuevoHorarioFin, turnoId)){
                        System.out.println("\n[AVISO] El nuevo horario se superpone con otro turno existente.");
                        System.out.println("Por favor, ingrese otra fecha y horario.\n");
                    }else{
                        break;
                    }
                }
                
                modificarFechaYHoraEnBD(turnoId, nuevaFecha, nuevoHorarioInicio, nuevoHorarioFin);
                break;
            }else if("No".equalsIgnoreCase(respuestaFechaHora)){
                break;
            }else{
                System.out.println("Entrada Inválida. Ingrese Si o No.");
            }
        }
    }
    
    /**
     * Busca el ID de un turno pendiente utilizando su fecha y horario de inicio.
     */
    private Integer buscarTurnoPorFechaYHora(LocalDate fecha, LocalTime hora) {
        String sql = "{CALL sp_buscar_turno_por_fecha_hora(?, ?, ?)}";
        
        try (Connection con = conexionDB.getConnection();
             CallableStatement cstmt = con.prepareCall(sql)) {
            
            cstmt.setObject(1, fecha);
            cstmt.setObject(2, hora);
            cstmt.registerOutParameter(3, Types.INTEGER);
            
            cstmt.execute();
            
            int id = cstmt.getInt(3);
            return cstmt.wasNull() ? null : id;
            
        } catch (SQLException e) {
            System.out.println("Error al buscar el turno: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Actualiza únicamente el motivo del turno en la base de datos.
     */
    private void modificarMotivoEnBD(int turnoId, String nuevoMotivo) {
        String sql = "UPDATE Turno SET motivo = ? WHERE id = ?";
        
        try (Connection con = conexionDB.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, nuevoMotivo);
            pstmt.setInt(2, turnoId);
            
            pstmt.executeUpdate();
            System.out.println("¡Motivo actualizado con éxito!\n");
            
        } catch (SQLException e) {
            System.out.println("Error al actualizar el motivo: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza la fecha y los horarios de inicio/fin del turno en la base de datos.
     */
    private void modificarFechaYHoraEnBD(int turnoId, LocalDate nuevaFecha, LocalTime nuevoInicio, LocalTime nuevoFin) {
        String sql = "UPDATE Turno SET fecha = ?, horario_inicio = ?, horario_fin = ? WHERE id = ?";
        
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
            
            pstmt.setObject(1, nuevaFecha);
            pstmt.setObject(2, nuevoInicio);
            pstmt.setObject(3, nuevoFin);
            pstmt.setInt(4, turnoId);
            
            pstmt.executeUpdate();
            System.out.println("¡Fecha y horario actualizados con éxito!\n");
            
        } catch (SQLException e) {
            System.out.println("Error al actualizar la fecha y horario: " + e.getMessage());
        }
    }
    
    /**
     * Verifica superposiciones de turnos excluyendo al turno que estamos modificando.
     */
    private boolean existeSuperposicionTurnoExcluyendo(LocalDate fecha, LocalTime inicio, LocalTime fin, int turnoIdExcluir) {
        String sql = "SELECT f_verificar_superposicion_turno_excluyendo(?, ?, ?, ?)";
    
        try (Connection con = conexionDB.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
        
            pstmt.setObject(1, fecha);
            pstmt.setObject(2, inicio);
            pstmt.setObject(3, fin);
            pstmt.setInt(4, turnoIdExcluir);
        
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Error al verificar superposición: " + e.getMessage());
            return true;
        }
    }
}
