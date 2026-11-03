package Controladores;

import Servicios.*;
import Vistas.Vista;
import Modelos.Turno;

import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase ControladorConsultas: Actúa como controlador (según el patrón MVC).
 * Gestiona la lógica de negocio y la interacción por consola para todas las 
 * opciones de consulta del sistema de turnos médicos.
 */
public class ControladorConsultas {
    // --- VARIABLES DE INSTANCIA ---
    /** Instancia de la Vista para mostrar mensajes por consola y leer datos del usuario. */
    private final Vista V = new Vista();
    
    /** Instancia del servicio de conexión para obtener accesos activos a la base de datos. */
    private final Conexion conexionDB = new Conexion();
    
    /** Servicio encargado de actualizar automáticamente el estado de los turnos vencidos. */
    private final ActualizadorDeTurnosService actualizadorService = new ActualizadorDeTurnosService();
    
    // Instancia del nuevo servicio de PDF
    private final ReportePdfService pdfService = new ReportePdfService();
    
    /**
     * Método iniciar: Controla el bucle principal del menú de consultas,
     * mostrando las opciones y delegando la ejecución según la opción 
     * seleccionada por el usuario.
     * 
     * @param in Objeto Scanner compartido para la lectura de datos por consola.
     */
    public void iniciar(Scanner in) {         
        int opcion = -1;
        
        do {
            // Sincroniza automáticamente los turnos pasados.
            actualizadorService.actualizarTurnosVencidos();
            
            opcion = V.leerEntero(in, V.menuDeConsultas() + "Ingresar Opcion: ");
            
            switch(opcion){
                case 0 -> {
                    System.out.println("\nSaliendo Del Programa...");
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("Hasta Pronto!");
                    System.exit(0);
                }
                case 1 -> consultarAgenda(in);
                case 2 -> consultarTurnosPendientes(in);
                case 3 -> consultarTurnosDeUnPaciente(in);
                case 4 -> consultarTodosLosTurnos(in);
                case 5 -> consultarPacientes();
                case 6 -> buscarPacienteEspecifico(in);
                default -> System.out.println("Opcion Invalida. Ingrese Un Numero Entero Entre 0 y 6.");
            }
        } while (opcion != 0);
    }
    
    /*
     * ========================================================================
     *                     METODO PARA CONSULTAR AGENDA
     * ========================================================================
     */
    
    /**
     * Metodo consultarAgenda: Solicita una fecha válida al usuario, consulta los turnos 
     * registrados para ese día ordenados cronológicamente y ofrece la opción de generar un reporte en PDF.
     * 
     * @param in Objeto Scanner para la lectura de datos por consola.
     */
    private void consultarAgenda(Scanner in) {
        System.out.println("\n--- CONSULTAR AGENDA POR FECHA ---");
        LocalDate fechaConsulta = V.leerFecha(in, "Ingrese la fecha a consultar (dd/MM/yyyy): ");
        
        String sql = "SELECT t.id, t.fecha, t.horario_inicio, t.horario_fin, t.motivo, t.estado, " +
                     "p.id AS paciente_id, p.nombre_completo, p.dni, p.numero_de_telefono, p.obra_social " +
                     "FROM Turno t JOIN Paciente p ON t.paciente_id = p.id " +
                     "WHERE t.fecha = ? ORDER BY t.horario_inicio ASC";
                     
        List<Turno> turnosAgenda = ejecutarConsultaTurnos(sql, fechaConsulta);
        
        if (turnosAgenda.isEmpty()) {
            System.out.println("\n[AVISO] No hay turnos asignados para la fecha " + 
                               fechaConsulta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".\n");
            return;
        }
        
        System.out.println("\nAgenda del día " + fechaConsulta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                           " (" + turnosAgenda.size() + " turnos encontrados):");
        mostrarListaTurnos(turnosAgenda);
        
        // Llamada al servicio de PDF si el usuario confirma.
        if(confirmarAccion(in, "Desea generar un archivo PDF con la agenda consultada?")) {
            pdfService.generarPdfAgenda(fechaConsulta, turnosAgenda);
        }
    }
    
    /*
     * ========================================================================
     *                METODO PARA CONSULTAR TURNOS PENDIENTES
     * ========================================================================
     */
    
