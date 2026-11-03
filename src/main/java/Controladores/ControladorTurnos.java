package Controladores;

import Modelos.Turno;    // Importa el modelo de datos que representa un turno médico.
import Vistas.Vista;     // Importa la clase encargada de la interacción con la consola (IO).
import Servicios.*;      // Importa los servicios de negocio de la aplicación (ej. ActualizadorDeTurnosService).

import java.sql.CallableStatement;        // Permite ejecutar procedimientos almacenados en la BD.
import java.sql.Connection;               // Representa una sesión de conexión activa con la base de datos.
import java.sql.PreparedStatement;        // Permite ejecutar consultas SQL precompiladas de manera segura.
import java.sql.ResultSet;                // Almacena y recorre los resultados devueltos por una consulta SQL.
import java.sql.SQLException;             // Maneja las excepciones y errores relacionados con JDBC y la BD.
import java.time.LocalDate;               // Maneja fechas (año, mes, día) sin zona horaria.
import java.time.LocalDateTime;           // Maneja fechas y horas combinadas (fecha y hora exacta).
import java.time.LocalTime;               // Maneja horas, minutos y segundos (horarios).
import java.util.Scanner;                 // Permite capturar la entrada del usuario por la consola.

/**
 * Clase ControladorTurnos: Actúa como controlador (según el patrón MVC). Gestiona 
 * la lógica de negocio, el flujo de interacción del menú de turnos y la persistencia 
 * de datos (registro y modificación).
 */
public class ControladorTurnos {
    // --- VARIABLES DE INSTANCIA ---
    
    /** Instancia de la Vista para mostrar mensajes por consola y leer datos del usuario. */
    private final Vista V = new Vista();
    
    /** Servicio encargado de actualizar automáticamente el estado de los turnos vencidos. */
    private final ActualizadorDeTurnosService actualizadorService = new ActualizadorDeTurnosService();
    
    /** Instancia del controlador de pacientes para buscar, registrar o actualizar datos del paciente. */
    private final ControladorPacientes cp = new ControladorPacientes();
    
    /** Instancia de la clase de conexión para obtener accesos activos a la base de datos (getConnection). */
    private final Conexion conexionDB = new Conexion();
    
    /**
     * Método iniciar: Controla el bucle principal del menú de gestión de turnos, 
     * mostrando las opciones y delegando la ejecución según la opción seleccionada 
     * por la secretaria.
     * 
     * @param in Objeto Scanner compartido para la lectura de la consola.
     */
    public void iniciar(Scanner in) {
        int opcion = -1;
        
        do {
            // Sincroniza automáticamente los turnos pasados al inicio de cada iteración del menú.
            actualizadorService.actualizarTurnosVencidos();
            
            opcion = V.leerEntero(in, V.menuDeTurnos() + "Ingresar Opcion: ");
            
            switch(opcion){
                case 0 -> {
                    System.out.println("\nVolviendo Al Menu Principal...");
                    try{
                        Thread.sleep(700);   // Pausa la ejecución del programa por 700 milisegundos.
                    }catch (InterruptedException e){
                        Thread.currentThread().interrupt();   // Restaura la interrupción del hilo en caso de error.
                    }
                }
                case 1 -> registrarTurno(in);  // Llama al flujo de registro de un nuevo turno.
                case 2 -> modificarTurno(in);  // Llama al flujo de modificación de un turno existente.
                default -> System.out.println("Opcion Invalida. Ingrese Un Numero Entero Entre 0 y 2.");
            }
        }while (opcion != 0);
    }
    
    /*
     * ========================================================================
     *                    METODOS PARA REGISTRAR UN TURNO
     * ========================================================================
     */
    
    /**
     * Método registrarTurno: Coordina el flujo completo para dar de alta un nuevo 
     * turno, validando paciente y guardando en BD.
     * 
     * @param in Objeto Scanner para la entrada de datos por consola.
     */
    private void registrarTurno(Scanner in) {
        System.out.println("\n--- REGISTRO DE NUEVO TURNO ---");
        
        Turno t = new Turno();
        ingresarDatos(in, t);  // Captura los datos del turno y del paciente.
        
        // 1. Busca si el paciente ya se encuentra registrado en la base de datos mediante su DNI.
        t.getPaciente().setId(cp.buscarPacientePorDni(t));
        
        // 2. Si el paciente no existe, se lo da de alta. Si ya existe, se actualizan sus datos.
        if(t.getPaciente().getId() == null){
            t.getPaciente().setId(cp.registrarPaciente(t));
        }else{
            cp.actualizarPaciente(t);
        }
        
        // 3. Registra en la base de datos el turno vinculado al ID del paciente con estado por defecto "Pendiente".
        registrarTurnoEnBD(t);
    }
    
