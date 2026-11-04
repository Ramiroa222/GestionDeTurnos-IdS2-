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
   cd GestionDeTurnos-IdS2-
   ```

2. **Prepara la base de datos.** El archivo [`BaseDeDatos.sql`](BaseDeDatos.sql) contiene el esquema y los procedimientos, funciones y vistas que utiliza el sistema. Antes de ejecutarlo, revisa el script: incluye sentencias `DROP DATABASE`, `DROP TABLE`, `DROP PROCEDURE`, `DROP FUNCTION` y `DROP VIEW`, que pueden eliminar datos u objetos existentes. En una instalación nueva, comenta o elimina esas sentencias de borrado y ejecuta el resto del script en MySQL Workbench o desde el cliente de MySQL. La base de datos esperada por la aplicación se llama `Gestion_De_Turnos_BD`.

   **No ejecutes el script sin revisarlo** si la base de datos contiene información que quieras conservar.

3. **Compila el proyecto** desde la raíz:

   ```bash
   mvn clean package
   ```

4. **Inicia la aplicación:**

   ```bash
   mvn exec:java
   ```

5. **Configura la conexión a MySQL en el primer inicio.** La aplicación solicitará:
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
- **Error de compilación por versión de Java:** comprueba que Maven esté usando JDK 23 o superior (`java -version` y `mvn -version`).

## Tecnologías

- Java 23
- Maven
- MySQL y MySQL Connector/J
