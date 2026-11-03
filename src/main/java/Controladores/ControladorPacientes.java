package Controladores;

import Modelos.*;
import Servicios.Conexion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class ControladorPacientes {
    private final Conexion conexionDB = new Conexion(); // Instancia para usar el método getConnection().
    
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
    
    public Integer registrarPaciente(Turno t) {
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
