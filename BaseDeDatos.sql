-- ============================================================
-- BASE DE DATOS: Gestion De Turnos De Un Consultorio Medico
-- ============================================================

-- ============================================================
-- PARA CREAR Y USAR LA BASE DE DATOS
-- ============================================================
CREATE DATABASE Gestion_De_Turnos_BD;
USE Gestion_De_Turnos_BD;

-- =====================================================
-- PARA ELIMINAR LA BASE DE DATOS
-- =====================================================
DROP DATABASE IF EXISTS Gestion_De_Turnos_BD;

-- =====================================================
-- PARA ELIMINAR LAS TABLAS
-- =====================================================
DROP TABLE Turno;
DROP TABLE Paciente;
DROP TABLE HistorialDeTurnosPendientes;

-- =====================================================
-- TABLA: Paciente (1° Crear La Tabla Paciente)
-- =====================================================
CREATE TABLE Paciente (
   id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
   nombre_completo VARCHAR(255) NOT NULL,
   dni VARCHAR(20) NOT NULL UNIQUE,
   numero_de_telefono VARCHAR(50) NOT NULL,
   obra_social VARCHAR(100) NOT NULL
);

-- =====================================================
-- TABLA: Turno (2° Crear La Tabla Turno)
-- =====================================================
CREATE TABLE Turno (
   id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
   motivo VARCHAR(255) NOT NULL,
   fecha DATE NOT NULL,
   horario_inicio TIME NOT NULL,
   horario_fin TIME NOT NULL,
   paciente_id INT UNSIGNED NOT NULL,
   estado VARCHAR(10) NOT NULL,
   FOREIGN KEY (paciente_id) REFERENCES Paciente(id)
);

-- =====================================================================================
-- TABLA: HistorialDeTurnosPendientes (3° Crear La Tabla HistorialDeTurnosPendientes)
-- =====================================================================================
CREATE TABLE HistorialDeTurnosPendientes (
   id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
   turno_id INT UNSIGNED NOT NULL,
   FOREIGN KEY (turno_id) REFERENCES Turno(id)
);

-- =====================================================
-- CONSULTAS (PARA VERIFICAR LOS DATOS)
-- =====================================================
SELECT * FROM Turno;
SELECT * FROM Paciente;
SELECT * FROM HistorialDeTurnosPendientes;

-- =====================================================
-- PROCEDIMIENTOS
-- =====================================================
-- 1. Procedimiento para registrar un nuevo paciente.
DELIMITER //

CREATE PROCEDURE sp_registrar_paciente(
    IN p_nombre_completo VARCHAR(255),
    IN p_dni VARCHAR(20),
    IN p_numero_de_telefono VARCHAR(50),
    IN p_obra_social VARCHAR(100),
    OUT p_nuevo_id INT
)
BEGIN
    INSERT INTO Paciente (nombre_completo, dni, numero_de_telefono, obra_social)
    VALUES (p_nombre_completo, p_dni, p_numero_de_telefono, p_obra_social);
    
    SET p_nuevo_id = LAST_INSERT_ID();
END //

DELIMITER ;

-- 2. Procedimiento que registra un turno y lo asigna automáticamente al HistorialDeTurnosPendientes.
DELIMITER //