    /**
     * Método ingresarDatos: Solicita por consola todos los atributos requeridos 
     * tanto para el turno como para el paciente.
     * 
     * @param in Objeto Scanner para la lectura de datos.
     * @param t Instancia del turno a poblar.
     */
    private void ingresarDatos(Scanner in, Turno t) {
        t.setMotivo(V.obtenerEntradaNoVacia(in, "Ingresar Motivo: "));
        ingresarFechayHorarioParaRegistrar(in, t);
        
        t.getPaciente().setNombreCompleto(V.obtenerEntradaNoVacia(in, "\nIngresar Nombre Completo Del Paciente: "));
        t.getPaciente().setDni(V.leerDni(in, "Ingresar DNI: "));
        t.getPaciente().setNumeroDeTelefono(V.leerTelefono(in, "Ingresar Numero De Telefono: "));
        t.getPaciente().setObraSocial(V.obtenerEntradaNoVacia(in, "Ingresar Obra Social: "));
    }
    
    /**
     * Método ingresarFechayHorarioParaRegistrar: Bucle interactivo que valida 
     * la fecha y hora de un nuevo turno, calculando la duración (40 min) y 
     * previniendo superposiciones con otros turnos activos o pasados.
     * 
     * @param in Objeto Scanner para la lectura de fechas y horas.
     * @param t Instancia del turno al que se le asignará la fecha y horarios.
     */
    private void ingresarFechayHorarioParaRegistrar(Scanner in, Turno t) {
        while(true){
            LocalDate fecha = V.leerFecha(in, "Ingresar Fecha (dd/MM/yyyy): ");
            LocalTime horarioInicio = V.leerHora(in, "Ingresar Horario de Inicio (HH:mm): ");
            LocalTime horarioFin = horarioInicio.plusMinutes(40);
            
            boolean esPasado = LocalDateTime.of(fecha, horarioInicio).isBefore(LocalDateTime.now());
            String sql = esPasado 
                ? "SELECT f_verificar_superposicion_turno_pasado(?, ?, ?)"
                : "SELECT f_verificar_superposicion_turno(?, ?, ?)";
            
            boolean haySuperposicion = consultarBooleano(sql, fecha, horarioInicio, horarioFin);
            
            if(haySuperposicion){
                System.out.println("\n[AVISO] Ya existe un turno en ese horario o se superpone con otro.");
                System.out.println("Por favor, ingrese otra fecha y horario de inicio.\n");
            }else{
                t.setFecha(fecha);
                t.setHorarioInicio(horarioInicio);
                t.setHorarioFin(horarioFin);
                break;
            }
        }
    }
    
    /**
     * Método registrarTurnoEnBD: Ejecuta el procedimiento almacenado en MySQL para 
     * registrar formalmente el turno y asociarlo al historial.
     * 
     * @param t Instancia del turno a registrar.
     */
    private void registrarTurnoEnBD(Turno t) {
        String sql = "{CALL sp_registrar_turno(?, ?, ?, ?, ?)}";
        
        ejecutarTransaccion(sql, cstmt -> {
            cstmt.setString(1, t.getMotivo());
            cstmt.setObject(2, t.getFecha());
            cstmt.setObject(3, t.getHorarioInicio());
            cstmt.setObject(4, t.getHorarioFin());
            cstmt.setInt(5, t.getPaciente().getId());
        }, "Operacion exitosa. Turno agendado correctamente.\n", "Error al registrar el turno en la base de datos");
    }
    
    /*
     * ========================================================================
     *                     METODOS PARA MODIFICAR UN TURNO
     * ========================================================================
     */
    
