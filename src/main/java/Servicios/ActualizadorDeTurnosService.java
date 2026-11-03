package Servicios;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Clase ActualizadorDeTurnosService:
 * Servicio de negocio encargado de gestionar la sincronización automática 
 * y la actualización del estado de los turnos médicos que han expirado en el sistema.
 */
public class ActualizadorDeTurnosService {
    /** Instancia del servicio de conexión para obtener accesos activos a la base de datos. */
    private final Conexion conexionDB = new Conexion();
    
    /**
     * Ejecuta el procedimiento almacenado para la actualización masiva de turnos vencidos.
     * Pasa a estado 'Pasado' los turnos cuya fecha y horario ya expiraron y los retira 
     * automáticamente del historial de turnos pendientes.
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