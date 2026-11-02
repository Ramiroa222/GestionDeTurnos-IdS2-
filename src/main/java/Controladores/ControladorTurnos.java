package Controladores;

import Modelos.Paciente;
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
import java.time.LocalTime;
import java.util.Scanner;

public class ControladorTurnos {
    private final Vista V = new Vista();
    private final Conexion conexionDB = new Conexion(); // Instancia para usar el método getConnection().
    
    private Paciente p = new Paciente();
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
                    guardarTurnoEnHistorialDeTurnosPendientesEnBD();
                    break;
                    
                case 2:
                    //modificarTurno(in);
                    System.out.println("En Desarrollo.");
                    break;
                    
                default:
                    System.out.println("Opcion Invalida. Ingrese Un Numero Entero Entre 0 y 2.");
            }
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
        p.setId(buscarPacientePorDni());
        
        // 2. Si no existe, darlo de alta mediante procedimiento.
        if(p.getId() == null){
            p.setId(registrarPaciente());
        }
        
        // 3. Registrar el turno vinculado al ID del paciente con estado "Pendiente".
        registrarTurnoEnBD();
    }
    
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
        p.setNombreCompleto(V.obtenerEntradaNoVacia(in, "Ingresar Nombre Completo Del Paciente: "));
        p.setDni(V.leerDni(in, "Ingresar DNI: "));
        p.setNumeroDeTelefono(V.leerTelefono(in, "Ingresar Numero De Telefono: "));
        p.setObraSocial(V.obtenerEntradaNoVacia(in, "Ingresar Obra Social: "));
    }
    
    /**
     * Utiliza la funcion: f_verificar_superposicion_turno.
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
     * Utiliza la funcion: f_buscar_paciente_por_dni.
     */
    private Integer buscarPacientePorDni() {
        String sql = "SELECT f_buscar_paciente_por_dni(?)";
    
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
        
            pstmt.setString(1, p.getDni());
        
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
     * Utiliza el procedimiento: sp_registrar_paciente.
     */
    private Integer registrarPaciente() {
        String sql = "{CALL sp_registrar_paciente(?, ?, ?, ?, ?)}";
        
        try(Connection con = conexionDB.getConnection();
            CallableStatement cstmt = con.prepareCall(sql)){
            
            cstmt.setString(1, p.getNombreCompleto());
            cstmt.setString(2, p.getDni());
            cstmt.setString(3, p.getNumeroDeTelefono());
            cstmt.setString(4, p.getObraSocial());
            cstmt.registerOutParameter(5, Types.INTEGER); // Parámetro OUT p_nuevo_id.
            
            cstmt.executeUpdate();
            
            return cstmt.getInt(5); // Retorna el ID generado.
            
        }catch(SQLException e){
            System.out.println("Error al registrar paciente: " + e.getMessage());
            return null;
        }
    }
    
    private void registrarTurnoEnBD() {
        String sql = "INSERT INTO Turno (motivo, fecha, horario_inicio, horario_fin, paciente_id, estado) VALUES (?, ?, ?, ?, ?, 'Pendiente')";
        
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
            
            pstmt.setString(1, t.getMotivo());
            pstmt.setObject(2, t.getFecha());
            pstmt.setObject(3, t.getHorarioInicio());
            pstmt.setObject(4, t.getHorarioFin());
            pstmt.setInt(5, p.getId());
        
            pstmt.executeUpdate();
            
            // CAPTURAMOS EL ID AUTOGENERADO DEL TURNO
            try(ResultSet generatedKeys = pstmt.getGeneratedKeys()){
                if(generatedKeys.next()){
                    t.setId(generatedKeys.getInt(1));
                }
            }
            System.out.println("Operación exitosa. Turno agendado\n");
        }catch(SQLException e){
            System.out.println("Error al registrar el turno en la base de datos: " + e.getMessage());
        }
    }
    
    private void guardarTurnoEnHistorialDeTurnosPendientesEnBD() {
        String sql = "INSERT INTO HistorialDeTurnosPendientes (turno_id) VALUES (?)";
        
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
            
            pstmt.setInt(1, t.getId());
            pstmt.executeUpdate();
        }catch(SQLException e){
            System.out.println("Error al guardar en el historial de turnos pendientes: " + e.getMessage());
        }
    }
    
    /*
     * ========================================================================
     *                     METODOS PARA MODIFICAR UN TURNO
     * ========================================================================
     */
    
}
