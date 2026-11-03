package Servicios;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class ActualizadorDeTurnosService {
    private final Conexion conexionDB = new Conexion();

    /**
     * Ejecuta el proceso de actualización masiva.
     * Pasa a estado 'Pasado' los turnos cuya fecha/hora ya expiró y los retira del historial de pendientes.
     */
    public void actualizarTurnosVencidos() {
        String sql = "{CALL sp_actualizar_turnos_vencidos()}";
        
        try(Connection con = conexionDB.getConnection();
            CallableStatement cstmt = con.prepareCall(sql)){
            
            cstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println("Error al actualizar los turnos vencidos: " + e.getMessage());
        }
    }
}