    /**
     * Metodo consultarTurnosPendientes: Consulta y muestra todos los turnos activos 
     * en estado 'Pendiente' utilizando la vista de MySQL `v_turnos_pendientes`.
     * 
     * @param in Objeto Scanner para la lectura de consola.
     */
    private void consultarTurnosPendientes(Scanner in) {
        System.out.println("\n--- LISTADO DE TURNOS PENDIENTES ---");
        List<Turno> turnos = ejecutarConsultaTurnos("SELECT * FROM v_turnos_pendientes ORDER BY fecha ASC, horario_inicio ASC");
        
        if (turnos.isEmpty()) {
            System.out.println("[AVISO] No hay turnos pendientes registrados en el sistema.\n");
            return;
        }
        mostrarListaTurnos(turnos);
    }
    
    /*
     * ========================================================================
     *                METODO PARA CONSULTAR TURNOS DE UN PACIENTE
     * ========================================================================
     */
    
    /**
     * Metodo consultarTurnosDeUnPaciente: Solicita el DNI del paciente y consulta 
     * todos sus turnos asociados (pendientes, pasados o cancelados) utilizando la vista 
     * de MySQL `v_turnos_por_paciente`.
     * 
     * @param in Objeto Scanner para la lectura de consola.
     */
    /**
     * Solicita el DNI del paciente y consulta todos sus turnos asociados utilizando la vista de MySQL.
     * 
     * @param in Objeto Scanner para la lectura de consola.
     */
    private void consultarTurnosDeUnPaciente(Scanner in) {
        System.out.println("\n--- CONSULTAR TURNOS DE UN PACIENTE ---");
        String dni = V.leerDni(in, "Ingrese el DNI del paciente: ");
        String sql = "SELECT * FROM v_turnos_por_paciente WHERE dni = ? ORDER BY fecha DESC, horario_inicio DESC";
        
        List<Turno> turnos = ejecutarConsultaTurnos(sql, dni);
        
        if (turnos.isEmpty()) {
            System.out.println("\n[AVISO] No se encontraron turnos registrados para el DNI ingresado.\n");
            return;
        }

        System.out.println("\nTurnos encontrados para el DNI " + dni + " (" + turnos.size() + " en total):");
        mostrarListaTurnos(turnos);
    }
    
    /*
     * ========================================================================
     *           METODO PARA CONSULTAR TODOS LOS TURNOS EXISTENTES
     * ========================================================================
     */
    
    /**
     * Metodo consultarTodosLosTurnosExistentes: Muestra el historial completo de todos 
     * los turnos registrados en el consultorio utilizando la vista de MySQL `v_todos_los_turnos`.
     * 
     * @param in Objeto Scanner para la lectura de consola.
     */
    private void consultarTodosLosTurnos(Scanner in) {
        System.out.println("\n--- HISTORIAL COMPLETO DE TURNOS ---");
        List<Turno> turnos = ejecutarConsultaTurnos("SELECT * FROM v_todos_los_turnos ORDER BY fecha DESC, horario_inicio DESC");
        
        if (turnos.isEmpty()) {
            System.out.println("[AVISO] No existen turnos registrados en el sistema.\n");
            return;
        }
        mostrarListaTurnos(turnos);
    }
    
    /*
     * ========================================================================
     *               METODO PARA CONSULTAR TODOS LOS PACIENTES
     * ========================================================================
     */
    
