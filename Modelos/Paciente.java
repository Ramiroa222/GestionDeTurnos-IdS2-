package Modelos;

public class Paciente {
    private Integer id;
    private String nombreCompleto;
    private String dni;
    private String numeroDeTelefono;
    private String obraSocial;
    
    /*
     * ========================================================================
     *                               CONSTRUCTORES
     * ========================================================================
     */
    // Constructor Vacio.
    public Paciente() {}
    
    // Constructor Completo.
    public Paciente(int id, String nombreCompleto, String dni, String numeroDeTelefono,
            String obraSocial) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.dni = dni;
        this.numeroDeTelefono = numeroDeTelefono;
        this.obraSocial = obraSocial;
    }
    
    /*
     * ========================================================================
     *                                 GETTERS
     * ========================================================================
     */
    public Integer getId() {
        return id;
    }
    
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    
    public String getDni() {
        return dni;
    }
    
    public String getNumeroDeTelefono() {
        return numeroDeTelefono;
    }
    
    public String getObraSocial() {
        return obraSocial;
    }
    
    /*
     * ========================================================================
     *                                 SETTERS
     * ========================================================================
     */
    public void setId(Integer id) {
        this.id = id;
    }
    
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    
    public void setDni(String dni) {
        this.dni = dni;
    }
    
    public void setNumeroDeTelefono(String numeroDeTelefono) {
        this.numeroDeTelefono = numeroDeTelefono;
    }
    
    public void setObraSocial(String obraSocial) {
        this.obraSocial = obraSocial;
    }
    
    @Override
    public String toString() {
        return "Nombre Completo: " + nombreCompleto + "\nDNI: " + dni +
               "\nTelefono: " + numeroDeTelefono + "\nObra Social: " +
               obraSocial;
    }
}