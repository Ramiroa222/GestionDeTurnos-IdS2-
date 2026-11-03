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
DROP TABLE HistorialDeTurnosPendientes;
DROP TABLE Turno;
DROP TABLE Paciente;

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
-- CONSULTAS (Para verificar los datos)
-- =====================================================
SELECT * FROM Turno;
SELECT * FROM Paciente;
SELECT * FROM HistorialDeTurnosPendientes;

-- =====================================================
-- PROCEDIMIENTOS
-- =====================================================

-- =============================================================================
-- PROCEDIMIENTO para registrar un nuevo paciente y devolver el id del paciente
-- =============================================================================
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

-- ===============================================================================================
-- PROCEDIMIENTO que registra un turno y lo asigna automáticamente al HistorialDeTurnosPendientes
-- ===============================================================================================
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

-- ===========================================================================
-- PROCEDIMIENTO que actualiza los turnos pendientes vencidos (Que ya pasaron)
-- ===========================================================================
DELIMITER //

CREATE PROCEDURE sp_actualizar_turnos_vencidos()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- 1. Cambiar el estado a 'Pasado' de aquellos turnos cuya fecha y hora de fin ya pasaron
    -- y que todavía figuraban como 'Pendiente'
    UPDATE Turno 
    SET estado = 'Pasado'
    WHERE estado = 'Pendiente' 
      AND CONCAT(fecha, ' ', horario_fin) < NOW();

    -- 2. Remover de la tabla HistorialDeTurnosPendientes aquellos turnos que ya no están pendientes
    DELETE h 
    FROM HistorialDeTurnosPendientes h
    JOIN Turno t ON h.turno_id = t.id
    WHERE t.estado <> 'Pendiente';

    COMMIT;
END //

DELIMITER ;

-- ===========================================================================
-- PROCEDIMIENTO que activa un turno a pendiente
-- ===========================================================================
DELIMITER //

CREATE PROCEDURE sp_activar_turno_pendiente(
    IN p_turno_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- 1. Actualizar el estado del turno a 'Pendiente'
    UPDATE Turno 
    SET estado = 'Pendiente' 
    WHERE id = p_turno_id;

    -- 2. Insertarlo en el historial asegurando que no se duplique si ya existiera
    INSERT INTO HistorialDeTurnosPendientes (turno_id)
    SELECT p_turno_id 
    FROM DUAL
    WHERE NOT EXISTS (
        SELECT 1 FROM HistorialDeTurnosPendientes WHERE turno_id = p_turno_id
    );

    COMMIT;
END //

DELIMITER ;

-- ================================================================================
-- PROCEDIMIENTO que actualiza el estado del turno a 'Cancelado' y lo retira de la
-- tabla HistorialDeTurnosPendientes para liberar el bloque horario
-- ================================================================================
DELIMITER //

CREATE PROCEDURE sp_cancelar_turno(
    IN p_turno_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- 1. Actualizar el estado del turno a 'Cancelado'
    UPDATE Turno 
    SET estado = 'Cancelado' 
    WHERE id = p_turno_id;

    -- 2. Eliminar del historial de pendientes para liberar el bloque horario
    DELETE FROM HistorialDeTurnosPendientes 
    WHERE turno_id = p_turno_id;

    COMMIT;
END //

DELIMITER ;

-- =====================================================
-- PARA ELIMINAR PROCEDIMIENTOS
-- =====================================================
DROP PROCEDURE sp_registrar_paciente;
DROP PROCEDURE sp_registrar_turno;
DROP PROCEDURE sp_actualizar_turnos_vencidos;
DROP PROCEDURE sp_activar_turno_pendiente;
DROP PROCEDURE sp_cancelar_turno;

-- =====================================================
-- FUNCIONES
-- =====================================================

-- =====================================================================================
-- FUNCION que SOLO busca al paciente por DNI y devuelve su ID (o NULL si no existe)
-- =====================================================================================
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

-- =====================================================================================
-- FUNCION para verificar si un turno se superpone con otro existente
-- =====================================================================================
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
          AND t.estado <> 'Cancelado'
          AND t.horario_inicio < p_horario_fin
          AND t.horario_fin > p_horario_inicio
    ) INTO v_existe;

    RETURN v_existe;
END //

DELIMITER ;

-- =====================================================================================
-- FUNCION de superposición excluyendo el ID actual
-- =====================================================================================
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
          AND t.estado <> 'Cancelado'
          AND t.horario_inicio < p_horario_fin
          AND t.horario_fin > p_horario_inicio
    ) INTO v_existe;

    RETURN v_existe;
END //

DELIMITER ;