    /**
     * Metodo consultarPacientes: Muestra el listado completo de todos los pacientes 
     * dados de alta en el sistema utilizando la vista de MySQL `v_pacientes`.
     * 
     * @param in Objeto Scanner para la lectura de consola.
     */
    private void consultarPacientes() {
        System.out.println("\n--- LISTADO GENERAL DE PACIENTES ---");
        String sql = "SELECT * FROM v_pacientes ORDER BY nombre_completo ASC";
        
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()){
            
            int contador = 0;
            System.out.println("--------------------------------------------------------------------------------");
            while(rs.next()){
                contador++;
                System.out.println(contador + ". Nombre: " + rs.getString("nombre_completo") + 
                                   " | DNI: " + rs.getString("dni") + 
                                   " | Teléfono: " + rs.getString("numero_de_telefono") + 
                                   " | Obra Social: " + rs.getString("obra_social"));
            }
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Total de pacientes registrados: " + contador + "\n");
            
        }catch(SQLException e){
            System.out.println("Error al consultar el listado de pacientes: " + e.getMessage());
        }
    }
    
    /*
     * ========================================================================
     *         METODO PARA CONSULTAR BUSCAR UN PACIENTE ESPECIFICO
     * ========================================================================
     */
    
    /**
     * Metodo Buscar Paciente Específico (Por DNI): Realiza una búsqueda individual y directa 
     * de un paciente mediante un comando SELECT sobre la tabla `Paciente` de la base de datos.
     * 
     * @param in Objeto Scanner para la lectura de consola.
     */
    private void buscarPacienteEspecifico(Scanner in) {
        System.out.println("\n--- BUSCAR PACIENTE ESPECÍFICO ---");
        String dni = V.leerDni(in, "Ingrese el DNI del paciente a buscar: ");
        String sql = "SELECT id, nombre_completo, dni, numero_de_telefono, obra_social FROM Paciente WHERE dni = ?";
        
        try (Connection con = conexionDB.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, dni);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n[PACIENTE ENCONTRADO]");
                    System.out.println("--------------------------------------------------");
                    System.out.println("ID Sistema  : " + rs.getInt("id"));
                    System.out.println("Nombre      : " + rs.getString("nombre_completo"));
                    System.out.println("DNI         : " + rs.getString("dni"));
                    System.out.println("Teléfono    : " + rs.getString("numero_de_telefono"));
                    System.out.println("Obra Social : " + rs.getString("obra_social"));
                    System.out.println("--------------------------------------------------\n");
                } else {
                    System.out.println("\n[AVISO] No se encontro ningun paciente registrado con el DNI " + dni + ".\n");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar el paciente en la base de datos: " + e.getMessage());
        }
    }
    
    /*
     * ========================================================================
     *                     MÉTODOS AUXILIARES DE APOYO
     * ========================================================================
     */
    
    /**
     * Método centralizado y reutilizable para ejecutar cualquier consulta de turnos en la base de datos.
     * 
     * @param sqlQuery Sentencia SQL o vista a consultar.
     * @param params Parámetros opcionales para la sentencia preparada (ej. fechas, DNI).
     * @return List<Turno> Lista de turnos mapeados.
     */
    private List<Turno> ejecutarConsultaTurnos(String sqlQuery, Object... params) {
        List<Turno> listaTurnos = new ArrayList<>();
        
        try (Connection con = conexionDB.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sqlQuery)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Turno turn = new Turno();
                    turn.setId(rs.getInt("id"));
                    turn.setMotivo(rs.getString("motivo"));
                    turn.setFecha(rs.getObject("fecha", LocalDate.class));
                    turn.setHorarioInicio(rs.getObject("horario_inicio", LocalTime.class));
                    turn.setHorarioFin(rs.getObject("horario_fin", LocalTime.class));
                    turn.setEstado(rs.getString("estado"));
                    
                    turn.getPaciente().setId(rs.getInt("paciente_id"));
                    turn.getPaciente().setNombreCompleto(rs.getString("nombre_completo"));
                    turn.getPaciente().setDni(rs.getString("dni"));
                    turn.getPaciente().setNumeroDeTelefono(rs.getString("numero_de_telefono"));
                    turn.getPaciente().setObraSocial(rs.getString("obra_social"));
                    
                    listaTurnos.add(turn);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en la consulta de turnos: " + e.getMessage());
        }
        
        return listaTurnos;
    }
    
    /**
     * Imprime una lista de turnos formateada en consola.
     */
    private void mostrarListaTurnos(List<Turno> turnos) {
        System.out.println("--------------------------------------------------------------------------------");
        for (int i = 0; i < turnos.size(); i++) {
            System.out.println((i + 1) + ". " + turnos.get(i).toString());
        }
        System.out.println("--------------------------------------------------------------------------------\n");
    }
    
    /**
     * Valida respuestas cerradas de tipo Sí/No de forma interactiva.
     */
    private boolean confirmarAccion(Scanner in, String mensaje) {
        while (true) {
            String respuesta = V.obtenerEntradaNoVacia(in, mensaje + " (Si/No): ");
            if ("Si".equalsIgnoreCase(respuesta)) return true;
            if ("No".equalsIgnoreCase(respuesta)) return false;
            System.out.println("Entrada Invalida. Ingrese Si o No.");
        }
    }
}