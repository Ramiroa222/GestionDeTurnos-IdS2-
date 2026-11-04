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

2. **Instala y configura Maven en Windows:**

   1. Descarga el archivo **Binary zip archive** desde la [página oficial de descargas de Maven](https://maven.apache.org/download.cgi). No descargues el archivo *Source archive*.
   2. Crea una carpeta para Maven, por ejemplo `C:\herramientas`, y extrae el ZIP allí. Deberías obtener una carpeta con un nombre parecido a `C:\herramientas\apache-maven-3.x.x`.
   3. Abre esa carpeta y comprueba que dentro exista `bin`; la ruta completa será parecida a `C:\herramientas\apache-maven-3.x.x\bin`. Usa la ruta real de la versión que descargaste.
   4. Agrega esa ruta al `PATH` de Windows:
      - Busca en Inicio **Editar las variables de entorno del sistema** y abre esa opción.
      - Pulsa **Variables de entorno...**.
      - En **Variables del sistema**, selecciona `Path` y pulsa **Editar**. Si no tienes permisos de administrador, puedes editar el `Path` de usuario.
      - Pulsa **Nuevo**, pega la ruta completa a la carpeta `bin` de Maven y acepta todas las ventanas.
      - No reemplaces ni borres las entradas existentes del `Path`.
   5. Cierra las ventanas de CMD o PowerShell que ya estuvieran abiertas y abre una nueva. Comprueba la instalación con:

      ```bat
      mvn -version
      ```

      Debe mostrarse la versión de Apache Maven y la versión de Java. Si Windows indica que `mvn` no se reconoce, revisa que agregaste al `Path` la carpeta `bin` correcta y que abriste una terminal nueva.

   Maven necesita un JDK disponible. Verifica también:

   ```bat
   java -version
   mvn -version
   ```

   Ambos comandos deben funcionar y Maven debe detectar JDK 23 o superior. Si `java` no se reconoce o Maven muestra otra versión, instala/configura el JDK y vuelve a abrir la terminal antes de continuar.

3. **Crea y prepara la base de datos MySQL:**

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

   Cambia `root` por tu usuario de MySQL si corresponde. Escribe la contraseña cuando la solicite. También puedes importar el archivo desde otra herramienta cliente de MySQL.

4. **Compila el proyecto** desde la raíz, en la carpeta que contiene `pom.xml`:

   ```bat
   mvn clean package
   ```

5. **Inicia la aplicación:**

   ```bat
   mvn exec:java
   ```

6. **Configura la conexión a MySQL en el primer inicio.** La aplicación solicitará:
   - URL JDBC, por ejemplo: `jdbc:mysql://localhost:3306/Gestion_De_Turnos_BD`
   - Usuario de MySQL, por ejemplo: `root`
   - Contraseña del usuario

   La configuración se guarda en `db.properties` en el directorio desde donde se ejecuta la aplicación. En las siguientes ejecuciones, responde `No` para conservarla o `Si` para cambiarla. Mantén este archivo privado y no compartas sus credenciales.

## Ejecutar en Windows con `ejecutar.bat`

El archivo [`ejecutar.bat`](ejecutar.bat) automatiza el inicio del programa en Windows: cambia el directorio de trabajo a la ubicación del propio archivo, ejecuta `mvn clean package` para compilar el proyecto y, si la compilación termina correctamente, inicia la aplicación con `mvn exec:java`. Si falla la compilación, muestra un aviso y no intenta iniciar el programa.

Para usarlo, asegúrate de tener Java, Maven y MySQL instalados y configurados, y luego haz doble clic en `ejecutar.bat` desde la carpeta del proyecto (la misma que contiene `pom.xml`). También puedes abrir CMD en esa carpeta y ejecutar:

```bat
ejecutar.bat
```

La ventana de consola permanecerá abierta al terminar para que puedas revisar los mensajes. Si modificas el código fuente, no hace falta crear otro `.bat`: vuelve a ejecutar el mismo archivo y este recompilará antes de iniciar la aplicación. La primera vez, introduce los datos de conexión a MySQL cuando los solicite.

## Uso

La aplicación se maneja mediante menús en la terminal. Escribe el número de la opción deseada y presiona **Enter**:

- **0 — Salir** del programa.
- **1 — Gestionar turnos:** registrar un turno o modificar uno existente.
- **2 — Consultar turnos y pacientes:** consultar la agenda, turnos pendientes, turnos de un paciente, todos los turnos, la lista de pacientes o buscar un paciente por DNI.
- **3 — Cancelar un turno** registrado.
- **4 — Generar un reporte PDF** de los turnos del día. Revisa la terminal para ver el resultado o la ubicación del archivo generado.

Al registrar datos, sigue el formato que solicita la consola. La aplicación valida, entre otros, el DNI, el teléfono, la fecha (`dd/MM/yyyy`) y la hora (`HH:mm`). Los turnos se otorgan de lunes a viernes y en el rango horario de 08:00 a 19:20. Los turnos vencidos se actualizan automáticamente al iniciar cada ciclo del menú.

## Solución de problemas

- **`mvn` no se reconoce:** confirma que agregaste al `Path` la ruta a la carpeta `bin` de Maven, abre una terminal nueva y verifica con `mvn -version`.
- **No se puede conectar a MySQL:** verifica que el servidor esté activo, que la URL incluya el puerto correcto, que exista `Gestion_De_Turnos_BD` y que el usuario y la contraseña sean válidos. Puedes elegir `Si` al inicio para volver a configurar la conexión.
- **No encuentra la base de datos o sus objetos:** confirma que ejecutaste el script de inicialización revisado y que se crearon las tablas, procedimientos, funciones y vistas.
- **Error de compilación por versión de Java:** comprueba que Maven esté usando JDK 23 o superior (`java -version` y `mvn -version`).

## Tecnologías

- Java 23
- Maven
- MySQL y MySQL Connector/J
