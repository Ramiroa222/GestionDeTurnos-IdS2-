-- ============================================================
-- BASE DE DATOS: Gestion De Turnos De Un Consultorio Medico
-- ============================================================

-- =====================================================
-- PARA ELIMINAR LA BASE DE DATOS:
-- DROP DATABASE IF EXISTS Gestion_De_Turnos_BD;
-- =====================================================

-- =====================================================
-- PARA ELIMINAR LAS TABLAS:
-- DROP TABLE Turno;
-- DROP TABLE Paciente;
-- =====================================================

CREATE DATABASE Gestion_De_Turnos_BD;
USE Gestion_De_Turnos_BD;

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

CREATE TABLE HistorialTurnosPendientes (
   id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
   turno_id INT UNSIGNED NOT NULL,
   FOREIGN KEY (turno_id) REFERENCES Turno(id)
);

-- =====================================================
-- PROCEDIMIENTOS Y FUNCIONES
-- =====================================================

-- =====================================================
-- PARA ELIMINAR PROCEDIMIENTOS Y FUNCIONES:
-- DROP FUNCTION f_buscar_paciente_por_dni;
-- DROP PROCEDURE sp_registrar_paciente;
-- DROP FUNCTION f_verificar_superposicion_turno;
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

-- 2. Procedimiento para registrar un nuevo paciente.
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

-- 3. Funcion para verificar si un turno se superpone con otro existente.
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