    /**
     * Método modificarTurno: Orquesta el caso de uso para buscar un turno 
     * pendiente existente por su fecha/hora y permitir la modificación interactiva 
     * de su motivo, fecha u horario.
     * 
     * @param in Objeto Scanner para la entrada de opciones y datos.
     */
    private void modificarTurno(Scanner in) {
        System.out.println("\n--- MODIFICAR TURNO ---");
        
        LocalDate fechaBusqueda = V.leerFecha(in, "Ingrese la fecha del turno a modificar (dd/MM/yyyy): ");
        LocalTime horaBusqueda = V.leerHora(in, "Ingrese el horario de inicio del turno (HH:mm): ");
        
        Integer turnoId = buscarTurnoPorFechaYHora(fechaBusqueda, horaBusqueda);
        
        if(turnoId == null){
            System.out.println("No se encontro ningun turno pendiente con esa fecha y horario.\n");
            return;
        }
        
        System.out.println("Turno encontrado con exito\n");
        
        if(confirmarAccion(in, "Desea Modificar el Motivo del Turno?")){
            String nuevoMotivo = V.obtenerEntradaNoVacia(in, "Ingresar Nuevo Motivo: ");
            actualizarAtributoTurnoEnBD("motivo", nuevoMotivo, turnoId);
        }
        
        if(confirmarAccion(in, "Desea Modificar la Fecha y Horario del Turno?")){
            boolean esPasado = LocalDateTime.of(fechaBusqueda, horaBusqueda).isBefore(LocalDateTime.now());
            Turno tModificado = new Turno();
            
            ingresarFechaYHorarioParaModificacion(in, turnoId, esPasado, tModificado);
            actualizarFechaYHoraEnBD(turnoId, tModificado);
        }
    }
    
