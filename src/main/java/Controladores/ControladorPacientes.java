package Controladores;

import Modelos.*; // Importa todas las clases del paquete de modelos (como Turno y Paciente).
import Servicios.Conexion; // Importa la clase encargada de gestionar y proveer la conexión a la base de datos.

import java.sql.CallableStatement; // Permite invocar procedimientos almacenados con parámetros de entrada y salida (OUT).
import java.sql.Connection; // Representa una sesión activa de conexión con la base de datos relacional.
import java.sql.PreparedStatement; // Permite ejecutar sentencias SQL precompiladas de manera segura y parametrizada.
import java.sql.ResultSet; // Almacena y permite recorrer los registros devueltos por una consulta SQL.
import java.sql.SQLException; // Maneja las excepciones y errores derivados de la interacción JDBC con la BD.
import java.sql.Types; // Proporciona constantes estándar para identificar tipos de datos SQL (usado en parámetros OUT).

/**
 * Clase ControladorPacientes: Actúa como controlador de persistencia (según el patrón MVC)
 * para la entidad Paciente. Se encarga de gestionar las operaciones de búsqueda por DNI, 
 * registro de nuevos pacientes y actualización de sus datos personales en la base de datos.
 */
public class ControladorPacientes {
    // --- VARIABLES DE INSTANCIA --- //
    
    /** Instancia de la clase Conexion utilizada para obtener accesos activos (getConnection) a la base de datos. */
    private final Conexion conexionDB = new Conexion(); 
    
    /**
     * Metodo buscarPacientePorDni: Busca un paciente en la base de datos a través 
     * de su número de DNI, invocando una función SQL específica y manejando posibles 
     * valores nulos.
     * 
     * @param t Objeto Turno que contiene la información del paciente, específicamente el DNI a consultar.
     * @return Integer El ID único del paciente si es encontrado, o null si no existe o ocurre un error.
     */
    public Integer buscarPacientePorDni(Turno t) {
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
     * Metodo registrarPaciente: Registra un nuevo paciente en la base de datos 
     * utilizando un procedimiento almacenado que inserta la información y retorna 
     * de forma automática el identificador generado mediante un parámetro OUT.
     * 
     * @param t Objeto Turno que contiene los datos personales del paciente (nombre completo, DNI, teléfono y obra social).
     * @return Integer El ID único generado y asignado al nuevo paciente en la base de datos, o null si ocurre algún fallo.
     */
    public Integer registrarPaciente(Turno t) {
        String sql = "{CALL sp_registrar_paciente(?, ?, ?, ?, ?)}";
        
        try(Connection con = conexionDB.getConnection();
            CallableStatement cstmt = con.prepareCall(sql)){
            
            cstmt.setString(1, t.getPaciente().getNombreCompleto());
            cstmt.setString(2, t.getPaciente().getDni());
            cstmt.setString(3, t.getPaciente().getNumeroDeTelefono());
            cstmt.setString(4, t.getPaciente().getObraSocial());
            cstmt.registerOutParameter(5, Types.INTEGER); // Registra el parámetro OUT correspondiente a p_nuevo_id.
            
            cstmt.executeUpdate();
            
            return cstmt.getInt(5); // Retorna el ID generado por la base de datos.
            
        }catch(SQLException e){
            System.out.println("Error al registrar paciente: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Metodo actualizarPaciente: Actualiza de forma directa los datos personales 
     * de un paciente existente en la base de datos (nombre completo, número de 
     * teléfono y obra social) basándose en su ID único.
     * 
     * @param t Objeto Turno que contiene el ID del paciente y sus nuevos atributos actualizados.
     */
    public void actualizarPaciente(Turno t) {
        String sql = "UPDATE Paciente SET nombre_completo = ?, numero_de_telefono = ?, obra_social = ? WHERE id = ?";
    
        try(Connection con = conexionDB.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)){
        
            pstmt.setString(1, t.getPaciente().getNombreCompleto());
            pstmt.setString(2, t.getPaciente().getNumeroDeTelefono());
            pstmt.setString(3, t.getPaciente().getObraSocial());
            pstmt.setInt(4, t.getPaciente().getId());
        
            pstmt.executeUpdate();
        
        }catch(SQLException e){
            System.out.println("Error al actualizar los datos del paciente: " + e.getMessage());
        }
    }
}