package Modelos;

/**
 * Clase Paciente:
 * Representa el modelo de datos para un paciente dentro del sistema médico.
 * Almacena su información personal de contacto, identificación (DNI) y cobertura social.
 */
public class Paciente {
    /** Identificador único del paciente en la base de datos. */
    private Integer id;
    
    /** Nombre y apellido completos del paciente. */
    private String nombreCompleto;
    
    /** Número de Documento Nacional de Identidad (DNI) del paciente. */
    private String dni;
    
    /** Número de teléfono de contacto del paciente. */
    private String numeroDeTelefono;
    
    /** Obra social o prepaga a la que pertenece el paciente. */
    private String obraSocial;
    
    /*
     * ========================================================================
     *                               CONSTRUCTORES
     * ========================================================================
     */
    
    /**
     * Constructor vacío de la clase Paciente.
     * Permite instanciar objetos vacíos para ser poblados posteriormente mediante setters.
     */
    public Paciente() {}
    
    /**
     * Constructor completo de la clase Paciente.
     * Permite inicializar todos los atributos del paciente al momento de su creación.
     * 
     * @param id Identificador único del paciente.
     * @param nombreCompleto Nombre y apellido.
     * @param dni Número de documento.
     * @param numeroDeTelefono Teléfono de contacto.
     * @param obraSocial Obra social del paciente.
     */
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
    
    /**
     * Obtiene el identificador único del paciente.
     * 
     * @return Integer ID del paciente.
     */
    public Integer getId() {
        return id;
    }
    
    /**
     * Obtiene el nombre completo del paciente.
     * 
     * @return String Nombre y apellido.
     */
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    
    /**
     * Obtiene el número de DNI del paciente.
     * 
     * @return String DNI.
     */
    public String getDni() {
        return dni;
    }
    
    /**
     * Obtiene el número de teléfono de contacto.
     * 
     * @return String Teléfono.
     */
    public String getNumeroDeTelefono() {
        return numeroDeTelefono;
    }
    
    /**
     * Obtiene la obra social asociada al paciente.
     * 
     * @return String Obra social.
     */
    public String getObraSocial() {
        return obraSocial;
    }
    
    /*
     * ========================================================================
     *                                 SETTERS
     * ========================================================================
     */
    
    /**
     * Establece o modifica el identificador único del paciente.
     * 
     * @param id Nuevo ID.
     */
    public void setId(Integer id) {
        this.id = id;
    }
    
    /**
     * Establece o modifica el nombre completo del paciente.
     * 
     * @param nombreCompleto Nuevo nombre y apellido.
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    
    /**
     * Establece o modifica el número de DNI del paciente.
     * 
     * @param dni Nuevo DNI.
     */
    public void setDni(String dni) {
        this.dni = dni;
    }
    
    /**
     * Establece o modifica el número de teléfono de contacto.
     * 
     * @param numeroDeTelefono Nuevo teléfono.
     */
    public void setNumeroDeTelefono(String numeroDeTelefono) {
        this.numeroDeTelefono = numeroDeTelefono;
    }
    
    /**
     * Establece o modifica la obra social del paciente.
     * 
     * @param obraSocial Nueva obra social.
     */
    public void setObraSocial(String obraSocial) {
        this.obraSocial = obraSocial;
    }
    
    /**
     * Genera una representación en texto formateado con los datos principales del paciente.
     * 
     * @return String Detalle textual de los atributos del paciente.
     */
    @Override
    public String toString() {
        return "Nombre Completo: " + nombreCompleto + " | DNI: " + dni +
               " | Telefono: " + numeroDeTelefono + " | Obra Social: " +
               obraSocial;
    }
}