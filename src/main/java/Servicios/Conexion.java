package Servicios;

import Vistas.Vista;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

/**
 * Clase Conexion:
 * Gestiona la conectividad con la base de datos MySQL mediante JDBC y maneja 
 * la persistencia dinámica de las credenciales a través de un archivo de configuración (`db.properties`).
 * Permite la configuración inicial interactiva o la modificación de parámetros por parte del operador.
 */
public class Conexion {
    /** Instancia de la Vista para interactuar por consola con el usuario. */
    private static final Vista V = new Vista();
    
    /** Nombre del archivo físico donde se almacenan las credenciales de conexión. */
    private static final String ARCHIVO_CONFIG = "db.properties";
    
    /** Objeto Properties contenedor de los parámetros de conexión (url, usuario, password). */
    private static final Properties props = new Properties();

    /**
     * Verifica la existencia del archivo de configuración de la base de datos al iniciar el sistema.
     * Si el archivo no existe, solicita los datos de acceso al usuario. Si ya existe, 
     * le ofrece la opción de modificarlos interactivamente antes de cargar las propiedades en memoria.
     * 
     * @throws IllegalStateException Si ocurre un error crítico al intentar cargar el archivo de configuración.
     */
    public void verificarYConfigurarCredenciales() {
        Scanner in = new Scanner(System.in);
        
        File archivo = new File(ARCHIVO_CONFIG);

        if(!archivo.exists()){
            System.out.println("\n--- CONFIGURACION INICIAL DE BASE DE DATOS ---");
            System.out.println("No se encontro el archivo de configuracion.");

            solicitarYGuardarCredenciales(in);
        }else{
            while(true){
                String respuesta = V.obtenerEntradaNoVacia(in, "Desea modificar la configuracion de la base de datos? (Si/No): ");

                if("Si".equalsIgnoreCase(respuesta)){
                    solicitarYGuardarCredenciales(in);
                    break;
                }

                if("No".equalsIgnoreCase(respuesta)){
                    break;
                }

                System.out.println("[Error] Debe ingresar Si o No. Intentelo nuevamente.");
            }
        }

        try{
            cargarPropiedades();
        }catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar la configuracion de la base de datos.", e);
        }
    }
    
    /**
     * Solicita por consola los parámetros de conexión a MySQL (URL, usuario y contraseña),
     * los almacena en el objeto de propiedades y los persiste físicamente en el archivo de configuración.
     * 
     * @param in Objeto Scanner compartido para la captura de datos por consola.
     * @throws IllegalStateException Si ocurre un error de Entrada/Salida (I/O) al guardar el archivo.
     */
    private static void solicitarYGuardarCredenciales(Scanner in) {
        String url = V.obtenerEntradaNoVacia(in, "Ingrese URL de MySQL (Ej: jdbc:mysql://localhost:3306/Gestion_De_Turnos_BD): ");
        String usuario = V.obtenerEntradaNoVacia(in, "Ingrese Usuario de la BD (Ej: root): ");
        String password = V.obtenerEntradaNoVacia(in, "Ingrese Password de la BD: ");

        props.setProperty("url", url);
        props.setProperty("usuario", usuario);
        props.setProperty("password", password);
        
        try(FileOutputStream fos = new FileOutputStream(ARCHIVO_CONFIG)){
            props.store(fos, "Configuracion de conexion a Base de Datos");
            System.out.println("\n[Sistema] Configuracion guardada correctamente.\n");

        }catch(IOException e){
            throw new IllegalStateException("Error al guardar la configuracion.", e);
        }
    }
    
    /**
     * Lee y carga en memoria las propiedades de conexión almacenadas en el archivo de configuración.
     * 
     * @throws IOException Si el archivo de configuración no existe o no puede ser leído.
     */
    private static void cargarPropiedades() throws IOException {
        try(FileInputStream fis = new FileInputStream(ARCHIVO_CONFIG)){
            props.load(fis);
        }
    }

    /**
     * Establece y devuelve una nueva sesión activa de conexión con la base de datos MySQL 
     * utilizando las credenciales cargadas previamente en las propiedades.
     * 
     * @return Connection Objeto que representa la sesión activa con la base de datos.
     * @throws SQLException Si ocurre un error al intentar autenticar o conectar con el servidor de base de datos.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("url"),
                props.getProperty("usuario"),
                props.getProperty("password")
        );
    }
}