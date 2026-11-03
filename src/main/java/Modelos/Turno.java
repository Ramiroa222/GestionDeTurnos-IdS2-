package Modelos;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Clase Turno: Representa el modelo de datos para un turno médico dentro del sistema. 
 * Contiene la información temporal, el motivo de la consulta, su estado actual 
 * y mantiene una relación de composición directa con la entidad Paciente.
 */
public class Turno {
    /** Identificador único del turno en la base de datos. */
    private int id;
    
    /** Motivo o razón principal por la cual se solicita la consulta médica. */
    private String motivo;
    
    /** Fecha calendario en la que se llevará a cabo el turno. */
    private LocalDate fecha;
    
    /** Horario exacto en el que inicia la atención del turno. */
    private LocalTime horarioInicio;
    
    /** Horario exacto en el que finaliza el turno (calculado con una duración fija de 40 min). */
    private LocalTime horarioFin;
    
    /** Instancia del paciente asociado a este turno (relación de composición). */
    private Paciente paciente;
    
    /** Estado actual del turno (ej. 'Pendiente', 'Cancelado', etc.). */
    private String estado;
    
    /*
     * ========================================================================
     *                               CONSTRUCTORES
     * ========================================================================
     */
    
    /**
     * Constructor vacío de la clase Turno.
     * Inicializa por defecto una nueva instancia de Paciente para evitar nulos en la composición.
     */
    public Turno() {
        this.paciente = new Paciente();
    }
    
    /**
     * Constructor completo de la clase Turno.
     * Permite inicializar todos los atributos del turno con valores predefinidos.
     * 
     * @param id Identificador único del turno.
     * @param motivo Razón de la consulta.
     * @param fecha Fecha del turno.
     * @param horarioInicio Hora de inicio.
     * @param horarioFin Hora de finalización.
     * @param paciente Objeto Paciente vinculado al turno.
     * @param estado Estado actual del turno.
     */
    public Turno(int id, String motivo, LocalDate fecha, LocalTime horarioInicio,
            LocalTime horarioFin, Paciente paciente, String estado) {
        this.id = id;
        this.motivo = motivo;
        this.fecha = fecha;
        this.horarioInicio = horarioInicio;
        this.horarioFin = horarioFin;
        this.paciente = paciente;
        this.estado = estado;
    }
    
    /*
     * ========================================================================
     *                                 GETTERS
     * ========================================================================
     */
    
    /**
     * Obtiene el identificador único del turno.
     * 
     * @return Integer ID del turno.
     */
    public Integer getId() {
        return id;
    }
    
    /**
     * Obtiene el motivo de la consulta médica.
     * 
     * @return String Motivo del turno.
     */
    public String getMotivo() {
        return motivo;
    }
    
    /**
     * Obtiene la fecha asignada para el turno.
     * 
     * @return LocalDate Fecha del turno.
     */
    public LocalDate getFecha() {
        return fecha;
    }
    
    /**
     * Obtiene el horario de inicio del turno.
     * 
     * @return LocalTime Horario de inicio.
     */
    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }
    
    /**
     * Obtiene el horario de finalización del turno.
     * 
     * @return LocalTime Horario de fin.
     */
    public LocalTime getHorarioFin() {
        return horarioFin;
    }
    
    /**
     * Obtiene el objeto Paciente asociado al turno.
     * 
     * @return Paciente Datos del paciente vinculado.
     */
    public Paciente getPaciente() {
        return paciente;
    }
    
    /**
     * Obtiene el estado actual en el que se encuentra el turno.
     * 
     * @return String Estado (ej. 'Pendiente', 'Cancelado').
     */
    public String getEstado() {
        return estado;
    }
    
    /*
     * ========================================================================
     *                                 SETTERS
     * ========================================================================
     */
    
    /**
     * Establece o modifica el identificador único del turno.
     * 
     * @param id Nuevo ID para el turno.
     */
    public void setId(Integer id) {
        this.id = id;
    }
    
    /**
     * Establece o modifica el motivo de la consulta.
     * 
     * @param motivo Nueva razón de la consulta.
     */
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    /**
     * Establece o modifica la fecha del turno.
     * 
     * @param fecha Nueva fecha.
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    /**
     * Establece o modifica el horario de inicio del turno.
     * 
     * @param horarioInicio Nuevo horario de inicio.
     */
    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }
    
    /**
     * Establece o modifica el horario de finalización del turno.
     * 
     * @param horarioFin Nuevo horario de finalización.
     */
    public void setHorarioFin(LocalTime horarioFin) {
        this.horarioFin = horarioFin;
    }
    
    /**
     * Asocia o modifica el objeto Paciente vinculado al turno.
     * 
     * @param paciente Nueva instancia de Paciente.
     */
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
    
    /**
     * Establece o modifica el estado actual del turno.
     * 
     * @param estado Nuevo estado.
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    /**
     * Genera una representación en texto formateado de toda la información del turno,
     * incluyendo los datos del paciente asociado mediante su propio método toString().
     * 
     * @return String Estructura textual con los detalles del turno.
     */
    @Override
    public String toString() {
        return "Turno: \nMotivo: " + motivo + "\nFecha: " + fecha + " | Horario: " +
                horarioInicio + " - " + horarioFin + "\nPaciente:\n " + paciente + "\nEstado: " +
                estado;
    }
}