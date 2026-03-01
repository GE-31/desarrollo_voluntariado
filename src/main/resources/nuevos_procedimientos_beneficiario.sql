-- PROCEDIMIENTO: CREAR BENEFICIARIO
DELIMITER $$
CREATE PROCEDURE sp_crear_beneficiario_nuevo (
    IN p_organizacion VARCHAR(255),
    IN p_direccion VARCHAR(255),
    IN p_distrito VARCHAR(100),
    IN p_necesidad_principal VARCHAR(100),
    IN p_observaciones TEXT,
    IN p_nombre_responsable VARCHAR(100),
    IN p_apellidos_responsable VARCHAR(100),
    IN p_dni VARCHAR(20),
    IN p_telefono VARCHAR(20),
    IN p_id_usuario INT
)
BEGIN
    INSERT INTO beneficiario (
        organizacion, direccion, distrito, necesidad_principal, observaciones,
        nombre_responsable, apellidos_responsable, dni, telefono, estado, id_usuario
    ) VALUES (
        p_organizacion, p_direccion, p_distrito, p_necesidad_principal, p_observaciones,
        p_nombre_responsable, p_apellidos_responsable, p_dni, p_telefono, 'ACTIVO', p_id_usuario
    );
    SELECT LAST_INSERT_ID() AS id_beneficiario;
END$$
DELIMITER ;

-- PROCEDIMIENTO: ACTUALIZAR BENEFICIARIO
DELIMITER $$
CREATE PROCEDURE sp_actualizar_beneficiario_nuevo (
    IN p_id_beneficiario INT,
    IN p_organizacion VARCHAR(255),
    IN p_direccion VARCHAR(255),
    IN p_distrito VARCHAR(100),
    IN p_necesidad_principal VARCHAR(100),
    IN p_observaciones TEXT,
    IN p_nombre_responsable VARCHAR(100),
    IN p_apellidos_responsable VARCHAR(100),
    IN p_dni VARCHAR(20),
    IN p_telefono VARCHAR(20)
)
BEGIN
    UPDATE beneficiario SET
        organizacion = p_organizacion,
        direccion = p_direccion,
        distrito = p_distrito,
        necesidad_principal = p_necesidad_principal,
        observaciones = p_observaciones,
        nombre_responsable = p_nombre_responsable,
        apellidos_responsable = p_apellidos_responsable,
        dni = p_dni,
        telefono = p_telefono
    WHERE id_beneficiario = p_id_beneficiario;
END$$
DELIMITER ;

-- PROCEDIMIENTO: CAMBIAR ESTADO BENEFICIARIO
DELIMITER $$
CREATE PROCEDURE sp_cambiar_estado_beneficiario_nuevo (
    IN p_id_beneficiario INT,
    IN p_estado VARCHAR(10)
)
BEGIN
    UPDATE beneficiario SET estado = p_estado WHERE id_beneficiario = p_id_beneficiario;
END$$
DELIMITER ;

-- PROCEDIMIENTO: ELIMINAR BENEFICIARIO
DELIMITER $$
CREATE PROCEDURE sp_eliminar_beneficiario_nuevo (
    IN p_id_beneficiario INT
)
BEGIN
    DELETE FROM beneficiario WHERE id_beneficiario = p_id_beneficiario;
END$$
DELIMITER ;

-- PROCEDIMIENTO: OBTENER TODOS LOS BENEFICIARIOS
DELIMITER $$
CREATE PROCEDURE sp_obtener_todos_beneficiarios_nuevo ()
BEGIN
    SELECT * FROM beneficiario ORDER BY creado_en DESC;
END$$
DELIMITER ;

-- PROCEDIMIENTO: OBTENER BENEFICIARIO POR ID
DELIMITER $$
CREATE PROCEDURE sp_obtener_beneficiario_por_id_nuevo (
    IN p_id_beneficiario INT
)
BEGIN
    SELECT * FROM beneficiario WHERE id_beneficiario = p_id_beneficiario;
END$$
DELIMITER ;
