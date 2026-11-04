# Gestor de turnos para consultorio médico

Aplicación de consola escrita en Java para administrar pacientes y turnos de un consultorio médico. Permite registrar y modificar turnos, consultar información, cancelar turnos y generar un reporte PDF de los turnos del día.

## Requisitos

- Java Development Kit (JDK) 23 o superior.
- Apache Maven.
- MySQL en ejecución y permisos para crear la base de datos y sus objetos.

## Instalación y configuración

1. **Clona el repositorio y entra al directorio del proyecto:**

   ```bash
   git clone https://github.com/Ramiroa222/GestionDeTurnos-IdS2-.git
   ```
   
2. **Crea y prepara la base de datos MySQL:**

   El archivo [`BaseDeDatos.sql`](BaseDeDatos.sql) crea la base `Gestion_De_Turnos_BD`, las tablas `Paciente`, `Turno` e `HistorialDeTurnosPendientes`, además de los procedimientos almacenados, funciones y vistas que usa la aplicación.

   > **Advertencia:** el script contiene sentencias `DROP` que pueden eliminar la base de datos, tablas y objetos. Antes de ejecutarlo, haz una copia de seguridad si ya tienes información y comenta o elimina todas las sentencias `DROP` del archivo, incluidas las secciones para borrar la base de datos, tablas, procedimientos, funciones y vistas. Si dejas los `DROP` de procedimientos, funciones o vistas al final, esos objetos se crearán y luego se borrarán, y el programa no podrá utilizarlos.

   **Opción A: MySQL Workbench**

   1. Inicia el servidor MySQL y abre MySQL Workbench.
   2. Conéctate al servidor con un usuario que tenga permisos para crear bases de datos, tablas, rutinas y vistas.
   3. Abre `BaseDeDatos.sql` desde **File > Open SQL Script**.
   4. Revisa el archivo y comenta/elimina las sentencias `DROP` indicadas arriba. En Workbench puedes anteponer `-- ` a cada sentencia para comentarla.
   5. Ejecuta el script completo con el botón del rayo (**Execute**) o con **Ctrl+Shift+Enter**.
   6. En el panel **SCHEMAS**, actualiza la lista y confirma que exista `Gestion_De_Turnos_BD` con las tablas, vistas, funciones y procedimientos definidos en el script.

   **Opción B: consola de MySQL**

   Después de revisar y guardar los cambios en el script, abre CMD en la carpeta donde está `BaseDeDatos.sql` y ejecuta:

   ```bat
   mysql -u root -p < BaseDeDatos.sql
   ```

   Cambia `root` por tu usuario de MySQL si corresponde. Escribe la contraseña cuando la solicite. 

3. **Inicia la aplicación:**
   **Opcion A**
   En CMD entra al directorio del proyecto:
   ```bash
   cd rutaHaciaElDirectorio\GestionDeTurnos-IdS2--main\ejecutable
   ```
   Luego:
   ```bash
   java -jar "%~dp0GestionDeTurnos-1.0-SNAPSHOT.jar"
   ```
   **Opcion B**
   Ejecutar el archivo Sistema De Gestion De Turnos.bat (Esta en la carpeta ejecutable).
   
4. **Configura la conexión a MySQL en el primer inicio.** La aplicación solicitará:
   - URL JDBC, por ejemplo: `jdbc:mysql://localhost:3306/Gestion_De_Turnos_BD`
   - Usuario de MySQL, por ejemplo: `root`
   - Contraseña del usuario

   La configuración se guarda en `db.properties` en el directorio desde donde se ejecuta la aplicación. En las siguientes ejecuciones, responde `No` para conservarla o `Si` para cambiarla. Mantén este archivo privado y no compartas sus credenciales.

## Uso

La aplicación se maneja mediante menús en la terminal. Escribe el número de la opción deseada y presiona **Enter**:

- **0 — Salir** del programa.
- **1 — Gestionar turnos:** registrar un turno o modificar uno existente.
- **2 — Consultar turnos y pacientes:** consultar la agenda, turnos pendientes, turnos de un paciente, todos los turnos, la lista de pacientes o buscar un paciente por DNI.
- **3 — Cancelar un turno** registrado.
- **4 — Generar un reporte PDF** de los turnos del día. Revisa la terminal para ver el resultado o la ubicación del archivo generado.

Al registrar datos, sigue el formato que solicita la consola. La aplicación valida, entre otros, el DNI, el teléfono, la fecha (`dd/MM/yyyy`) y la hora (`HH:mm`). Los turnos se otorgan de lunes a viernes y en el rango horario de 08:00 a 19:20. Los turnos vencidos se actualizan automáticamente al iniciar cada ciclo del menú.

## Solución de problemas
- **No se puede conectar a MySQL:** verifica que el servidor esté activo, que la URL incluya el puerto correcto, que exista `Gestion_De_Turnos_BD` y que el usuario y la contraseña sean válidos. Puedes elegir `Si` al inicio para volver a configurar la conexión.
- **No encuentra la base de datos o sus objetos:** confirma que ejecutaste el script de inicialización revisado y que se crearon las tablas, procedimientos, funciones y vistas.

## Tecnologías

- Java 23
- Maven
- MySQL y MySQL Connector/J