-- =====================================================
-- FUNCION que busca el ID de turno por fecha y horario
-- =====================================================
DELIMITER //

CREATE FUNCTION f_buscar_turno_por_fecha_hora(
    p_fecha DATE,
    p_horario_inicio TIME
)
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_turno_id INT DEFAULT NULL;

    SELECT t.id INTO v_turno_id 
    FROM Turno t
    WHERE t.fecha = p_fecha 
      AND t.horario_inicio = p_horario_inicio
    LIMIT 1;

    RETURN v_turno_id;
END //

DELIMITER ;

-- =====================================================================================
-- FUNCION que a diferencia de la que valida solo pendientes esta revisa toda la tabla
-- Turno para evitar duplicados históricos en el mismo horario
-- =====================================================================================
DELIMITER //

CREATE FUNCTION f_verificar_superposicion_turno_pasado(
    p_fecha DATE,
    p_horario_inicio TIME,
    p_horario_fin TIME
)
RETURNS BOOLEAN
READS SQL DATA
BEGIN
    DECLARE v_existe BOOLEAN DEFAULT FALSE;

    -- Compara contra cualquier turno registrado en esa fecha y horario
    SELECT EXISTS (
        SELECT 1  
        FROM Turno t
        WHERE t.fecha = p_fecha
          AND t.estado <> 'Cancelado'
          AND t.horario_inicio < p_horario_fin
          AND t.horario_fin > p_horario_inicio
    ) INTO v_existe;

    RETURN v_existe;
END //

DELIMITER ;

-- =====================================================================================
-- FUNCION que revisa contra cualquier turno existente, pero ignorando el ID del turno
-- que se esta editando
-- =====================================================================================
DELIMITER //

CREATE FUNCTION f_verificar_superposicion_turno_pasado_excluyendo(
    p_fecha DATE,
    p_horario_inicio TIME,
    p_horario_fin TIME,
    p_turno_id_excluir INT
)
RETURNS BOOLEAN
READS SQL DATA
BEGIN
    DECLARE v_existe BOOLEAN DEFAULT FALSE;

    -- Compara contra cualquier turno registrado excluyendo el ID actual
    SELECT EXISTS (
        SELECT 1  
        FROM Turno t
        WHERE t.fecha = p_fecha
          AND t.id <> p_turno_id_excluir
          AND t.estado <> 'Cancelado'
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
DROP FUNCTION f_verificar_superposicion_turno_excluyendo;
DROP FUNCTION f_buscar_turno_por_fecha_hora;
DROP FUNCTION f_verificar_superposicion_turno_pasado;
DROP FUNCTION f_verificar_superposicion_turno_pasado_excluyendo;

-- =====================================================
-- VISTAS
-- =====================================================

-- =====================================================
-- Vista para turnos pendientes
-- =====================================================
CREATE OR REPLACE VIEW v_turnos_pendientes AS
SELECT t.id, t.fecha, t.horario_inicio, t.horario_fin, t.motivo, t.estado,
       p.id AS paciente_id, p.nombre_completo, p.dni, p.numero_de_telefono, p.obra_social
FROM Turno t
JOIN Paciente p ON t.paciente_id = p.id
JOIN HistorialDeTurnosPendientes h ON t.id = h.turno_id
WHERE t.estado = 'Pendiente';

-- ===================================================================
-- Vista para turnos por paciente (historial completo por paciente)
-- ===================================================================
CREATE OR REPLACE VIEW v_turnos_por_paciente AS
SELECT t.id, t.fecha, t.horario_inicio, t.horario_fin, t.motivo, t.estado,
       p.id AS paciente_id, p.nombre_completo, p.dni, p.numero_de_telefono, p.obra_social
FROM Turno t
JOIN Paciente p ON t.paciente_id = p.id;

-- ============================================
-- Vista para todos los turnos existentes
-- ============================================
CREATE OR REPLACE VIEW v_todos_los_turnos AS
SELECT t.id, t.fecha, t.horario_inicio, t.horario_fin, t.motivo, t.estado,
       p.id AS paciente_id, p.nombre_completo, p.dni, p.numero_de_telefono, p.obra_social
FROM Turno t
JOIN Paciente p ON t.paciente_id = p.id;

-- ===============================
-- Vista para listar pacientes
-- ===============================
CREATE OR REPLACE VIEW v_pacientes AS
SELECT id, nombre_completo, dni, numero_de_telefono, obra_social
FROM Paciente;

-- ===============================
-- PARA ELIMINAR VISTAS
-- ===============================
DROP  VIEW v_turnos_pendientes;
DROP  VIEW v_turnos_por_paciente;
DROP  VIEW v_todos_los_turnos;
DROP  VIEW v_pacientes;