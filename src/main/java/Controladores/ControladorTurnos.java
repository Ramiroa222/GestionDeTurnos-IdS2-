package Controladores;

import Modelos.Turno;
import Vistas.Vista;
import Servicios.*;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Scanner;

public class ControladorTurnos {
    private final Vista V = new Vista();
    private final ActualizadorDeTurnosService actualizadorService = new ActualizadorDeTurnosService();
    private final ControladorPacientes  cp = new ControladorPacientes();
    private final Conexion conexionDB = new Conexion(); // Instancia para usar el método getConnection().
    
    private Turno t = new Turno();
    
    public void iniciar(Scanner in) {
        int opcion = -1;
        
        do{
            // Sincroniza automáticamente los turnos pasados.
            actualizadorService.actualizarTurnosVencidos();
            
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
        
        // 1. Busca si el paciente ya existe en la base de datos.
        t.getPaciente().setId(cp.buscarPacientePorDni(t));
        
        // 2. Si no existe, se lo da de alta. Sino se actualiza el nombre completo, 
        // el numero de telefono y la obra social.
        if(t.getPaciente().getId() == null){
            t.getPaciente().setId(cp.registrarPaciente(t));
        }else{
            cp.actualizarPaciente(t);
        }
        
        // 3. Registra en la base de datos el turno vinculado al ID del paciente 
        // con estado "Pendiente".
        registrarTurnoEnBD();
    }
    
    private void ingresarDatos(Scanner in) {
        // Datos Del Turno.
        t.setMotivo(V.obtenerEntradaNoVacia(in, "Ingresar Motivo: "));
        
        ingresarFechayHorarioParaRegistrar(in);
        
        // Datos Del Paciente.
        t.getPaciente().setNombreCompleto(V.obtenerEntradaNoVacia(in, "\nIngresar Nombre Completo Del Paciente: "));
        t.getPaciente().setDni(V.leerDni(in, "Ingresar DNI: "));
        t.getPaciente().setNumeroDeTelefono(V.leerTelefono(in, "Ingresar Numero De Telefono: "));
        t.getPaciente().setObraSocial(V.obtenerEntradaNoVacia(in, "Ingresar Obra Social: "));
    }
    
    private void ingresarFechayHorarioParaRegistrar(Scanner in) {
        // Bucle para repetir fecha y horario si se superponen con otro turno.
        while(true){
            LocalDate fecha = V.leerFecha(in, "Ingresar Fecha (dd/MM/yyyy): ");
            LocalTime horarioInicio = V.leerHora(in, "Ingresar Horario de Inicio (HH:mm): ");
            LocalTime horarioFin = horarioInicio.plusMinutes(40);
            
            // Evaluamos si es un turno en el pasado o futuro.
            boolean esPasado = LocalDateTime.of(fecha, horarioInicio).isBefore(LocalDateTime.now());
            
            // Seleccionamos la validación correspondiente de forma limpia.
            boolean haySuperposicion = esPasado
                ? existeSuperposicionTurnoPasado(fecha, horarioInicio, horarioFin)
                : existeSuperposicionTurno(fecha, horarioInicio, horarioFin);
            
            if(haySuperposicion){
                System.out.println("\n[AVISO] Ya existe un turno en ese horario o se superpone con otro.");
                System.out.println("Por favor, ingrese otra fecha y horario de inicio.\n");
            }else{
                t.setFecha(fecha);
                t.setHorarioInicio(horarioInicio);
                t.setHorarioFin(horarioFin);
                break; // Horario disponible, sale del bucle.
            }
        }
    }
    
    private boolean existeSuperposicionTurnoPasado(LocalDate fecha, LocalTime inicio, LocalTime fin) {
        String sql = "SELECT f_verificar_superposicion_turno_pasado(?, ?, ?)";
        return verificarSuperposicionBD(sql, fecha, inicio, fin, " pasada");
    }
    
    private boolean existeSuperposicionTurno(LocalDate fecha, LocalTime inicio, LocalTime fin) {
        String sql = "SELECT f_verificar_superposicion_turno(?, ?, ?)";
        return verificarSuperposicionBD(sql, fecha, inicio, fin, "");
    }
    
    /**
     * Método auxiliar privado (Principio DRY): Centraliza la lógica repetitiva de conexión y consulta JDBC.
     */
    private boolean verificarSuperposicionBD(String sql, LocalDate fecha, LocalTime inicio, LocalTime fin, String tipo) {
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
        
            pstmt.setObject(1, fecha);
            pstmt.setObject(2, inicio);
            pstmt.setObject(3, fin);
        
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    return rs.getBoolean(1); 
                }
            }
            return true; // Seguridad por defecto si el ResultSet está vacío.
        }catch(SQLException e){
            System.out.println("Error al verificar superposición" + tipo + ": " + e.getMessage());
            return true; // Seguridad ante fallos de BD.
        }
    }
    
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
            System.out.println("Operacion exitosa. Turno agendado correctamente.\n");

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
        
        // 1. Solicita fecha y hora para identificar el turno a modificar.
        LocalDate fechaBusqueda = V.leerFecha(in, "Ingrese la fecha del turno a modificar (dd/MM/yyyy): ");
        LocalTime horaBusqueda = V.leerHora(in, "Ingrese el horario de inicio del turno (HH:mm): ");
        
        // Buscar el ID del turno en la base de datos.
        Integer turnoId = buscarTurnoPorFechaYHora(fechaBusqueda, horaBusqueda);
        
        if (turnoId == null) {
            System.out.println("No se encontro ningun turno pendiente con esa fecha y horario.\n");
            return;
        }
        
        System.out.println("Turno encontrado con exito\n");
        
        // 2. Modificación del Motivo (interactiva).
        if (confirmarAccion(in, "Desea Modificar el Motivo del Turno?")) {
            String nuevoMotivo = V.obtenerEntradaNoVacia(in, "Ingresar Nuevo Motivo: ");
            actualizarAtributoTurnoEnBD("motivo", nuevoMotivo, turnoId);
        }
        
        // 3. Modificación de Fecha y Horario (interactiva).
        if (confirmarAccion(in, "Desea Modificar la Fecha y Horario del Turno?")) {
            boolean esPasado = LocalDateTime.of(fechaBusqueda, horaBusqueda).isBefore(LocalDateTime.now());
            
            ingresarFechaYHorarioParaModificacion(in, turnoId, esPasado);
            actualizarFechaYHoraEnBD(turnoId);
        }
    }
    
    /**
     * Bucle encapsulado para ingresar fecha y hora válidas sin superposiciones (excluyendo el turno actual).
     */
    private void ingresarFechaYHorarioParaModificacion(Scanner in, int turnoId, boolean eraPasado) {
        while(true){
            LocalDate fecha = V.leerFecha(in, "Ingresar Nueva Fecha (dd/MM/yyyy): ");
            LocalTime horarioInicio = V.leerHora(in, "Ingresar Nuevo Horario de Inicio (HH:mm): ");
            LocalTime horarioFin = horarioInicio.plusMinutes(40);
            
            boolean esPasadoActual = LocalDateTime.of(fecha, horarioInicio).isBefore(LocalDateTime.now());
            
            // Selecciona la función SQL adecuada dinámicamente
            String sql = esPasadoActual 
                ? "SELECT f_verificar_superposicion_turno_pasado_excluyendo(?, ?, ?, ?)"
                : "SELECT f_verificar_superposicion_turno_excluyendo(?, ?, ?, ?)";
                
            boolean haySuperposicion = verificarSuperposicionExcluyendoBD(sql, fecha, horarioInicio, horarioFin, turnoId);
            
            if(haySuperposicion){
                System.out.println("\n[AVISO] Ya existe un turno en ese horario o se superpone con otro.");
                System.out.println("Por favor, ingrese otra fecha y horario de inicio.\n");
            }else{
                // Si el turno era pasado y ahora se programa a futuro, lo reactivamos en el historial
                if(eraPasado && !esPasadoActual){
                    actualizarHistorialDeTurnosPendientes(turnoId);
                }
                
                t.setFecha(fecha);
                t.setHorarioInicio(horarioInicio);
                t.setHorarioFin(horarioFin);
                break; 
            }
        }
    }
    
    /**
     * Reincorpora un turno modificado (que pasó de pasado a futuro) al historial de pendientes
     * y actualiza su estado en la base de datos.
     */
    private void actualizarHistorialDeTurnosPendientes(int turnoId) {
        String sql = "{CALL sp_activar_turno_pendiente(?)}";
        
        try(Connection con = conexionDB.getConnection();
            CallableStatement cstmt = con.prepareCall(sql)){
            
            cstmt.setInt(1, turnoId);
            cstmt.executeUpdate();
            
        }catch(SQLException e){
            System.out.println("Error al actualizar el historial de turnos pendientes: " + e.getMessage());
        }
    }
    
    /**
     * Método auxiliar de apoyo para preguntas de Sí/No (Evita repetir bucles de validación de texto).
     */
    private boolean confirmarAccion(Scanner in, String mensaje) {
        while(true){
            String respuesta = V.obtenerEntradaNoVacia(in, mensaje + " (Si/No): ");
            if("Si".equalsIgnoreCase(respuesta)) return true;
            if("No".equalsIgnoreCase(respuesta)) return false;
            System.out.println("Entrada Invalida. Ingrese Si o No.");
        }
    }
    
    /**
     * Busca el ID de un turno pendiente utilizando su fecha y horario de inicio.
     */
    private Integer buscarTurnoPorFechaYHora(LocalDate fecha, LocalTime hora) {
        String sql = "SELECT f_buscar_turno_por_fecha_hora(?, ?)";
        
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
            
            pstmt.setObject(1, fecha);
            pstmt.setObject(2, hora);
            
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    int id = rs.getInt(1);
                    return rs.wasNull() ? null : id;
                }
            }
            
            return null;
        }catch (SQLException e){
            System.out.println("Error al buscar el turno: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Método genérico para actualizar campos simples tipo String (como el motivo).
     */
    private void actualizarAtributoTurnoEnBD(String columna, String valor, int turnoId) {
        String sql = "UPDATE Turno SET " + columna + " = ? WHERE id = ?";
        
        try(Connection con = conexionDB.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)){
            
            pstmt.setString(1, valor);
            pstmt.setInt(2, turnoId);
            
            pstmt.executeUpdate();
            System.out.println(columna.substring(0, 1).toUpperCase() + columna.substring(1) + " actualizado con exito!\n");
            
        }catch (SQLException e){
            System.out.println("Error al actualizar el " + columna + ": " + e.getMessage());
        }
    }
    
    /**
     * Actualiza la fecha y los horarios de inicio/fin del turno en la base de datos.
     */
    private void actualizarFechaYHoraEnBD(int turnoId) {
        String sql = "UPDATE Turno SET fecha = ?, horario_inicio = ?, horario_fin = ? WHERE id = ?";
        
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
            
            pstmt.setObject(1, t.getFecha());
            pstmt.setObject(2, t.getHorarioInicio());
            pstmt.setObject(3, t.getHorarioFin());
            pstmt.setInt(4, turnoId);
            
            pstmt.executeUpdate();
            System.out.println("Fecha y horario modificados con exito.\n");
            
        }catch (SQLException e){
            System.out.println("Error al actualizar la fecha y horario: " + e.getMessage());
        }
    }
    
    /**
     * Método unificado (Principio DRY) para verificar superposiciones excluyendo ID.
     */
    private boolean verificarSuperposicionExcluyendoBD(String sql, LocalDate fecha, LocalTime inicio, LocalTime fin, int turnoIdExcluir) {
        try(Connection con = conexionDB.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)){
        
            pstmt.setObject(1, fecha);
            pstmt.setObject(2, inicio);
            pstmt.setObject(3, fin);
            pstmt.setInt(4, turnoIdExcluir);
        
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    return rs.getBoolean(1);
                }
            }
            return true;
        }catch(SQLException e){
            System.out.println("Error al verificar superposición: " + e.getMessage());
            return true;
        }
    }
}