CREATE PROCEDURE sp_registrar_turno(
    IN p_motivo VARCHAR(255),
    IN p_fecha DATE,
    IN p_horario_inicio TIME,
    IN p_horario_fin TIME,
    IN p_paciente_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- 1. Insertar el turno
    INSERT INTO Turno (motivo, fecha, horario_inicio, horario_fin, paciente_id, estado)
    VALUES (p_motivo, p_fecha, p_horario_inicio, p_horario_fin, p_paciente_id, 'Pendiente');
    
    -- 2. Insertar en el historial usando el ID generado automáticamente por MySQL
    INSERT INTO HistorialDeTurnosPendientes (turno_id)
    VALUES (LAST_INSERT_ID());
    
    -- Si todo salió bien, confirmamos los cambios
    COMMIT;
END //

DELIMITER ;

-- 3. Procedimiento que actualiza los datos de un paciente basándose en su ID.
DELIMITER //

CREATE PROCEDURE sp_actualizar_paciente(
    IN p_id INT,
    IN p_nombre_completo VARCHAR(255),
    IN p_numero_de_telefono VARCHAR(50),
    IN p_obra_social VARCHAR(100)
)
BEGIN
    UPDATE Paciente 
    SET nombre_completo = p_nombre_completo,
        numero_de_telefono = p_numero_de_telefono,
        obra_social = p_obra_social
    WHERE id = p_id;
END //

DELIMITER ;

-- 4. Buscar ID de turno por fecha y horario.
DELIMITER //
CREATE PROCEDURE sp_buscar_turno_por_fecha_hora(
    IN p_fecha DATE,
    IN p_horario_inicio TIME,
    OUT p_turno_id INT
)
BEGIN
    SELECT t.id INTO p_turno_id
    FROM Turno t
    JOIN HistorialDeTurnosPendientes h ON t.id = h.turno_id
    WHERE t.fecha = p_fecha 
      AND t.horario_inicio = p_horario_inicio
    LIMIT 1;
END //

DELIMITER ;

-- =====================================================
-- PARA ELIMINAR PROCEDIMIENTOS
-- =====================================================
DROP PROCEDURE sp_registrar_paciente;
DROP PROCEDURE sp_registrar_turno;
DROP PROCEDURE sp_actualizar_paciente;
DROP PROCEDURE sp_buscar_turno_por_fecha_hora;

-- =====================================================
-- FUNCIONES
-- =====================================================
-- 1. Funcion que SOLO busca al paciente por DNI y devuelve su ID (o NULL si no existe).
DELIMITER //

CREATE FUNCTION f_buscar_paciente_por_dni(
    p_dni VARCHAR(20)
) 
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_id INT DEFAULT NULL;
    
    SELECT id INTO v_id
    FROM Paciente
    WHERE dni = p_dni;
    
    RETURN v_id;
END //

DELIMITER ;

-- 2. Funcion para verificar si un turno se superpone con otro existente.
DELIMITER //

CREATE FUNCTION f_verificar_superposicion_turno(
    p_fecha DATE,
    p_horario_inicio TIME,
    p_horario_fin TIME
)
RETURNS BOOLEAN
READS SQL DATA
BEGIN
    DECLARE v_existe BOOLEAN DEFAULT FALSE;

    -- Verifica si existe superposición cruzando Turno con su Historial de Pendientes.
    SELECT EXISTS (
        SELECT 1  
        FROM Turno t
        JOIN HistorialDeTurnosPendientes h ON t.id = h.turno_id
        WHERE t.fecha = p_fecha
          AND t.horario_inicio < p_horario_fin
          AND t.horario_fin > p_horario_inicio
    ) INTO v_existe;

    RETURN v_existe;
END //

DELIMITER ;

-- 3. Función de superposición excluyendo el ID actual.
DELIMITER //

CREATE FUNCTION f_verificar_superposicion_turno_excluyendo(
    p_fecha DATE,
    p_horario_inicio TIME,
    p_horario_fin TIME,
    p_turno_id_excluir INT
)
RETURNS BOOLEAN
READS SQL DATA
BEGIN
    DECLARE v_existe BOOLEAN DEFAULT FALSE;

    SELECT EXISTS (
        SELECT 1  
        FROM Turno t
        JOIN HistorialDeTurnosPendientes h ON t.id = h.turno_id
        WHERE t.fecha = p_fecha
          AND t.id <> p_turno_id_excluir
          AND t.horario_inicio < p_horario_fin
          AND t.horario_fin > p_horario_inicio
    ) INTO v_existe;

    RETURN v_existe;
END //

DELIMITER ;

-- =====================================================
-- PARA ELIMINAR FUNCIONES
-- =====================================================
DROP FUNCTION f_buscar_paciente_por_dni;
DROP FUNCTION f_verificar_superposicion_turno;