    /**
     * Método ingresarFechaYHorarioParaModificacion: Bucle seguro que valida nuevas fechas 
     * y horarios durante la modificación, excluyendo al propio turno del control de 
     * superposiciones y gestionando activaciones de historial.
     * 
     * @param in Objeto Scanner para la lectura de nuevas fechas y horas.
     * @param turnoId ID único del turno que se está modificando.
     * @param eraPasado Indicador booleano de si el turno original se encontraba en el pasado.
     * @param t Instancia temporal para almacenar los nuevos datos validados.
     */
    private void ingresarFechaYHorarioParaModificacion(Scanner in, int turnoId, boolean eraPasado, Turno t) {
        while(true){
            LocalDate fecha = V.leerFecha(in, "Ingresar Nueva Fecha (dd/MM/yyyy): ");
            LocalTime horarioInicio = V.leerHora(in, "Ingresar Nuevo Horario de Inicio (HH:mm): ");
            LocalTime horarioFin = horarioInicio.plusMinutes(40);
            
            boolean esPasadoActual = LocalDateTime.of(fecha, horarioInicio).isBefore(LocalDateTime.now());
            
            String sql = esPasadoActual 
                ? "SELECT f_verificar_superposicion_turno_pasado_excluyendo(?, ?, ?, ?)"
                : "SELECT f_verificar_superposicion_turno_excluyendo(?, ?, ?, ?)";
                
            boolean haySuperposicion = consultarBooleano(sql, fecha, horarioInicio, horarioFin, turnoId);
            
            if(haySuperposicion){
                System.out.println("\n[AVISO] Ya existe un turno en ese horario o se superpone con otro.");
                System.out.println("Por favor, ingrese otra fecha y horario de inicio.\n");
            }else{
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
     * Método actualizarHistorialDeTurnosPendientes: Llama al procedimiento almacenado 
     * para incorporar un turno modificado al historial de pendientes.
     * 
     * @param turnoId ID único del turno a activar.
     */
    private void actualizarHistorialDeTurnosPendientes(int turnoId) {
        String sql = "{CALL sp_activar_turno_pendiente(?)}";
        ejecutarTransaccion(sql, cstmt -> cstmt.setInt(1, turnoId), null, "Error al actualizar el historial de turnos pendientes");
    }
    
    /**
     * Método confirmarAccion: Método auxiliar de apoyo que valida respuestas 
     * cerradas de tipo Sí/No de forma interactiva.
     * 
     * @param in Objeto Scanner para la lectura de la respuesta.
     * @param mensaje Mensaje o pregunta a mostrar al usuario.
     * @return boolean Retorna true si el usuario responde Sí, o false si responde No.
     */
    private boolean confirmarAccion(Scanner in, String mensaje) {
        while(true){
            String respuesta = V.obtenerEntradaNoVacia(in, mensaje + " (Si/No): ");
            if ("Si".equalsIgnoreCase(respuesta)) return true;
            if ("No".equalsIgnoreCase(respuesta)) return false;
            System.out.println("Entrada Invalida. Ingrese Si o No.");
        }
    }
    
    /**
     * Método buscarTurnoPorFechaYHora: Consulta en la base de datos el ID de un 
     * turno pendiente utilizando una función SQL específica.
     * 
     * @param fecha Fecha de búsqueda del turno.
     * @param hora Horario de inicio del turno.
     * @return Integer ID del turno encontrado, o null si no existe coincidencia.
     */
    private Integer buscarTurnoPorFechaYHora(LocalDate fecha, LocalTime hora) {
        String sql = "SELECT f_buscar_turno_por_fecha_hora(?, ?)";
        
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
            
            pstmt.setObject(1, fecha);
            pstmt.setObject(2, hora);
            
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()) {
                    int id = rs.getInt(1);
                    return rs.wasNull() ? null : id;
                }
            }
            
            return null;
        }catch(SQLException e){
            System.out.println("Error al buscar el turno: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Método actualizarAtributoTurnoEnBD: Método genérico encargado de actualizar 
     * atributos individuales de tipo texto (como el motivo) en la tabla Turno.
     * 
     * @param columna Nombre de la columna de la tabla a actualizar.
     * @param valor Nuevo valor de tipo String a asignar.
     * @param turnoId ID único del turno a modificar.
     */
    private void actualizarAtributoTurnoEnBD(String columna, String valor, int turnoId) {
        String sql = "UPDATE Turno SET " + columna + " = ? WHERE id = ?";
        
        ejecutarActualizacion(sql, pstmt -> {
            pstmt.setString(1, valor);
            pstmt.setInt(2, turnoId);
        }, columna.substring(0, 1).toUpperCase() + columna.substring(1) + " actualizado con exito!\n", "Error al actualizar el " + columna);
    }
    
    /**
     * Método actualizarFechaYHoraEnBD: Actualiza de forma atómica la fecha, el horario 
     * de inicio y el horario de fin de un turno en la base de datos.
     * 
     * @param turnoId ID único del turno a modificar.
     * @param t Instancia que contiene los nuevos datos de fecha y hora.
     */
    private void actualizarFechaYHoraEnBD(int turnoId, Turno t) {
        String sql = "UPDATE Turno SET fecha = ?, horario_inicio = ?, horario_fin = ? WHERE id = ?";
        
        ejecutarActualizacion(sql, pstmt -> {
            pstmt.setObject(1, t.getFecha());
            pstmt.setObject(2, t.getHorarioInicio());
            pstmt.setObject(3, t.getHorarioFin());
            pstmt.setInt(4, turnoId);
        }, "Fecha y horario modificados con exito.\n", "Error al actualizar la fecha y horario");
    }
    
    // =========================================================================
    //         MÉTODOS AUXILIARES DE PERSISTENCIA (PRINCIPIO DRY)
    // =========================================================================

    /**
     * Método centralizado para ejecutar consultas booleanas de superposición en la BD.
     * 
     * @param sql Sentencia SQL o función a ejecutar.
     * @param params Arreglo de objetos con los parámetros a inyectar en la sentencia preparada.
     * @return boolean Resultado booleano de la consulta (true por defecto ante fallos).
     */
    private boolean consultarBooleano(String sql, Object... params) {
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
            
            for(int i = 0; i < params.length; i++){
                pstmt.setObject(i + 1, params[i]);
            }
            
            try(ResultSet rs = pstmt.executeQuery()){
                if (rs.next()) return rs.getBoolean(1);
            }
            
            return true;
        }catch(SQLException e){
            System.out.println("Error al verificar superposición: " + e.getMessage());
            return true;
        }
    }

    /**
     * Método centralizado para ejecutar actualizaciones en la BD.
     * 
     * @param sql Sentencia SQL de actualización (UPDATE).
     * @param configurador Interfaz funcional para asignar los parámetros al PreparedStatement.
     * @param mensajeExito Mensaje opcional a imprimir al completarse con éxito.
     * @param mensajeError Mensaje a mostrar si ocurre una excepción SQL.
     */
    private void ejecutarActualizacion(String sql, SQLConsumer<PreparedStatement> configurador, String mensajeExito, String mensajeError) {
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
            configurador.accept(pstmt);
            pstmt.executeUpdate();
            if(mensajeExito != null) System.out.print(mensajeExito);
        }catch(SQLException e){
            System.out.println(mensajeError + ": " + e.getMessage());
        }
    }

    /**
     * Método centralizado para ejecutar transacciones con procedimientos almacenados (CallableStatement).
     * 
     * @param sql Llamada al procedimiento almacenado.
     * @param configurador Interfaz funcional para asignar los parámetros al CallableStatement.
     * @param mensajeExito Mensaje opcional a imprimir al completarse con éxito.
     * @param mensajeError Mensaje a mostrar si ocurre una excepción SQL.
     */
    private void ejecutarTransaccion(String sql, SQLConsumer<CallableStatement> configurador, String mensajeExito, String mensajeError) {
        try(Connection con = conexionDB.getConnection();
            CallableStatement cstmt = con.prepareCall(sql)){
            configurador.accept(cstmt);
            cstmt.executeUpdate();
            
            if (mensajeExito != null) System.out.print(mensajeExito);
        }catch (SQLException e){
            System.out.println(mensajeError + ": " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }
}