package Modelos;

import java.time.LocalDate;
import java.time.LocalTime;

public class Turno {
    private int id;
    private String motivo;
    private LocalDate fecha;
    private LocalTime horarioInicio;
    private LocalTime horarioFin;
    private Paciente paciente;
    private String estado;
    
    /*
     * ========================================================================
     *                               CONSTRUCTORES
     * ========================================================================
     */
    // Constructor.
    public Turno() {
        this.paciente = new Paciente();
    }
    
    // Constructor Completo.
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
    public Integer getId() {
        return id;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }
    
    public LocalTime getHorarioFin() {
        return horarioFin;
    }
    
    public Paciente getPaciente() {
        return paciente;
    }
    
    /*
     * ========================================================================
     *                                 SETTERS
     * ========================================================================
     */
    public void setId(Integer id) {
        this.id = id;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }
    
    public void setHorarioFin(LocalTime horarioFin) {
        this.horarioFin = horarioFin;
    }
    
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
    
    @Override
    public String toString() {
        return "Turno: \nMotivo: " + motivo + "\nFecha: " + fecha + "\nHorario: " +
                horarioInicio + " a " + horarioFin + "\n" + paciente + "\nEstado: " +
                estado;
    }
}