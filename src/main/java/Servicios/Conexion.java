package Servicios;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    // Configura aquí los datos de tu servidor MySQL, el puerto y el nombre de tu base de datos
    private static final String URL = "jdbc:mysql://localhost:3306/Gestion_De_Turnos_BD";
    private static final String USUARIO = "root";
    private static final String PASSWORD = ""; // Escribe tu contraseña de MySQL si la tiene.

    /**
     * Método para establecer y retornar la conexión con la base de datos MySQL.
     * @return Objeto Connection activo, o null si ocurrió un error.
     */
    public Connection getConnection() {
        Connection con = null;
        
        try{
            con = (Connection) DriverManager.getConnection(URL, USUARIO, PASSWORD);
            System.out.println("Conexion exitosa a la base de datos!");
        }catch (SQLException e){
            System.out.println("Error al conectar con la base de datos: " + e.getMessage());
        }
        
        return con;
    }
}
