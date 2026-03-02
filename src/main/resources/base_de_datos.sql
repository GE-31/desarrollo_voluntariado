-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 02-03-2026 a las 04:47:18
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00"; 


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `sistema_voluntariado`
--

DELIMITER $$
--
-- Procedimientos
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actividades_por_mes` ()   BEGIN
    SELECT 
        DATE_FORMAT(m.mes, '%Y-%m') AS mes,
        DATE_FORMAT(m.mes, '%b') AS nombre_mes,
        IFNULL(COUNT(a.id_actividad), 0) AS total_actividades
    FROM (
        SELECT DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL n MONTH), '%Y-%m-01') AS mes
        FROM (
            SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 
            UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
        ) nums
    ) m
    LEFT JOIN actividades a 
        ON DATE_FORMAT(a.fecha_inicio, '%Y-%m') = DATE_FORMAT(m.mes, '%Y-%m')
    GROUP BY m.mes
    ORDER BY m.mes ASC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizarDonacion` (IN `p_id_donacion` INT, IN `p_cantidad` DOUBLE, IN `p_descripcion` VARCHAR(150), IN `p_id_tipo_donacion` INT, IN `p_id_actividad` INT)   BEGIN
    UPDATE donacion
    SET cantidad = p_cantidad,
        descripcion = p_descripcion,
        id_tipo_donacion = p_id_tipo_donacion,
        id_actividad = p_id_actividad
    WHERE id_donacion = p_id_donacion;
    
    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizarMovimiento` (IN `p_id` INT, IN `p_tipo` VARCHAR(10), IN `p_monto` DECIMAL(12,2), IN `p_descripcion` VARCHAR(255), IN `p_categoria` VARCHAR(60), IN `p_comprobante` VARCHAR(100), IN `p_fecha` DATE, IN `p_id_actividad` INT)   BEGIN
    UPDATE movimiento_financiero
    SET tipo              = p_tipo,
        monto             = p_monto,
        descripcion       = p_descripcion,
        categoria         = p_categoria,
        comprobante       = p_comprobante,
        fecha_movimiento  = p_fecha,
        id_actividad      = NULLIF(p_id_actividad, 0)
    WHERE id_movimiento   = p_id;

    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_actividad` (IN `p_id` INT, IN `p_nombre` VARCHAR(200), IN `p_descripcion` TEXT, IN `p_fecha_inicio` DATE, IN `p_fecha_fin` DATE, IN `p_ubicacion` VARCHAR(300), IN `p_cupo_maximo` INT)   BEGIN
    UPDATE actividades
    SET nombre       = p_nombre,
        descripcion  = p_descripcion,
        fecha_inicio = p_fecha_inicio,
        fecha_fin    = p_fecha_fin,
        ubicacion    = p_ubicacion,
        cupo_maximo  = p_cupo_maximo
    WHERE id_actividad = p_id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_asistencia` (IN `p_id_asistencia` INT, IN `p_hora_entrada` TIME, IN `p_hora_salida` TIME, IN `p_estado` VARCHAR(20), IN `p_observaciones` TEXT)   BEGIN
    DECLARE v_horas DECIMAL(5,2) DEFAULT 0.00;

    IF p_hora_entrada IS NOT NULL AND p_hora_salida IS NOT NULL THEN
        SET v_horas = ROUND(TIMESTAMPDIFF(MINUTE, p_hora_entrada, p_hora_salida) / 60.0, 2);
        IF v_horas < 0 THEN
            SET v_horas = 0.00;
        END IF;
    END IF;

    UPDATE asistencias
    SET hora_entrada = p_hora_entrada,
        hora_salida  = p_hora_salida,
        horas_totales = v_horas,
        estado       = p_estado,
        observaciones = p_observaciones
    WHERE id_asistencia = p_id_asistencia;

    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_beneficiario_nuevo` (IN `p_id_beneficiario` INT, IN `p_organizacion` VARCHAR(255), IN `p_direccion` VARCHAR(255), IN `p_distrito` VARCHAR(100), IN `p_necesidad_principal` VARCHAR(100), IN `p_observaciones` TEXT, IN `p_nombre_responsable` VARCHAR(100), IN `p_apellidos_responsable` VARCHAR(100), IN `p_dni` VARCHAR(20), IN `p_telefono` VARCHAR(20))   BEGIN
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_cubierta_recurso_campana` (IN `p_id` INT, IN `p_cantidad_cubierta` DOUBLE)   BEGIN
    DECLARE v_requerida DOUBLE DEFAULT 0;
    DECLARE v_estado VARCHAR(20);

    SELECT cantidad_requerida INTO v_requerida
    FROM recurso_campana WHERE id_recurso_campana = p_id;

    IF p_cantidad_cubierta <= 0 THEN
        SET v_estado = 'PENDIENTE';
    ELSEIF p_cantidad_cubierta < v_requerida THEN
        SET v_estado = 'PARCIAL';
    ELSE
        SET v_estado = 'COMPLETO';
    END IF;

    UPDATE recurso_campana
    SET cantidad_cubierta = p_cantidad_cubierta, estado = v_estado
    WHERE id_recurso_campana = p_id;
    SELECT ROW_COUNT() AS filas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_detalle_especie` (IN `p_id_donacion` INT, IN `p_cantidad` DECIMAL(10,2), IN `p_observacion` VARCHAR(255))   BEGIN
    UPDATE donacion_detalle
    SET cantidad = p_cantidad,
        observacion = p_observacion
    WHERE id_donacion = p_id_donacion;

    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_donacion_inventario` (IN `p_id_donacion` INT, IN `p_cantidad` DECIMAL(10,2), IN `p_descripcion` VARCHAR(150), IN `p_subtipo_donacion` VARCHAR(50), IN `p_id_actividad` INT, IN `p_donacion_anonima` TINYINT, IN `p_donante_tipo` VARCHAR(20), IN `p_donante_nombre` VARCHAR(150), IN `p_donante_correo` VARCHAR(100), IN `p_donante_telefono` VARCHAR(30), IN `p_donante_dni` VARCHAR(20), IN `p_donante_ruc` VARCHAR(20), IN `p_id_usuario_edicion` INT, IN `p_motivo_edicion` VARCHAR(255))   BEGIN
    DECLARE v_tipo INT;
    DECLARE v_id_donante INT DEFAULT NULL;
    DECLARE v_tipo_donante VARCHAR(20) DEFAULT NULL;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT id_tipo_donacion INTO v_tipo
    FROM donacion
    WHERE id_donacion = p_id_donacion
      AND COALESCE(estado, 'ACTIVO') = 'ACTIVO'
    FOR UPDATE;

    IF v_tipo IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La donacion no existe o ya fue anulada.';
    END IF;

    IF v_tipo = 1 THEN
        IF p_cantidad IS NULL OR p_cantidad <= 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El monto para donaciones de dinero debe ser mayor a cero.';
        END IF;
    END IF;

    UPDATE donacion
    SET cantidad = CASE WHEN v_tipo = 1 THEN p_cantidad ELSE cantidad END,
        descripcion = p_descripcion,
        subtipo_donacion = NULLIF(TRIM(p_subtipo_donacion),''),
        id_actividad = p_id_actividad,
        actualizado_en = NOW()
    WHERE id_donacion = p_id_donacion;

    IF IFNULL(p_donacion_anonima, 0) = 1 THEN
        DELETE FROM donacion_donante WHERE id_donacion = p_id_donacion;
    ELSE
        IF p_donante_nombre IS NULL OR TRIM(p_donante_nombre) = '' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Debe indicar el nombre del donante o marcar donacion anonima.';
        END IF;

        SET v_tipo_donante = CASE UPPER(TRIM(IFNULL(p_donante_tipo, 'PERSONA')))
            WHEN 'EMPRESA' THEN 'Empresa'
            WHEN 'GRUPO' THEN 'Grupo'
            ELSE 'Persona'
        END;

        SELECT dnt.id_donante INTO v_id_donante
        FROM donante dnt
        WHERE LOWER(TRIM(dnt.nombre)) = LOWER(TRIM(p_donante_nombre))
          AND dnt.tipo = v_tipo_donante
          AND (
                IFNULL(TRIM(dnt.correo), '') = IFNULL(TRIM(p_donante_correo), '')
                OR IFNULL(TRIM(dnt.telefono), '') = IFNULL(TRIM(p_donante_telefono), '')
                OR IFNULL(TRIM(dnt.dni), '') = IFNULL(TRIM(p_donante_dni), '')
          )
        LIMIT 1;

        IF v_id_donante IS NULL THEN
            INSERT INTO donante(tipo, nombre, correo, telefono, dni, ruc)
            VALUES(v_tipo_donante, TRIM(p_donante_nombre), NULLIF(TRIM(p_donante_correo), ''), NULLIF(TRIM(p_donante_telefono), ''), NULLIF(TRIM(p_donante_dni), ''), NULLIF(TRIM(p_donante_ruc), ''));
            SET v_id_donante = LAST_INSERT_ID();
        END IF;

        DELETE FROM donacion_donante WHERE id_donacion = p_id_donacion;
        INSERT INTO donacion_donante(id_donacion, id_donante) VALUES(p_id_donacion, v_id_donante);
    END IF;

    COMMIT;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_foto_perfil` (IN `p_id_usuario` INT, IN `p_foto_perfil` VARCHAR(255))   BEGIN
    UPDATE usuario
    SET foto_perfil = p_foto_perfil,
        actualizado_en = NOW()
    WHERE id_usuario = p_id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_item_inventario` (IN `p_id_item` INT, IN `p_nombre` VARCHAR(150), IN `p_categoria` VARCHAR(50), IN `p_unidad_medida` VARCHAR(30), IN `p_stock_minimo` DECIMAL(10,2), IN `p_observacion` VARCHAR(255))   BEGIN
    UPDATE inventario_item
    SET nombre = TRIM(p_nombre),
        categoria = UPPER(TRIM(p_categoria)),
        unidad_medida = LOWER(TRIM(p_unidad_medida)),
        stock_minimo = IFNULL(p_stock_minimo, 0),
        observacion = p_observacion,
        actualizado_en = NOW()
    WHERE id_item = p_id_item;

    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_recurso_campana` (IN `p_id` INT, IN `p_id_actividad` INT, IN `p_id_item` INT, IN `p_cantidad_requerida` DOUBLE, IN `p_cantidad_cubierta` DOUBLE, IN `p_unidad` VARCHAR(50), IN `p_estado` VARCHAR(20), IN `p_observaciones` TEXT)   BEGIN
    UPDATE recurso_campana
    SET id_actividad = p_id_actividad,
        id_item = p_id_item,
        cantidad_requerida = p_cantidad_requerida,
        cantidad_cubierta = p_cantidad_cubierta,
        unidad = p_unidad,
        estado = p_estado,
        observaciones = p_observaciones
    WHERE id_recurso_campana = p_id;
    SELECT ROW_COUNT() AS filas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_salida_donacion` (IN `p_id_salida` INT, IN `p_id_actividad` INT, IN `p_cantidad` DOUBLE, IN `p_descripcion` TEXT, IN `p_id_item` INT, IN `p_cantidad_item` DOUBLE, IN `p_comprobante` VARCHAR(100))   BEGIN
    UPDATE salida_donacion
    SET id_actividad = p_id_actividad,
        cantidad = p_cantidad,
        descripcion = p_descripcion,
        id_item = IF(p_id_item = 0, NULL, p_id_item),
        cantidad_item = IF(p_cantidad_item = 0, NULL, p_cantidad_item),
        comprobante = NULLIF(p_comprobante, ''),
        actualizado_en = NOW()
    WHERE id_salida = p_id_salida
      AND estado = 'PENDIENTE';
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_usuario` (IN `p_id_usuario` INT, IN `p_nombres` VARCHAR(100), IN `p_apellidos` VARCHAR(100), IN `p_correo` VARCHAR(100), IN `p_username` VARCHAR(60), IN `p_dni` VARCHAR(20))   BEGIN
    UPDATE usuario
    SET nombres = p_nombres,
        apellidos = p_apellidos,
        correo = p_correo,
        username = p_username,
        dni = p_dni,
        actualizado_en = NOW()
    WHERE id_usuario = p_id_usuario;
    
    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_voluntario` (IN `p_id_voluntario` INT, IN `p_nombres` VARCHAR(100), IN `p_apellidos` VARCHAR(100), IN `p_dni` VARCHAR(20), IN `p_correo` VARCHAR(100), IN `p_telefono` VARCHAR(20), IN `p_carrera` VARCHAR(100))   BEGIN
    UPDATE voluntario
    SET nombres = p_nombres,
        apellidos = p_apellidos,
        dni = p_dni,
        correo = p_correo,
        telefono = p_telefono,
        carrera = p_carrera
    WHERE id_voluntario = p_id_voluntario;
    
    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_anular_certificado` (IN `p_id_certificado` INT, IN `p_motivo_anulacion` TEXT)   BEGIN
    UPDATE certificados
    SET 
        estado = 'ANULADO',
        fecha_anulacion = CURDATE(),
        motivo_anulacion = p_motivo_anulacion
    WHERE id_certificado = p_id_certificado;
    
    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_anular_donacion_inventario` (IN `p_id_donacion` INT, IN `p_id_usuario_anula` INT, IN `p_motivo` VARCHAR(255))   BEGIN
    DECLARE v_tipo INT;
    DECLARE v_item INT;
    DECLARE v_cantidad DECIMAL(10,2);
    DECLARE v_stock_anterior DECIMAL(10,2) DEFAULT 0;
    DECLARE v_stock_nuevo DECIMAL(10,2) DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT id_tipo_donacion
    INTO v_tipo
    FROM donacion
    WHERE id_donacion = p_id_donacion
    FOR UPDATE;

    IF v_tipo IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La donacion no existe.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM donacion
        WHERE id_donacion = p_id_donacion
          AND COALESCE(estado, 'ACTIVO') = 'ANULADO'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La donacion ya esta anulada.';
    END IF;

    IF v_tipo = 2 THEN
        SELECT id_item, cantidad
        INTO v_item, v_cantidad
        FROM donacion_detalle
        WHERE id_donacion = p_id_donacion
        LIMIT 1;

        IF v_item IS NOT NULL AND v_cantidad IS NOT NULL AND v_cantidad > 0 THEN
            SELECT stock_actual INTO v_stock_anterior
            FROM inventario_item
            WHERE id_item = v_item
            FOR UPDATE;

            IF v_stock_anterior < v_cantidad THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No hay stock suficiente para revertir la donacion.';
            END IF;

            SET v_stock_nuevo = v_stock_anterior - v_cantidad;

            UPDATE inventario_item
            SET stock_actual = v_stock_nuevo,
                actualizado_en = NOW()
            WHERE id_item = v_item;

            INSERT INTO inventario_movimiento(
                id_item, tipo_movimiento, motivo, cantidad, stock_anterior, stock_nuevo,
                id_referencia, tabla_referencia, observacion, id_usuario, creado_en
            ) VALUES(
                v_item, 'SALIDA', 'ANULACION_DONACION', v_cantidad, v_stock_anterior, v_stock_nuevo,
                p_id_donacion, 'donacion', CONCAT('Anulacion de donacion #', p_id_donacion, '. ', IFNULL(p_motivo, '')), p_id_usuario_anula, NOW()
            );
        END IF;
    END IF;

    UPDATE donacion
    SET estado = 'ANULADO',
        anulado_en = NOW(),
        id_usuario_anula = p_id_usuario_anula,
        motivo_anulacion = LEFT(IFNULL(p_motivo, 'Anulacion manual'), 255),
        actualizado_en = NOW()
    WHERE id_donacion = p_id_donacion;

    COMMIT;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_anular_salida_donacion` (IN `p_id_salida` INT, IN `p_id_usuario` INT, IN `p_motivo` VARCHAR(250))   BEGIN
    UPDATE salida_donacion
    SET estado = 'ANULADO',
        anulado_en = NOW(),
        id_usuario_anula = p_id_usuario,
        motivo_anulacion = p_motivo
    WHERE id_salida = p_id_salida
      AND estado != 'ANULADO';
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_anular_salida_inventario` (IN `p_id_salida_inv` INT, IN `p_id_usuario` INT, IN `p_motivo` VARCHAR(255))   BEGIN
    
    UPDATE inventario_item i
    JOIN salida_inventario_detalle d ON i.id_item = d.id_item
    SET i.stock_actual = i.stock_actual + d.cantidad
    WHERE d.id_salida_inv = p_id_salida_inv;

    
    UPDATE salida_inventario
    SET estado = 'ANULADO',
        anulado_en = NOW(),
        motivo_anulacion = p_motivo
    WHERE id_salida_inv = p_id_salida_inv;

    SELECT 1 AS resultado;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_buscar_donaciones_disponibles` (IN `p_query` VARCHAR(100))   BEGIN
    SET @buscar = CONVERT(p_query USING utf8mb4) COLLATE utf8mb4_general_ci;
    SELECT
        d.id_donacion,
        d.cantidad AS cantidad_original,
        d.cantidad - COALESCE(
            (SELECT SUM(s.cantidad) FROM salida_donacion s
             WHERE s.id_donacion = d.id_donacion AND s.estado != 'ANULADO'), 0
        ) AS saldo_disponible,
        d.descripcion,
        td.nombre AS tipo_donacion,
        td.id_tipo_donacion,
        COALESCE(a.nombre, 'Sin actividad') AS actividad_origen,
        COALESCE(dn.nombre, 'AN├ôNIMO') AS donante
    FROM donacion d
    INNER JOIN tipo_donacion td ON td.id_tipo_donacion = d.id_tipo_donacion
    LEFT JOIN actividades a ON a.id_actividad = d.id_actividad
    LEFT JOIN donacion_donante dd ON dd.id_donacion = d.id_donacion
    LEFT JOIN donante dn ON dn.id_donante = dd.id_donante
    WHERE d.estado IN ('CONFIRMADO', 'ACTIVO')
      AND (
          CAST(d.id_donacion AS CHAR) LIKE CONCAT('%', @buscar, '%')
          OR COALESCE(dn.nombre, '') COLLATE utf8mb4_general_ci LIKE CONCAT('%', @buscar, '%')
          OR CAST(d.cantidad AS CHAR) LIKE CONCAT('%', @buscar, '%')
          OR COALESCE(d.descripcion, '') COLLATE utf8mb4_general_ci LIKE CONCAT('%', @buscar, '%')
      )
    HAVING saldo_disponible > 0
    ORDER BY d.registrado_en DESC
    LIMIT 20;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_cambiar_estado_actividad` (IN `p_id` INT, IN `p_estado` VARCHAR(20))   BEGIN
    UPDATE actividades
    SET estado = p_estado
    WHERE id_actividad = p_id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_cambiar_estado_beneficiario` (IN `p_id_beneficiario` INT, IN `p_estado` VARCHAR(10))   BEGIN
    UPDATE beneficiario
    SET estado = p_estado
    WHERE id_beneficiario = p_id_beneficiario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_cambiar_estado_beneficiario_nuevo` (IN `p_id_beneficiario` INT, IN `p_estado` VARCHAR(10))   BEGIN
    UPDATE beneficiario SET estado = p_estado WHERE id_beneficiario = p_id_beneficiario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_cambiar_estado_donacion` (IN `p_id_donacion` INT, IN `p_estado` VARCHAR(20))   BEGIN
    UPDATE donacion
    SET estado = p_estado,
        actualizado_en = NOW()
    WHERE id_donacion = p_id_donacion;

    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_cambiar_estado_inventario` (IN `p_id_item` INT, IN `p_estado` VARCHAR(20))   BEGIN
    UPDATE inventario_item
    SET estado = UPPER(p_estado),
        actualizado_en = NOW()
    WHERE id_item = p_id_item;

    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_cambiar_estado_salida` (IN `p_id_salida` INT, IN `p_estado` VARCHAR(20))   BEGIN
    UPDATE salida_donacion
    SET estado = p_estado,
        actualizado_en = NOW()
    WHERE id_salida = p_id_salida;
    SELECT ROW_COUNT() AS affected;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_cambiar_estado_usuario` (IN `p_id_usuario` INT, IN `p_estado` VARCHAR(20))   BEGIN
    UPDATE usuario
    SET estado = p_estado,
        actualizado_en = NOW()
    WHERE id_usuario = p_id_usuario;
    
    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_cambiar_estado_voluntario` (IN `p_id_voluntario` INT, IN `p_estado` VARCHAR(20))   BEGIN
    UPDATE voluntario
    SET estado = p_estado
    WHERE id_voluntario = p_id_voluntario;
    
    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_certificados_por_voluntario` (IN `p_id_voluntario` INT)   BEGIN
    SELECT 
        c.id_certificado,
        c.codigo_certificado,
        c.id_voluntario,
        c.id_actividad,
        c.horas_voluntariado,
        c.fecha_emision,
        c.estado,
        c.observaciones,
        c.id_usuario_emite,
        CONCAT(v.nombres, ' ', v.apellidos) AS nombre_voluntario,
        v.dni AS dni_voluntario,
        a.nombre AS nombre_actividad,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuario_emite
    FROM certificados c
    INNER JOIN voluntario v ON c.id_voluntario = v.id_voluntario
    INNER JOIN actividades a ON c.id_actividad = a.id_actividad
    INNER JOIN usuario u ON c.id_usuario_emite = u.id_usuario
    WHERE c.id_voluntario = p_id_voluntario
    ORDER BY c.fecha_emision DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_contar_notificaciones_no_leidas` (IN `p_id_usuario` INT)   BEGIN
    SELECT COUNT(*) AS total FROM notificaciones
    WHERE id_usuario = p_id_usuario AND leida = 0;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_contar_stock_bajo` ()   BEGIN
    SELECT COUNT(*) AS total
    FROM inventario_item
    WHERE estado = 'ACTIVO' AND stock_actual <= stock_minimo;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_actividad` (IN `p_nombre` VARCHAR(200), IN `p_descripcion` TEXT, IN `p_fecha_inicio` DATE, IN `p_fecha_fin` DATE, IN `p_ubicacion` VARCHAR(300), IN `p_cupo_maximo` INT, IN `p_id_usuario` INT)   BEGIN
    INSERT INTO actividades (nombre, descripcion, fecha_inicio, fecha_fin, ubicacion, cupo_maximo, id_usuario)
    VALUES (p_nombre, p_descripcion, p_fecha_inicio, p_fecha_fin, p_ubicacion, p_cupo_maximo, p_id_usuario);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_beneficiario_nuevo` (IN `p_organizacion` VARCHAR(255), IN `p_direccion` VARCHAR(255), IN `p_distrito` VARCHAR(100), IN `p_necesidad_principal` VARCHAR(100), IN `p_observaciones` TEXT, IN `p_nombre_responsable` VARCHAR(100), IN `p_apellidos_responsable` VARCHAR(100), IN `p_dni` VARCHAR(20), IN `p_telefono` VARCHAR(20), IN `p_id_usuario` INT)   BEGIN
    INSERT INTO beneficiario (
        organizacion, direccion, distrito, necesidad_principal, observaciones,
        nombre_responsable, apellidos_responsable, dni, telefono, estado, id_usuario
    ) VALUES (
        p_organizacion, p_direccion, p_distrito, p_necesidad_principal, p_observaciones,
        p_nombre_responsable, p_apellidos_responsable, p_dni, p_telefono, 'ACTIVO', p_id_usuario
    );
    SELECT LAST_INSERT_ID() AS id_beneficiario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_certificado` (IN `p_codigo_certificado` VARCHAR(50), IN `p_id_voluntario` INT, IN `p_id_actividad` INT, IN `p_horas_voluntariado` INT, IN `p_observaciones` TEXT, IN `p_id_usuario_emite` INT)   BEGIN
    
    DECLARE v_codigo VARCHAR(50);
    DECLARE v_anio INT;
    DECLARE v_secuencia INT;
    
    IF p_codigo_certificado IS NULL OR p_codigo_certificado = '' THEN
        SET v_anio = YEAR(CURDATE());
        
        
        SELECT IFNULL(MAX(CAST(SUBSTRING_INDEX(codigo_certificado, '-', -1) AS UNSIGNED)), 0) + 1
        INTO v_secuencia
        FROM certificados
        WHERE codigo_certificado LIKE CONCAT('CERT-', v_anio, '-%');
        
        SET v_codigo = CONCAT('CERT-', v_anio, '-', LPAD(v_secuencia, 4, '0'));
    ELSE
        SET v_codigo = p_codigo_certificado;
    END IF;
    
    INSERT INTO certificados (
        codigo_certificado,
        id_voluntario,
        id_actividad,
        horas_voluntariado,
        fecha_emision,
        estado,
        observaciones,
        id_usuario_emite
    ) VALUES (
        v_codigo,
        p_id_voluntario,
        p_id_actividad,
        p_horas_voluntariado,
        CURDATE(),
        'EMITIDO',
        p_observaciones,
        p_id_usuario_emite
    );
    
    SELECT LAST_INSERT_ID() AS id_certificado, v_codigo AS codigo_certificado;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_evento` (IN `p_titulo` VARCHAR(200), IN `p_descripcion` TEXT, IN `p_fecha_inicio` DATE, IN `p_fecha_fin` DATE, IN `p_color` VARCHAR(20), IN `p_id_usuario` INT)   BEGIN
    INSERT INTO eventos_calendario (titulo, descripcion, fecha_inicio, fecha_fin, color, id_usuario)
    VALUES (p_titulo, p_descripcion, p_fecha_inicio, p_fecha_fin, IFNULL(p_color, '#6366f1'), p_id_usuario);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_item_inventario` (IN `p_nombre` VARCHAR(150), IN `p_categoria` VARCHAR(50), IN `p_unidad_medida` VARCHAR(30), IN `p_stock_minimo` DECIMAL(10,2), IN `p_observacion` VARCHAR(255))   BEGIN
    INSERT INTO inventario_item(nombre, categoria, unidad_medida, stock_actual, stock_minimo, estado, observacion, creado_en, actualizado_en)
    VALUES(TRIM(p_nombre), UPPER(TRIM(p_categoria)), LOWER(TRIM(p_unidad_medida)), 0, IFNULL(p_stock_minimo, 0), 'ACTIVO', p_observacion, NOW(), NOW());

    SELECT LAST_INSERT_ID() AS id_item;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_notificacion` (IN `p_id_usuario` INT, IN `p_tipo` VARCHAR(30), IN `p_titulo` VARCHAR(200), IN `p_mensaje` TEXT, IN `p_icono` VARCHAR(50), IN `p_color` VARCHAR(20), IN `p_referencia_id` INT)   BEGIN
    INSERT INTO notificaciones (id_usuario, tipo, titulo, mensaje, icono, color, referencia_id)
    VALUES (p_id_usuario, p_tipo, p_titulo, p_mensaje, p_icono, p_color, p_referencia_id);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_usuario` (IN `p_nombres` VARCHAR(100), IN `p_apellidos` VARCHAR(100), IN `p_correo` VARCHAR(100), IN `p_username` VARCHAR(60), IN `p_dni` VARCHAR(20), IN `p_password_hash` VARCHAR(255))   BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        -- Manejo de errores: rollback en caso de fallo
        ROLLBACK;
    END;

    START TRANSACTION;

    -- Validar si el usuario ya existe por username, correo o DNI
    IF EXISTS (SELECT 1 FROM usuario WHERE username = p_username OR correo = p_correo OR dni = p_dni) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El usuario, correo o DNI ya existe';
    ELSE
        -- Insertar el nuevo usuario
        INSERT INTO usuario (nombres, apellidos, correo, username, dni, password_hash, estado, creado_en)
        VALUES (p_nombres, p_apellidos, p_correo, p_username, p_dni, p_password_hash, 'ACTIVO', NOW());
    END IF;

    -- Devolver el ID del usuario insertado
    SELECT LAST_INSERT_ID() AS id_usuario;

    COMMIT;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_voluntario` (IN `p_nombres` VARCHAR(100), IN `p_apellidos` VARCHAR(100), IN `p_dni` VARCHAR(20), IN `p_correo` VARCHAR(100), IN `p_telefono` VARCHAR(20), IN `p_carrera` VARCHAR(100), IN `p_id_usuario` INT, IN `p_cargo` VARCHAR(50), IN `p_acceso_sistema` TINYINT)   BEGIN
    INSERT INTO voluntario (nombres, apellidos, dni, correo, telefono, carrera, cargo, acceso_sistema, estado, id_usuario)
    VALUES (p_nombres, p_apellidos, p_dni, p_correo, p_telefono, p_carrera,
            IFNULL(p_cargo, 'Voluntario'),
            IFNULL(p_acceso_sistema, 0),
            'ACTIVO', 
            IF(p_id_usuario > 0, p_id_usuario, NULL));
    
    SELECT LAST_INSERT_ID() AS id_voluntario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_dashboard_actividades_por_mes` ()   BEGIN
    SELECT
        DATE_FORMAT(m.mes, '%b %Y') AS label,
        COALESCE(COUNT(a.id_actividad), 0) AS total
    FROM (
        SELECT DATE_FORMAT(CURDATE() - INTERVAL n MONTH, '%Y-%m-01') AS mes
        FROM (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2
              UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) nums
    ) m
    LEFT JOIN actividades a
        ON DATE_FORMAT(a.fecha_inicio, '%Y-%m') = DATE_FORMAT(m.mes, '%Y-%m')
    GROUP BY m.mes
    ORDER BY m.mes;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_dashboard_estadisticas` ()   BEGIN
    SELECT
        (SELECT COUNT(*) FROM voluntario) AS total_voluntarios,
        (SELECT COUNT(*) FROM voluntario WHERE estado = 'ACTIVO') AS voluntarios_activos,
        (SELECT COUNT(*) FROM voluntario WHERE estado = 'INACTIVO') AS voluntarios_inactivos,
        (SELECT COUNT(*) FROM actividades) AS total_actividades,
        (SELECT COUNT(*) FROM donacion WHERE estado = 'CONFIRMADO') AS total_donaciones,
        COALESCE((SELECT SUM(d.cantidad)
                  FROM donacion d
                  INNER JOIN tipo_donacion td ON d.id_tipo_donacion = td.id_tipo_donacion
                  WHERE td.nombre = 'DINERO' AND d.estado = 'CONFIRMADO'), 0) AS monto_donaciones,
        (SELECT COUNT(*) FROM beneficiario) AS total_beneficiarios;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_dashboard_horas_por_actividad` ()   BEGIN
    SELECT
        act.nombre AS label,
        COALESCE(SUM(a.horas_totales), 0) AS total_horas
    FROM actividades act
    INNER JOIN asistencias a ON act.id_actividad = a.id_actividad
    WHERE a.estado IN ('ASISTIO', 'TARDANZA')
    GROUP BY act.id_actividad, act.nombre
    ORDER BY total_horas DESC
    LIMIT 5;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_dashboard_proxima_actividad` ()   BEGIN
    SELECT
        nombre,
        DATE_FORMAT(fecha_inicio, '%Y-%m-%d') AS fecha,
        ubicacion
    FROM actividades
    WHERE fecha_inicio >= CURDATE()
      AND estado = 'ACTIVO'
    ORDER BY fecha_inicio ASC
    LIMIT 1;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_dashboard_total_horas` ()   BEGIN
    SELECT COALESCE(SUM(horas_totales), 0) AS total_horas
    FROM asistencias
    WHERE estado IN ('ASISTIO', 'TARDANZA');
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_donacionesPorCampana` ()   BEGIN
    SELECT
        COALESCE(a.nombre, 'Sin actividad') AS campana,
        SUM(CASE WHEN d.estado = 'CONFIRMADO' THEN d.cantidad ELSE 0 END) AS monto_confirmado,
        SUM(CASE WHEN d.estado = 'PENDIENTE'  THEN d.cantidad ELSE 0 END) AS monto_pendiente,
        COUNT(*) AS total_donaciones
    FROM donacion d
    LEFT JOIN actividades a ON d.id_actividad = a.id_actividad
    WHERE d.id_tipo_donacion = 1
      AND d.estado IN ('CONFIRMADO', 'PENDIENTE')
    GROUP BY a.nombre
    ORDER BY monto_confirmado DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_donaciones_disponibles_tesoreria` (IN `p_busqueda` VARCHAR(200))   BEGIN
    SELECT 
        d.id_donacion,
        d.cantidad AS monto_original,
        IFNULL(dn.nombre, 'ANONIMO') AS donante,
        dn.dni,
        dn.ruc,
        a.nombre AS actividad_origen,
        (d.cantidad 
            - COALESCE((SELECT SUM(sd2.cantidad) FROM salida_donacion sd2 WHERE sd2.id_donacion = d.id_donacion AND sd2.estado = 'CONFIRMADO' AND sd2.tipo_salida = 'DINERO'), 0)
            - COALESCE((SELECT SUM(gd2.monto) FROM gasto_donacion gd2 WHERE gd2.id_donacion = d.id_donacion), 0)
        ) AS saldo_disponible
    FROM donacion d
    LEFT JOIN donacion_donante dd ON dd.id_donacion = d.id_donacion
    LEFT JOIN donante dn ON dn.id_donante = dd.id_donante
    LEFT JOIN actividades a ON a.id_actividad = d.id_actividad
    WHERE d.estado = 'CONFIRMADO'
      AND d.cantidad IS NOT NULL
      AND d.cantidad > 0
    HAVING saldo_disponible > 0
    ORDER BY d.id_donacion DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminarDonacion` (IN `p_id_donacion` INT)   BEGIN
    DELETE FROM donacion WHERE id_donacion = p_id_donacion;
    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminarMovimiento` (IN `p_id` INT)   BEGIN
    DELETE FROM movimiento_financiero WHERE id_movimiento = p_id;
    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_actividad` (IN `p_id` INT)   BEGIN
    DELETE FROM actividades WHERE id_actividad = p_id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_asistencia` (IN `p_id_asistencia` INT)   BEGIN
    DELETE FROM asistencias WHERE id_asistencia = p_id_asistencia;
    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_beneficiario` (IN `p_id_beneficiario` INT)   BEGIN
    DELETE FROM beneficiario WHERE id_beneficiario = p_id_beneficiario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_beneficiario_nuevo` (IN `p_id_beneficiario` INT)   BEGIN
    DELETE FROM beneficiario WHERE id_beneficiario = p_id_beneficiario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_evento` (IN `p_id_evento` INT)   BEGIN
    DELETE FROM eventos_calendario WHERE id_evento = p_id_evento;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_notificacion` (IN `p_id` INT)   BEGIN
    DELETE FROM notificaciones WHERE id_notificacion = p_id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_permisos_usuario` (IN `p_id_usuario` INT)   BEGIN
    DELETE FROM usuario_permiso WHERE id_usuario = p_id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_recurso_campana` (IN `p_id` INT)   BEGIN
    DELETE FROM recurso_campana WHERE id_recurso_campana = p_id;
    SELECT ROW_COUNT() AS filas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_todas_notificaciones` (IN `p_id_usuario` INT)   BEGIN
    DELETE FROM notificaciones WHERE id_usuario = p_id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_usuario` (IN `p_id_usuario` INT)   BEGIN
    DELETE FROM usuario WHERE id_usuario = p_id_usuario;
    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_voluntario` (IN `p_id_voluntario` INT)   BEGIN
    DELETE FROM voluntario WHERE id_voluntario = p_id_voluntario;
    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_estadisticas_asistencias` ()   BEGIN
    SELECT
        COUNT(*) AS total_registros,
        SUM(CASE WHEN estado = 'ASISTIO' THEN 1 ELSE 0 END) AS total_asistieron,
        SUM(CASE WHEN estado = 'FALTA' THEN 1 ELSE 0 END) AS total_faltas,
        SUM(CASE WHEN estado = 'TARDANZA' THEN 1 ELSE 0 END) AS total_tardanzas,
        IFNULL(SUM(horas_totales), 0) AS total_horas,
        COUNT(DISTINCT id_voluntario) AS voluntarios_unicos,
        COUNT(DISTINCT id_actividad) AS actividades_registradas
    FROM asistencias;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_estadisticas_certificados` ()   BEGIN
    SELECT 
        COUNT(*) AS total_certificados,
        SUM(CASE WHEN estado = 'EMITIDO' THEN 1 ELSE 0 END) AS total_emitidos,
        SUM(CASE WHEN estado = 'ANULADO' THEN 1 ELSE 0 END) AS total_anulados,
        SUM(CASE WHEN estado = 'EMITIDO' THEN horas_voluntariado ELSE 0 END) AS total_horas_certificadas,
        COUNT(DISTINCT id_voluntario) AS voluntarios_certificados,
        COUNT(DISTINCT id_actividad) AS actividades_certificadas
    FROM certificados;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_filtrarMovimientos` (IN `p_tipo` VARCHAR(10), IN `p_categoria` VARCHAR(60), IN `p_fecha_ini` DATE, IN `p_fecha_fin` DATE, IN `p_busqueda` VARCHAR(200))   BEGIN
    SELECT m.id_movimiento, m.tipo, m.monto, m.descripcion,
           m.categoria, m.comprobante, m.fecha_movimiento,
           IFNULL(a.nombre, 'ù') AS actividad,
           m.id_actividad,
           CONCAT(u.nombres, ' ', u.apellidos) AS usuario_registro,
           m.creado_en
    FROM movimiento_financiero m
    INNER JOIN usuario u ON m.id_usuario = u.id_usuario
    LEFT JOIN actividades a ON m.id_actividad = a.id_actividad
    WHERE (p_tipo IS NULL      OR p_tipo = ''      OR m.tipo = p_tipo)
      AND (p_categoria IS NULL OR p_categoria = '' OR m.categoria = p_categoria)
      AND (p_fecha_ini IS NULL OR m.fecha_movimiento >= p_fecha_ini)
      AND (p_fecha_fin IS NULL OR m.fecha_movimiento <= p_fecha_fin)
      AND (p_busqueda IS NULL  OR p_busqueda = ''
           OR m.descripcion LIKE CONCAT('%', p_busqueda, '%')
           OR m.comprobante LIKE CONCAT('%', p_busqueda, '%')
           OR IFNULL(a.nombre, '') LIKE CONCAT('%', p_busqueda, '%')
           OR CONCAT(u.nombres, ' ', u.apellidos) LIKE CONCAT('%', p_busqueda, '%'))
    ORDER BY m.fecha_movimiento DESC, m.creado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_filtrar_inventario` (IN `p_q` VARCHAR(150), IN `p_categoria` VARCHAR(50), IN `p_estado` VARCHAR(20), IN `p_stock_bajo` TINYINT)   BEGIN
    SELECT id_item, nombre, categoria, unidad_medida, stock_actual, stock_minimo,
           estado, observacion, creado_en, actualizado_en
    FROM inventario_item
    WHERE (p_q IS NULL OR TRIM(p_q) = '' OR LOWER(nombre) LIKE CONCAT('%', LOWER(TRIM(p_q)), '%')
           OR LOWER(COALESCE(observacion, '')) LIKE CONCAT('%', LOWER(TRIM(p_q)), '%'))
      AND (p_categoria IS NULL OR TRIM(p_categoria) = '' OR categoria = TRIM(p_categoria))
      AND (p_estado IS NULL OR TRIM(p_estado) = '' OR estado = UPPER(TRIM(p_estado)))
      AND (p_stock_bajo = 0 OR stock_actual <= stock_minimo)
    ORDER BY creado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_generar_notificaciones_actividades_hoy` (IN `p_id_usuario` INT)   BEGIN
    
    
    INSERT INTO notificaciones (id_usuario, tipo, titulo, mensaje, icono, color, referencia_id)
    SELECT p_id_usuario, 'ACTIVIDAD_HOY',
           CONCAT('📋 Actividad hoy: ', a.nombre),
           CONCAT('La actividad "', a.nombre, '" está programada para hoy en ', IFNULL(a.ubicacion, 'ubicación por definir'), '.'),
           'fa-calendar-check', '#10b981', a.id_actividad
    FROM actividades a
    WHERE DATE(a.fecha_inicio) = CURDATE()
      AND a.estado = 'ACTIVO'
      AND NOT EXISTS (
          SELECT 1 FROM notificaciones n
          WHERE n.id_usuario = p_id_usuario
            AND n.tipo = 'ACTIVIDAD_HOY'
            AND n.referencia_id = a.id_actividad
            AND DATE(n.fecha_creacion) = CURDATE()
      );
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_generar_notificaciones_eventos_hoy` (IN `p_id_usuario` INT)   BEGIN
    INSERT INTO notificaciones (id_usuario, titulo, mensaje, tipo, leida, fecha_creacion)
    SELECT 
        p_id_usuario,
        CONCAT('Evento hoy: ', e.titulo),
        CONCAT('Tienes programado "', e.titulo, '" para hoy'),
        'EVENTO',
        0,
        NOW()
    FROM eventos_calendario e
    WHERE e.fecha_inicio = CURDATE()
      AND e.id_usuario = p_id_usuario
      AND NOT EXISTS (
          SELECT 1 FROM notificaciones n
          WHERE n.id_usuario = p_id_usuario
            AND n.tipo = 'EVENTO'
            AND n.titulo = CONCAT('Evento hoy: ', e.titulo)
            AND DATE(n.fecha_creacion) = CURDATE()
      );
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_generar_notif_dia_lleno` (IN `p_id_usuario` INT, IN `p_fecha` DATE)   BEGIN
    DECLARE v_count INT DEFAULT 0;
    SELECT COUNT(*) INTO v_count
    FROM eventos_calendario
    WHERE id_usuario = p_id_usuario AND DATE(fecha_inicio) = p_fecha;

    IF v_count >= 3 THEN
        IF NOT EXISTS (
            SELECT 1 FROM notificaciones
            WHERE id_usuario = p_id_usuario
              AND tipo = 'DIA_LLENO'
              AND DATE(fecha_creacion) = CURDATE()
              AND referencia_id = DATEDIFF(p_fecha, '2000-01-01')
        ) THEN
            INSERT INTO notificaciones (id_usuario, tipo, titulo, mensaje, icono, color, referencia_id)
            VALUES (
                p_id_usuario,
                'DIA_LLENO',
                CONCAT('Agenda llena: ', DATE_FORMAT(p_fecha, '%d/%m/%Y')),
                CONCAT('Tienes ', v_count, ' eventos agendados para el ', DATE_FORMAT(p_fecha, '%d/%m/%Y'), '. El dia esta completo.'),
                'fa-calendar-days',
                '#f59e0b',
                DATEDIFF(p_fecha, '2000-01-01')
            );
        END IF;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_guardarDonacion` (IN `p_cantidad` DOUBLE, IN `p_descripcion` VARCHAR(150), IN `p_id_tipo_donacion` INT, IN `p_id_actividad` INT, IN `p_id_usuario_registro` INT)   BEGIN
    INSERT INTO donacion (cantidad, descripcion, id_tipo_donacion, id_actividad, id_usuario_registro, registrado_en)
    VALUES (p_cantidad, p_descripcion, p_id_tipo_donacion, p_id_actividad, p_id_usuario_registro, NOW());
    
    SELECT LAST_INSERT_ID() AS id_donacion;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_guardar_permisos_usuario` (IN `p_id_usuario` INT, IN `p_ids_permisos` TEXT)   BEGIN
    
    DELETE FROM usuario_permiso WHERE id_usuario = p_id_usuario;

    
    IF p_ids_permisos IS NOT NULL AND p_ids_permisos != '' THEN
        SET @sql = CONCAT(
            'INSERT INTO usuario_permiso (id_usuario, id_permiso) ',
            'SELECT ', p_id_usuario, ', id_permiso FROM permiso ',
            'WHERE FIND_IN_SET(id_permiso, ''', p_ids_permisos, ''')'
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_horas_voluntarias_por_actividad` ()   BEGIN
    SELECT 
        act.nombre AS nombre_actividad,
        IFNULL(SUM(a.horas_totales), 0) AS total_horas
    FROM asistencias a
    INNER JOIN actividades act ON a.id_actividad = act.id_actividad
    WHERE a.estado IN ('ASISTIO', 'TARDANZA')
    GROUP BY act.id_actividad, act.nombre
    ORDER BY total_horas DESC
    LIMIT 5;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_limpiar_notificaciones_antiguas` ()   BEGIN
    DELETE FROM notificaciones WHERE fecha_creacion < DATE_SUB(NOW(), INTERVAL 30 DAY);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listarDonaciones` ()   BEGIN
    SELECT 
        d.id_donacion,
        d.cantidad,
        d.descripcion,
        td.nombre AS tipoDonacion,
        a.nombre AS actividad,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuarioRegistro,
        d.registrado_en,
        d.id_tipo_donacion,
        d.id_actividad
    FROM donacion d
    LEFT JOIN tipo_donacion td ON d.id_tipo_donacion = td.id_tipo_donacion
    LEFT JOIN actividades a ON d.id_actividad = a.id_actividad
    LEFT JOIN usuario u ON d.id_usuario_registro = u.id_usuario
    ORDER BY d.registrado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listarMovimientos` ()   BEGIN
    SELECT m.id_movimiento, m.tipo, m.monto, m.descripcion,
           m.categoria, m.comprobante, m.fecha_movimiento,
           IFNULL(a.nombre, '???') AS actividad,
           m.id_actividad,
           CONCAT(u.nombres, ' ', u.apellidos) AS usuario_registro,
           m.creado_en
    FROM movimiento_financiero m
    INNER JOIN usuario u ON m.id_usuario = u.id_usuario
    LEFT JOIN actividades a ON m.id_actividad = a.id_actividad
    ORDER BY m.fecha_movimiento DESC, m.creado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_asistencias` ()   BEGIN
    SELECT
        a.id_asistencia,
        a.id_voluntario,
        CONCAT(v.nombres, ' ', v.apellidos) AS nombre_voluntario,
        v.dni AS dni_voluntario,
        a.id_actividad,
        act.nombre AS nombre_actividad,
        a.fecha,
        a.hora_entrada,
        a.hora_salida,
        a.horas_totales,
        a.estado,
        a.observaciones,
        a.id_usuario_registro,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuario_registro,
        a.creado_en
    FROM asistencias a
    INNER JOIN voluntario v   ON a.id_voluntario = v.id_voluntario
    INNER JOIN actividades act ON a.id_actividad  = act.id_actividad
    LEFT JOIN usuario u       ON a.id_usuario_registro = u.id_usuario
    ORDER BY a.fecha DESC, a.creado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_asistencias_por_actividad` (IN `p_id_actividad` INT)   BEGIN
    SELECT
        a.id_asistencia,
        a.id_voluntario,
        CONCAT(v.nombres, ' ', v.apellidos) AS nombre_voluntario,
        v.dni AS dni_voluntario,
        a.id_actividad,
        act.nombre AS nombre_actividad,
        a.fecha,
        a.hora_entrada,
        a.hora_salida,
        a.horas_totales,
        a.estado,
        a.observaciones,
        a.creado_en
    FROM asistencias a
    INNER JOIN voluntario v   ON a.id_voluntario = v.id_voluntario
    INNER JOIN actividades act ON a.id_actividad  = act.id_actividad
    WHERE a.id_actividad = p_id_actividad
    ORDER BY a.fecha DESC, v.apellidos, v.nombres;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_asistencias_por_voluntario` (IN `p_id_voluntario` INT)   BEGIN
    SELECT
        a.id_asistencia,
        a.id_voluntario,
        CONCAT(v.nombres, ' ', v.apellidos) AS nombre_voluntario,
        a.id_actividad,
        act.nombre AS nombre_actividad,
        a.fecha,
        a.hora_entrada,
        a.hora_salida,
        a.horas_totales,
        a.estado,
        a.observaciones,
        a.creado_en
    FROM asistencias a
    INNER JOIN voluntario v   ON a.id_voluntario = v.id_voluntario
    INNER JOIN actividades act ON a.id_actividad  = act.id_actividad
    WHERE a.id_voluntario = p_id_voluntario
    ORDER BY a.fecha DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_certificados` ()   BEGIN
    SELECT 
        c.id_certificado,
        c.codigo_certificado,
        c.id_voluntario,
        c.id_actividad,
        c.horas_voluntariado,
        c.fecha_emision,
        c.estado,
        c.observaciones,
        c.id_usuario_emite,
        CONCAT(v.nombres, ' ', v.apellidos) AS nombre_voluntario,
        v.dni AS dni_voluntario,
        a.nombre AS nombre_actividad,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuario_emite
    FROM certificados c
    INNER JOIN voluntario v ON c.id_voluntario = v.id_voluntario
    INNER JOIN actividades a ON c.id_actividad = a.id_actividad
    INNER JOIN usuario u ON c.id_usuario_emite = u.id_usuario
    ORDER BY c.fecha_emision DESC, c.id_certificado DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_donaciones_con_detalle` ()   BEGIN
    SELECT
        d.id_donacion,
        d.cantidad,
        d.descripcion,
        d.id_tipo_donacion,
        d.id_actividad,
        d.id_usuario_registro,
        d.registrado_en,
        td.nombre AS tipoDonacion,
        a.nombre AS actividad,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuarioRegistro,
        COALESCE(dnt.nombre, 'ANONIMO') AS donanteNombre,
        d.estado,
        dnt.tipo AS tipoDonante,
        ddet.id_item,
        ddet.cantidad AS cantidad_item,
        ii.nombre AS item_nombre,
        ii.unidad_medida AS item_unidad_medida,
        dnt.dni AS dniDonante,
        dnt.ruc AS rucDonante,
        dnt.correo AS correoDonante,
        dnt.telefono AS telefonoDonante,
        d.subtipo_donacion AS subtipoDonacion
    FROM donacion d
    LEFT JOIN tipo_donacion td ON d.id_tipo_donacion = td.id_tipo_donacion
    LEFT JOIN actividades a ON d.id_actividad = a.id_actividad
    LEFT JOIN usuario u ON d.id_usuario_registro = u.id_usuario
    LEFT JOIN donacion_donante ddon ON d.id_donacion = ddon.id_donacion
    LEFT JOIN donante dnt ON ddon.id_donante = dnt.id_donante
    LEFT JOIN donacion_detalle ddet ON d.id_donacion = ddet.id_donacion
    LEFT JOIN inventario_item ii ON ddet.id_item = ii.id_item
    ORDER BY d.registrado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_donaciones_disponibles` ()   BEGIN
    SELECT
        d.id_donacion,
        d.cantidad,
        d.descripcion,
        td.nombre AS tipo_donacion,
        COALESCE(a.nombre, 'Sin actividad') AS actividad_origen,
        COALESCE(dn.nombre, 'AN├ôNIMO') AS donante,
        d.estado,
        d.id_tipo_donacion
    FROM donacion d
    INNER JOIN tipo_donacion td ON td.id_tipo_donacion = d.id_tipo_donacion
    LEFT JOIN actividades a ON a.id_actividad = d.id_actividad
    LEFT JOIN donacion_donante dd ON dd.id_donacion = d.id_donacion
    LEFT JOIN donante dn ON dn.id_donante = dd.id_donante
    WHERE d.estado IN ('CONFIRMADO', 'ACTIVO')
    ORDER BY d.registrado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_eventos` ()   BEGIN
    SELECT id_evento, titulo, descripcion, fecha_inicio, fecha_fin, color, id_usuario, creado_en
    FROM eventos_calendario
    ORDER BY fecha_inicio DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_inventario` ()   BEGIN
    SELECT id_item, nombre, categoria, unidad_medida, stock_actual, stock_minimo,
           estado, observacion, creado_en, actualizado_en
    FROM inventario_item
    ORDER BY creado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_items_disponibles_salida` ()   BEGIN
    SELECT id_item, nombre, categoria, unidad_medida, stock_actual, estado
    FROM inventario_item
    WHERE estado = 'ACTIVO' AND stock_actual > 0
    ORDER BY categoria, nombre;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_notificaciones` (IN `p_id_usuario` INT)   BEGIN
    SELECT id_notificacion, id_usuario, tipo, titulo, mensaje, icono, color,
           leida, referencia_id, fecha_creacion
    FROM notificaciones
    WHERE id_usuario = p_id_usuario
    ORDER BY fecha_creacion DESC
    LIMIT 20;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_recursos_campana` ()   BEGIN
    SELECT rc.id_recurso_campana, rc.id_actividad, rc.id_item,
           rc.cantidad_requerida, rc.cantidad_cubierta, rc.unidad, rc.estado,
           rc.observaciones, rc.fecha_registro,
           a.nombre AS nombre_actividad,
           i.nombre AS nombre_item, i.categoria AS categoria_item
    FROM recurso_campana rc
    LEFT JOIN actividades a ON a.id_actividad = rc.id_actividad
    LEFT JOIN inventario_item i ON i.id_item = rc.id_item
    ORDER BY rc.fecha_registro DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_recursos_campana_por_actividad` (IN `p_id_actividad` INT)   BEGIN
    SELECT rc.id_recurso_campana, rc.id_actividad, rc.id_item,
           rc.cantidad_requerida, rc.cantidad_cubierta, rc.unidad, rc.estado,
           rc.observaciones, rc.fecha_registro,
           a.nombre AS nombre_actividad,
           i.nombre AS nombre_item, i.categoria AS categoria_item
    FROM recurso_campana rc
    LEFT JOIN actividades a ON a.id_actividad = rc.id_actividad
    LEFT JOIN inventario_item i ON i.id_item = rc.id_item
    WHERE rc.id_actividad = p_id_actividad
    ORDER BY rc.fecha_registro DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_salidas_donaciones` ()   BEGIN
    SELECT
        s.id_salida,
        s.id_donacion,
        s.id_actividad,
        s.tipo_salida,
        s.cantidad,
        s.descripcion,
        s.id_item,
        s.cantidad_item,
        s.id_usuario_registro,
        s.registrado_en,
        s.estado,
        
        d.cantidad AS donacion_cantidad,
        td.nombre AS tipo_donacion_nombre,
        
        a.nombre AS actividad_nombre,
        
        CONCAT(u.nombres, ' ', u.apellidos) AS usuario_registro,
        
        ii.nombre AS item_nombre,
        ii.unidad_medida AS item_unidad_medida,
        
        COALESCE(dn.nombre, 'AN├ôNIMO') AS donante_nombre,
        
        s.motivo_anulacion,
        s.anulado_en,
        
        d.descripcion AS donacion_descripcion
    FROM salida_donacion s
    INNER JOIN donacion d ON d.id_donacion = s.id_donacion
    INNER JOIN tipo_donacion td ON td.id_tipo_donacion = d.id_tipo_donacion
    INNER JOIN actividades a ON a.id_actividad = s.id_actividad
    INNER JOIN usuario u ON u.id_usuario = s.id_usuario_registro
    LEFT JOIN inventario_item ii ON ii.id_item = s.id_item
    LEFT JOIN donacion_donante dd ON dd.id_donacion = d.id_donacion
    LEFT JOIN donante dn ON dn.id_donante = dd.id_donante
    WHERE s.estado != 'ANULADO'
    ORDER BY s.registrado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_salidas_inventario` ()   BEGIN
    SELECT
        si.id_salida_inv,
        si.id_actividad,
        COALESCE(a.nombre, 'Sin actividad') AS actividad_nombre,
        si.motivo,
        COALESCE(si.observacion, '') AS observacion,
        si.id_usuario_registro,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuario_registro,
        DATE_FORMAT(si.registrado_en, '%d/%m/%Y %H:%i') AS registrado_en,
        si.estado,
        CASE WHEN si.anulado_en IS NOT NULL
             THEN DATE_FORMAT(si.anulado_en, '%d/%m/%Y %H:%i')
             ELSE NULL END AS anulado_en,
        si.motivo_anulacion,
        COALESCE(det.total_items, 0) AS total_items,
        COALESCE(det.total_cantidad, 0) AS total_cantidad
    FROM salida_inventario si
    LEFT JOIN actividades a ON si.id_actividad = a.id_actividad
    LEFT JOIN usuario u ON si.id_usuario_registro = u.id_usuario
    LEFT JOIN (
        SELECT id_salida_inv,
               COUNT(*) AS total_items,
               SUM(cantidad) AS total_cantidad
        FROM salida_inventario_detalle
        GROUP BY id_salida_inv
    ) det ON si.id_salida_inv = det.id_salida_inv
    ORDER BY si.registrado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_marcar_notificacion_leida` (IN `p_id_notificacion` INT)   BEGIN
    UPDATE notificaciones SET leida = 1 WHERE id_notificacion = p_id_notificacion;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_marcar_todas_leidas` (IN `p_id_usuario` INT)   BEGIN
    UPDATE notificaciones SET leida = 1 WHERE id_usuario = p_id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtenerBalance` ()   BEGIN
    SELECT
        IFNULL(SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE 0 END), 0) AS total_ingresos,
        IFNULL(SUM(CASE WHEN tipo = 'GASTO'   THEN monto ELSE 0 END), 0) AS total_gastos,
        IFNULL(SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE -monto END), 0) AS saldo
    FROM movimiento_financiero;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtenerDonacionPorId` (IN `p_id_donacion` INT)   BEGIN
    SELECT 
        d.id_donacion,
        d.cantidad,
        d.descripcion,
        td.nombre AS tipoDonacion,
        a.nombre AS actividad,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuarioRegistro,
        d.registrado_en,
        d.id_tipo_donacion,
        d.id_actividad
    FROM donacion d
    LEFT JOIN tipo_donacion td ON d.id_tipo_donacion = td.id_tipo_donacion
    LEFT JOIN actividades a ON d.id_actividad = a.id_actividad
    LEFT JOIN usuario u ON d.id_usuario_registro = u.id_usuario
    WHERE d.id_donacion = p_id_donacion;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtenerMovimiento` (IN `p_id` INT)   BEGIN
    SELECT m.id_movimiento, m.tipo, m.monto, m.descripcion,
           m.categoria, m.comprobante, m.fecha_movimiento,
           m.id_actividad, m.id_usuario, m.creado_en
    FROM movimiento_financiero m
    WHERE m.id_movimiento = p_id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_actividad_por_id` (IN `p_id` INT)   BEGIN
    SELECT a.id_actividad, a.nombre, a.descripcion, a.fecha_inicio, a.fecha_fin,
           a.ubicacion, a.cupo_maximo,
           (SELECT COUNT(*) FROM participacion p WHERE p.id_actividad = a.id_actividad) AS inscritos,
           a.estado, a.id_usuario, a.creado_en
    FROM actividades a
    WHERE a.id_actividad = p_id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_asistencia_por_id` (IN `p_id_asistencia` INT)   BEGIN
    SELECT
        a.id_asistencia,
        a.id_voluntario,
        CONCAT(v.nombres, ' ', v.apellidos) AS nombre_voluntario,
        v.dni AS dni_voluntario,
        a.id_actividad,
        act.nombre AS nombre_actividad,
        a.fecha,
        a.hora_entrada,
        a.hora_salida,
        a.horas_totales,
        a.estado,
        a.observaciones,
        a.id_usuario_registro,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuario_registro,
        a.creado_en
    FROM asistencias a
    INNER JOIN voluntario v   ON a.id_voluntario = v.id_voluntario
    INNER JOIN actividades act ON a.id_actividad  = act.id_actividad
    LEFT JOIN usuario u       ON a.id_usuario_registro = u.id_usuario
    WHERE a.id_asistencia = p_id_asistencia;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_beneficiario_por_id` (IN `p_id_beneficiario` INT)   BEGIN
    SELECT * FROM beneficiario WHERE id_beneficiario = p_id_beneficiario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_beneficiario_por_id_nuevo` (IN `p_id_beneficiario` INT)   BEGIN
    SELECT * FROM beneficiario WHERE id_beneficiario = p_id_beneficiario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_certificado_por_codigo` (IN `p_codigo_certificado` VARCHAR(50))   BEGIN
    SELECT 
        c.id_certificado,
        c.codigo_certificado,
        c.id_voluntario,
        c.id_actividad,
        c.horas_voluntariado,
        c.fecha_emision,
        c.estado,
        c.observaciones,
        c.id_usuario_emite,
        CONCAT(v.nombres, ' ', v.apellidos) AS nombre_voluntario,
        v.dni AS dni_voluntario,
        a.nombre AS nombre_actividad,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuario_emite
    FROM certificados c
    INNER JOIN voluntario v ON c.id_voluntario = v.id_voluntario
    INNER JOIN actividades a ON c.id_actividad = a.id_actividad
    INNER JOIN usuario u ON c.id_usuario_emite = u.id_usuario
    WHERE c.codigo_certificado = p_codigo_certificado;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_certificado_por_id` (IN `p_id_certificado` INT)   BEGIN
    SELECT 
        c.id_certificado,
        c.codigo_certificado,
        c.id_voluntario,
        c.id_actividad,
        c.horas_voluntariado,
        c.fecha_emision,
        c.estado,
        c.observaciones,
        c.id_usuario_emite,
        CONCAT(v.nombres, ' ', v.apellidos) AS nombre_voluntario,
        v.dni AS dni_voluntario,
        a.nombre AS nombre_actividad,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuario_emite
    FROM certificados c
    INNER JOIN voluntario v ON c.id_voluntario = v.id_voluntario
    INNER JOIN actividades a ON c.id_actividad = a.id_actividad
    INNER JOIN usuario u ON c.id_usuario_emite = u.id_usuario
    WHERE c.id_certificado = p_id_certificado;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_donacion_detalle` (IN `p_id` INT)   BEGIN
    SELECT
        d.id_donacion,
        d.cantidad,
        d.descripcion,
        d.id_tipo_donacion,
        d.id_actividad,
        d.id_usuario_registro,
        d.registrado_en,
        td.nombre AS tipoDonacion,
        a.nombre AS actividad,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuarioRegistro,
        COALESCE(dnt.nombre, 'ANONIMO') AS donanteNombre,
        d.estado,
        dnt.tipo AS tipoDonante,
        ddet.id_item,
        ddet.cantidad AS cantidad_item,
        ii.nombre AS item_nombre,
        ii.unidad_medida AS item_unidad_medida,
        dnt.dni AS dniDonante,
        dnt.ruc AS rucDonante,
        dnt.correo AS correoDonante,
        dnt.telefono AS telefonoDonante,
        d.subtipo_donacion AS subtipoDonacion
    FROM donacion d
    LEFT JOIN tipo_donacion td ON d.id_tipo_donacion = td.id_tipo_donacion
    LEFT JOIN actividades a ON d.id_actividad = a.id_actividad
    LEFT JOIN usuario u ON d.id_usuario_registro = u.id_usuario
    LEFT JOIN donacion_donante ddon ON d.id_donacion = ddon.id_donacion
    LEFT JOIN donante dnt ON ddon.id_donante = dnt.id_donante
    LEFT JOIN donacion_detalle ddet ON d.id_donacion = ddet.id_donacion
    LEFT JOIN inventario_item ii ON ddet.id_item = ii.id_item
    WHERE d.id_donacion = p_id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_intentos_restantes` (IN `p_username` VARCHAR(60), IN `p_max_intentos` INT)   BEGIN
    SELECT (p_max_intentos - COALESCE(intentos_fallidos, 0)) AS intentos_restantes
    FROM usuario
    WHERE username = p_username;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_item_inventario` (IN `p_id_item` INT)   BEGIN
    SELECT id_item, nombre, categoria, unidad_medida, stock_actual, stock_minimo,
           estado, observacion, creado_en, actualizado_en
    FROM inventario_item
    WHERE id_item = p_id_item;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_nombre_rol_usuario` (IN `p_id_usuario` INT)   BEGIN
    SELECT rs.nombre_rol
    FROM usuario_rol ur
    INNER JOIN rol_sistema rs ON ur.id_rol_sistema = rs.id_rol_sistema
    WHERE ur.id_usuario = p_id_usuario
    LIMIT 1;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_permisos_usuario` (IN `p_id_usuario` INT)   BEGIN
    SELECT p.id_permiso, p.nombre_permiso, p.descripcion
    FROM usuario_permiso up
    JOIN permiso p ON up.id_permiso = p.id_permiso
    WHERE up.id_usuario = p_id_usuario
    ORDER BY p.id_permiso;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_recurso_campana` (IN `p_id` INT)   BEGIN
    SELECT rc.id_recurso_campana, rc.id_actividad, rc.id_item,
           rc.cantidad_requerida, rc.cantidad_cubierta, rc.unidad, rc.estado,
           rc.observaciones, rc.fecha_registro,
           a.nombre AS nombre_actividad,
           i.nombre AS nombre_item, i.categoria AS categoria_item
    FROM recurso_campana rc
    LEFT JOIN actividades a ON a.id_actividad = rc.id_actividad
    LEFT JOIN inventario_item i ON i.id_item = rc.id_item
    WHERE rc.id_recurso_campana = p_id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_roles_por_usuario` ()   BEGIN
    SELECT ur.id_usuario, rs.nombre_rol
    FROM usuario_rol ur
    INNER JOIN rol_sistema rs ON ur.id_rol_sistema = rs.id_rol_sistema
    ORDER BY ur.id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_saldo_donacion` (IN `p_id_donacion` INT)   BEGIN
    SELECT 
        d.id_donacion,
        d.cantidad AS monto_original,
        IFNULL(dn.nombre, 'ANONIMO') AS donante,
        (d.cantidad 
            - COALESCE((SELECT SUM(sd2.cantidad) FROM salida_donacion sd2 WHERE sd2.id_donacion = d.id_donacion AND sd2.estado = 'CONFIRMADO' AND sd2.tipo_salida = 'DINERO'), 0)
            - COALESCE((SELECT SUM(gd2.monto) FROM gasto_donacion gd2 WHERE gd2.id_donacion = d.id_donacion), 0)
        ) AS saldo_disponible
    FROM donacion d
    LEFT JOIN donacion_donante dd ON dd.id_donacion = d.id_donacion
    LEFT JOIN donante dn ON dn.id_donante = dd.id_donante
    WHERE d.id_donacion = p_id_donacion;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_salida_donacion` (IN `p_id` INT)   BEGIN
    SELECT
        s.id_salida,
        s.id_donacion,
        s.id_actividad,
        s.tipo_salida,
        s.cantidad,
        s.descripcion,
        s.id_item,
        s.cantidad_item,
        s.id_usuario_registro,
        s.registrado_en,
        s.estado,
        d.cantidad AS donacion_cantidad,
        td.nombre AS tipo_donacion_nombre,
        a.nombre AS actividad_nombre,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuario_registro,
        ii.nombre AS item_nombre,
        ii.unidad_medida AS item_unidad_medida,
        COALESCE(dn.nombre, 'AN├ôNIMO') AS donante_nombre,
        s.motivo_anulacion,
        s.anulado_en,
        d.descripcion AS donacion_descripcion,
        s.comprobante
    FROM salida_donacion s
    INNER JOIN donacion d ON d.id_donacion = s.id_donacion
    INNER JOIN tipo_donacion td ON td.id_tipo_donacion = d.id_tipo_donacion
    INNER JOIN actividades a ON a.id_actividad = s.id_actividad
    INNER JOIN usuario u ON u.id_usuario = s.id_usuario_registro
    LEFT JOIN inventario_item ii ON ii.id_item = s.id_item
    LEFT JOIN donacion_donante dd ON dd.id_donacion = d.id_donacion
    LEFT JOIN donante dn ON dn.id_donante = dd.id_donante
    WHERE s.id_salida = p_id
    LIMIT 1;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_salida_inventario` (IN `p_id` INT)   BEGIN
    SELECT
        si.id_salida_inv,
        si.id_actividad,
        COALESCE(a.nombre, 'Sin actividad') AS actividad_nombre,
        si.motivo,
        COALESCE(si.observacion, '') AS observacion,
        si.id_usuario_registro,
        CONCAT(u.nombres, ' ', u.apellidos) AS usuario_registro,
        DATE_FORMAT(si.registrado_en, '%d/%m/%Y %H:%i') AS registrado_en,
        si.estado,
        CASE WHEN si.anulado_en IS NOT NULL
             THEN DATE_FORMAT(si.anulado_en, '%d/%m/%Y %H:%i')
             ELSE NULL END AS anulado_en,
        si.motivo_anulacion
    FROM salida_inventario si
    LEFT JOIN actividades a ON si.id_actividad = a.id_actividad
    LEFT JOIN usuario u ON si.id_usuario_registro = u.id_usuario
    WHERE si.id_salida_inv = p_id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_salida_inventario_detalle` (IN `p_id` INT)   BEGIN
    SELECT
        d.id_detalle,
        d.id_salida_inv,
        d.id_item,
        i.nombre AS item_nombre,
        i.categoria AS item_categoria,
        i.unidad_medida AS item_unidad,
        d.cantidad,
        d.stock_antes,
        d.stock_despues
    FROM salida_inventario_detalle d
    JOIN inventario_item i ON d.id_item = i.id_item
    WHERE d.id_salida_inv = p_id
    ORDER BY i.nombre;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_todas_actividades` ()   BEGIN
    SELECT a.id_actividad, a.nombre, a.descripcion, a.fecha_inicio, a.fecha_fin,
           a.ubicacion, a.cupo_maximo,
           (SELECT COUNT(*) FROM participacion p WHERE p.id_actividad = a.id_actividad) AS inscritos,
           a.estado, a.id_usuario, a.creado_en
    FROM actividades a
    ORDER BY a.creado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_todos_beneficiarios` ()   BEGIN
    SELECT * FROM beneficiario ORDER BY creado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_todos_beneficiarios_nuevo` ()   BEGIN
    SELECT * FROM beneficiario ORDER BY creado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_todos_permisos` ()   BEGIN
    SELECT id_permiso, nombre_permiso, descripcion
    FROM permiso
    ORDER BY id_permiso;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_todos_usuarios` ()   BEGIN
    SELECT id_usuario, nombres, apellidos, correo, username, dni, estado, creado_en, actualizado_en 
    FROM usuario 
    ORDER BY id_usuario DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_todos_voluntarios` ()   BEGIN
    SELECT id_voluntario, nombres, apellidos, dni, correo, telefono, carrera, estado, id_usuario
    FROM voluntario
    ORDER BY id_voluntario DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_usuario_por_id` (IN `p_id_usuario` INT)   BEGIN
    SELECT id_usuario, nombres, apellidos, correo, username, dni, estado, creado_en, actualizado_en 
    FROM usuario 
    WHERE id_usuario = p_id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_usuario_por_username` (IN `p_username` VARCHAR(60))   BEGIN
    SELECT id_usuario, nombres, apellidos, correo, username, dni,
           password_hash, foto_perfil, estado, creado_en, actualizado_en
    FROM usuario
    WHERE username = p_username;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_voluntario_por_id` (IN `p_id_voluntario` INT)   BEGIN
    SELECT id_voluntario, nombres, apellidos, dni, correo, telefono, carrera, estado, id_usuario
    FROM voluntario
    WHERE id_voluntario = p_id_voluntario
    LIMIT 1;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_proxima_actividad` ()   BEGIN
    SELECT 
        id_actividad,
        nombre,
        fecha_inicio,
        ubicacion
    FROM actividades
    WHERE fecha_inicio >= CURDATE()
      AND estado = 'ACTIVO'
    ORDER BY fecha_inicio ASC
    LIMIT 1;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrarMovimiento` (IN `p_tipo` VARCHAR(10), IN `p_monto` DECIMAL(12,2), IN `p_descripcion` VARCHAR(255), IN `p_categoria` VARCHAR(60), IN `p_comprobante` VARCHAR(100), IN `p_fecha` DATE, IN `p_id_actividad` INT, IN `p_id_usuario` INT)   BEGIN
    INSERT INTO movimiento_financiero
        (tipo, monto, descripcion, categoria, comprobante,
         fecha_movimiento, id_actividad, id_usuario)
    VALUES
        (p_tipo, p_monto, p_descripcion, p_categoria, p_comprobante,
         p_fecha, NULLIF(p_id_actividad, 0), p_id_usuario);

    SELECT LAST_INSERT_ID() AS id_movimiento;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_asistencia` (IN `p_id_voluntario` INT, IN `p_id_actividad` INT, IN `p_fecha` DATE, IN `p_hora_entrada` TIME, IN `p_hora_salida` TIME, IN `p_estado` VARCHAR(20), IN `p_observaciones` TEXT, IN `p_id_usuario_registro` INT)   BEGIN
    DECLARE v_horas DECIMAL(5,2) DEFAULT 0.00;

    
    IF p_hora_entrada IS NOT NULL AND p_hora_salida IS NOT NULL THEN
        SET v_horas = ROUND(TIMESTAMPDIFF(MINUTE, p_hora_entrada, p_hora_salida) / 60.0, 2);
        IF v_horas < 0 THEN
            SET v_horas = 0.00;
        END IF;
    END IF;

    INSERT INTO asistencias (
        id_voluntario, id_actividad, fecha,
        hora_entrada, hora_salida, horas_totales,
        estado, observaciones, id_usuario_registro
    ) VALUES (
        p_id_voluntario, p_id_actividad, p_fecha,
        p_hora_entrada, p_hora_salida, v_horas,
        p_estado, p_observaciones, p_id_usuario_registro
    );

    SELECT LAST_INSERT_ID() AS id_asistencia;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_donacion_inventario` (IN `p_cantidad` DECIMAL(10,2), IN `p_descripcion` VARCHAR(150), IN `p_id_tipo_donacion` INT, IN `p_subtipo_donacion` VARCHAR(50), IN `p_id_actividad` INT, IN `p_id_usuario_registro` INT, IN `p_id_item` INT, IN `p_crear_nuevo_item` TINYINT, IN `p_item_nombre` VARCHAR(150), IN `p_item_categoria` VARCHAR(50), IN `p_item_unidad_medida` VARCHAR(30), IN `p_item_stock_minimo` DECIMAL(10,2), IN `p_donacion_anonima` TINYINT, IN `p_donante_tipo` VARCHAR(20), IN `p_donante_nombre` VARCHAR(150), IN `p_donante_correo` VARCHAR(100), IN `p_donante_telefono` VARCHAR(30), IN `p_donante_dni` VARCHAR(20), IN `p_donante_ruc` VARCHAR(20))   BEGIN
    DECLARE v_id_donacion INT;
    DECLARE v_id_donante INT DEFAULT NULL;
    DECLARE v_tipo_donante VARCHAR(20) DEFAULT NULL;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF p_cantidad IS NULL OR p_cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad/monto de donacion debe ser mayor a cero.';
    END IF;

    INSERT INTO donacion(cantidad, descripcion, id_tipo_donacion, subtipo_donacion, id_actividad, id_usuario_registro, registrado_en, estado)
    VALUES(p_cantidad, p_descripcion, p_id_tipo_donacion, NULLIF(TRIM(p_subtipo_donacion),''), p_id_actividad, p_id_usuario_registro, NOW(), 'PENDIENTE');
    SET v_id_donacion = LAST_INSERT_ID();

    IF IFNULL(p_donacion_anonima, 0) = 0 THEN
        IF p_donante_nombre IS NULL OR TRIM(p_donante_nombre) = '' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Debe indicar el nombre del donante o marcar donacion anonima.';
        END IF;

        SET v_tipo_donante = CASE UPPER(TRIM(IFNULL(p_donante_tipo, 'PERSONA')))
            WHEN 'EMPRESA' THEN 'Empresa'
            WHEN 'GRUPO' THEN 'Grupo'
            ELSE 'Persona'
        END;

        SELECT dnt.id_donante INTO v_id_donante
        FROM donante dnt
        WHERE LOWER(TRIM(dnt.nombre)) = LOWER(TRIM(p_donante_nombre))
          AND dnt.tipo = v_tipo_donante
          AND (IFNULL(TRIM(dnt.correo), '') = IFNULL(TRIM(p_donante_correo), '') OR IFNULL(TRIM(dnt.telefono), '') = IFNULL(TRIM(p_donante_telefono), ''))
        LIMIT 1;

        IF v_id_donante IS NULL THEN
            INSERT INTO donante(tipo, nombre, correo, telefono, dni, ruc)
            VALUES(v_tipo_donante, TRIM(p_donante_nombre), NULLIF(TRIM(p_donante_correo), ''), NULLIF(TRIM(p_donante_telefono), ''), NULLIF(TRIM(p_donante_dni), ''), NULLIF(TRIM(p_donante_ruc), ''));
            SET v_id_donante = LAST_INSERT_ID();
        END IF;

        INSERT INTO donacion_donante(id_donacion, id_donante) VALUES(v_id_donacion, v_id_donante);
    END IF;

    COMMIT;
    SELECT v_id_donacion AS id_donacion;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_intento_fallido` (IN `p_username` VARCHAR(60), IN `p_max_intentos` INT, IN `p_tiempo_bloqueo_minutos` INT)   BEGIN
    
    UPDATE usuario
    SET intentos_fallidos = intentos_fallidos + 1
    WHERE username = p_username;

    
    UPDATE usuario
    SET bloqueado_hasta = DATE_ADD(NOW(), INTERVAL p_tiempo_bloqueo_minutos MINUTE)
    WHERE username = p_username
      AND intentos_fallidos >= p_max_intentos;

    
    SELECT intentos_fallidos
    FROM usuario
    WHERE username = p_username;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_movimiento_inventario` (IN `p_id_item` INT, IN `p_tipo_movimiento` VARCHAR(20), IN `p_motivo` VARCHAR(30), IN `p_cantidad` DECIMAL(10,2), IN `p_observacion` VARCHAR(255), IN `p_id_usuario` INT)   BEGIN
    DECLARE v_stock_anterior DECIMAL(10,2) DEFAULT 0;
    DECLARE v_stock_nuevo DECIMAL(10,2) DEFAULT 0;
    DECLARE v_tipo VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SET v_tipo = UPPER(TRIM(p_tipo_movimiento));

    IF p_id_item IS NULL OR p_id_item <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Debe seleccionar un item de inventario valido.';
    END IF;

    IF p_cantidad IS NULL OR p_cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad del movimiento debe ser mayor a cero.';
    END IF;

    IF v_tipo NOT IN ('ENTRADA', 'SALIDA') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tipo de movimiento invalido. Use ENTRADA o SALIDA.';
    END IF;

    START TRANSACTION;

    SELECT stock_actual INTO v_stock_anterior
    FROM inventario_item
    WHERE id_item = p_id_item
    FOR UPDATE;

    IF v_tipo = 'ENTRADA' THEN
        SET v_stock_nuevo = v_stock_anterior + p_cantidad;
    ELSE
        IF v_stock_anterior < p_cantidad THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insuficiente para registrar la salida.';
        END IF;
        SET v_stock_nuevo = v_stock_anterior - p_cantidad;
    END IF;

    UPDATE inventario_item
    SET stock_actual = v_stock_nuevo,
        actualizado_en = NOW()
    WHERE id_item = p_id_item;

    INSERT INTO inventario_movimiento(
        id_item, tipo_movimiento, motivo, cantidad, stock_anterior, stock_nuevo,
        id_referencia, tabla_referencia, observacion, id_usuario, creado_en
    ) VALUES(
        p_id_item, v_tipo, UPPER(TRIM(IFNULL(p_motivo, 'MANUAL'))), p_cantidad, v_stock_anterior, v_stock_nuevo,
        NULL, NULL, p_observacion, p_id_usuario, NOW()
    );

    COMMIT;
    SELECT v_stock_nuevo AS stock_actual;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_recurso_campana` (IN `p_id_actividad` INT, IN `p_id_item` INT, IN `p_cantidad_requerida` DOUBLE, IN `p_cantidad_cubierta` DOUBLE, IN `p_unidad` VARCHAR(50), IN `p_estado` VARCHAR(20), IN `p_observaciones` TEXT)   BEGIN
    INSERT INTO recurso_campana (id_actividad, id_item, cantidad_requerida, cantidad_cubierta, unidad, estado, observaciones)
    VALUES (p_id_actividad, p_id_item, p_cantidad_requerida, p_cantidad_cubierta, p_unidad, p_estado, p_observaciones);
    SELECT LAST_INSERT_ID() AS id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_salida_donacion` (IN `p_id_donacion` INT, IN `p_id_actividad` INT, IN `p_tipo_salida` VARCHAR(20), IN `p_cantidad` DOUBLE, IN `p_descripcion` TEXT, IN `p_id_item` INT, IN `p_cantidad_item` DOUBLE, IN `p_id_usuario` INT, IN `p_comprobante` VARCHAR(100))   BEGIN
    INSERT INTO salida_donacion (
        id_donacion, id_actividad, tipo_salida, cantidad,
        descripcion, id_item, cantidad_item, id_usuario_registro, comprobante, estado
    ) VALUES (
        p_id_donacion, p_id_actividad, p_tipo_salida, p_cantidad,
        p_descripcion,
        IF(p_id_item = 0, NULL, p_id_item),
        IF(p_cantidad_item = 0, NULL, p_cantidad_item),
        p_id_usuario, NULLIF(p_comprobante, ''), 'PENDIENTE'
    );
    SELECT LAST_INSERT_ID() AS id_salida;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_salida_inventario` (IN `p_id_actividad` INT, IN `p_motivo` VARCHAR(255), IN `p_observacion` VARCHAR(500), IN `p_id_usuario` INT)   BEGIN
    DECLARE v_id_act INT;
    SET v_id_act = IF(p_id_actividad = 0, NULL, p_id_actividad);

    INSERT INTO salida_inventario (id_actividad, motivo, observacion, id_usuario_registro, estado)
    VALUES (v_id_act, p_motivo, p_observacion, p_id_usuario, 'CONFIRMADO');

    SELECT LAST_INSERT_ID() AS id_salida_inv;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_salida_inventario_detalle` (IN `p_id_salida_inv` INT, IN `p_id_item` INT, IN `p_cantidad` DECIMAL(10,2))   BEGIN
    DECLARE v_stock_actual DECIMAL(10,2);

    
    SELECT stock_actual INTO v_stock_actual
    FROM inventario_item WHERE id_item = p_id_item;

    
    INSERT INTO salida_inventario_detalle (id_salida_inv, id_item, cantidad, stock_antes, stock_despues)
    VALUES (p_id_salida_inv, p_id_item, p_cantidad, v_stock_actual, v_stock_actual - p_cantidad);

    
    UPDATE inventario_item
    SET stock_actual = stock_actual - p_cantidad
    WHERE id_item = p_id_item;

    SELECT 1 AS resultado;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_resetear_intentos_fallidos` (IN `p_username` VARCHAR(60))   BEGIN
    UPDATE usuario
    SET intentos_fallidos = 0,
        bloqueado_hasta = NULL
    WHERE username = p_username;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_resumenMensual` ()   BEGIN
    SELECT
        DATE_FORMAT(fecha_movimiento, '%Y-%m') AS mes,
        SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE 0 END) AS ingresos,
        SUM(CASE WHEN tipo = 'GASTO'   THEN monto ELSE 0 END) AS gastos
    FROM movimiento_financiero
    GROUP BY DATE_FORMAT(fecha_movimiento, '%Y-%m')
    ORDER BY mes DESC
    LIMIT 12;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_resumenPorCategoria` ()   BEGIN
    SELECT categoria, tipo,
           SUM(monto) AS total,
           COUNT(*)   AS cantidad
    FROM movimiento_financiero
    GROUP BY categoria, tipo
    ORDER BY total DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_sincronizar_donaciones_tesoreria` ()   BEGIN
    DECLARE v_count INT DEFAULT 0;

    INSERT INTO movimiento_financiero (tipo, monto, descripcion, categoria, comprobante, fecha_movimiento, id_actividad, id_usuario)
    SELECT
        'INGRESO',
        d.cantidad,
        CONCAT('Donaci??n', IF(d.descripcion IS NOT NULL AND d.descripcion != '', CONCAT(': ', d.descripcion), ''), ' (Donacion #', d.id_donacion, ')'),
        'Donaciones',
        CONCAT('BOLETA-', d.id_donacion),
        CURDATE(),
        d.id_actividad,
        d.id_usuario_registro
    FROM donacion d
    WHERE d.id_tipo_donacion = 1
      AND d.estado = 'CONFIRMADO'
      AND NOT EXISTS (
          SELECT 1 FROM movimiento_financiero mf
          WHERE mf.descripcion LIKE CONCAT('%Donacion #', d.id_donacion, '%')
      );

    SET v_count = ROW_COUNT();
    SELECT v_count AS sincronizadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_tiene_permiso` (IN `p_id_usuario` INT, IN `p_nombre_permiso` VARCHAR(100) COLLATE utf8mb4_spanish_ci, OUT `p_resultado` TINYINT)   BEGIN
    SELECT COUNT(*) INTO p_resultado
    FROM usuario_permiso up
    JOIN permiso p ON up.id_permiso = p.id_permiso
    WHERE up.id_usuario = p_id_usuario
      AND p.nombre_permiso COLLATE utf8mb4_spanish_ci = p_nombre_permiso;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_total_horas_voluntarias` ()   BEGIN
    SELECT IFNULL(SUM(horas_totales), 0) AS total_horas
    FROM asistencias
    WHERE estado IN ('ASISTIO', 'TARDANZA');
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_verificar_bloqueo` (IN `p_username` VARCHAR(60))   BEGIN
    SELECT intentos_fallidos, bloqueado_hasta
    FROM usuario
    WHERE username = p_username;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `actividades`
--

CREATE TABLE `actividades` (
  `id_actividad` int(11) NOT NULL,
  `nombre` varchar(200) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date DEFAULT NULL,
  `ubicacion` varchar(300) NOT NULL,
  `cupo_maximo` int(11) NOT NULL DEFAULT 30,
  `inscritos` int(11) NOT NULL DEFAULT 0,
  `estado` enum('ACTIVO','FINALIZADO','CANCELADO') NOT NULL DEFAULT 'ACTIVO',
  `id_usuario` int(11) DEFAULT NULL COMMENT 'Quién creó la actividad',
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `actividades`
--

INSERT INTO `actividades` (`id_actividad`, `nombre`, `descripcion`, `fecha_inicio`, `fecha_fin`, `ubicacion`, `cupo_maximo`, `inscritos`, `estado`, `id_usuario`, `creado_en`) VALUES
(20, 'Campaña de Limpieza del Río La Leche', 'Actividad ecológica donde voluntarios realizarán la recolección de desechos sólidos y clasificación de residuos en zonas críticas del río.', '2026-03-01', '2026-03-04', 'Ribera del Río La Leche, Chiclayo', 30, 1, 'ACTIVO', 21, '2026-03-01 18:25:09'),
(21, 'Campaña de Limpieza Comunitaria', 'Jornada de limpieza y recolección de residuos sólidos con apoyo de voluntarios de la comunidad.', '2026-03-01', '2026-03-03', 'Parque Central del distrito', 20, 1, 'ACTIVO', 21, '2026-03-02 02:45:48'),
(22, 'Campaña Médica Gratuita', 'Atención médica básica gratuita para la población vulnerable con apoyo de personal de salud voluntario.', '2026-03-01', '2026-03-04', 'Centro comunal del distrito', 40, 0, 'ACTIVO', 21, '2026-03-02 03:10:41'),
(23, 'Taller de Capacitación para Voluntarios', 'Capacitación dirigida a nuevos voluntarios sobre protocolos de apoyo comunitario y manejo de recursos.', '2026-03-02', '2026-03-04', 'Auditorio municipal', 60, 0, 'ACTIVO', 21, '2026-03-02 03:11:41');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `actividad_beneficiario`
--

CREATE TABLE `actividad_beneficiario` (
  `id_actividad_beneficiario` int(11) NOT NULL,
  `id_actividad` int(11) DEFAULT NULL,
  `id_beneficiario` int(11) DEFAULT NULL,
  `observacion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `actividad_beneficiario`
--

INSERT INTO `actividad_beneficiario` (`id_actividad_beneficiario`, `id_actividad`, `id_beneficiario`, `observacion`) VALUES
(2, 20, 11, ''),
(3, 21, 9, '');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `actividad_lugar`
--

CREATE TABLE `actividad_lugar` (
  `id_actividad_lugar` int(11) NOT NULL,
  `id_actividad` int(11) DEFAULT NULL,
  `id_lugar` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `actividad_lugar`
--

INSERT INTO `actividad_lugar` (`id_actividad_lugar`, `id_actividad`, `id_lugar`) VALUES
(1, 21, 4),
(2, 22, 5),
(3, 23, 6);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `actividad_recurso`
--

CREATE TABLE `actividad_recurso` (
  `id_actividad_recurso` int(11) NOT NULL,
  `id_actividad` int(11) DEFAULT NULL,
  `id_recurso` int(11) DEFAULT NULL,
  `cantidad_requerida` decimal(10,2) DEFAULT NULL,
  `cantidad_conseguida` decimal(10,2) DEFAULT NULL,
  `prioridad` varchar(20) DEFAULT NULL,
  `observacion` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `actividad_recurso`
--

INSERT INTO `actividad_recurso` (`id_actividad_recurso`, `id_actividad`, `id_recurso`, `cantidad_requerida`, `cantidad_conseguida`, `prioridad`, `observacion`) VALUES
(11, 20, 20, 20.00, 10.00, 'ALTA', ''),
(12, 21, 20, 50.00, 0.00, 'ALTA', ''),
(13, 21, 24, 30.00, 0.00, 'ALTA', '');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `asistencias`
--

CREATE TABLE `asistencias` (
  `id_asistencia` int(11) NOT NULL,
  `id_voluntario` int(11) NOT NULL,
  `id_actividad` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `hora_entrada` time DEFAULT NULL,
  `hora_salida` time DEFAULT NULL,
  `horas_totales` decimal(5,2) DEFAULT 0.00,
  `estado` enum('ASISTIO','FALTA','TARDANZA') NOT NULL DEFAULT 'FALTA',
  `observaciones` text DEFAULT NULL,
  `id_usuario_registro` int(11) DEFAULT NULL COMMENT 'Usuario que registró la asistencia',
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `actualizado_en` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `asistencias`
--

INSERT INTO `asistencias` (`id_asistencia`, `id_voluntario`, `id_actividad`, `fecha`, `hora_entrada`, `hora_salida`, `horas_totales`, `estado`, `observaciones`, `id_usuario_registro`, `creado_en`, `actualizado_en`) VALUES
(11, 57, 20, '2026-03-01', '07:00:00', '11:00:00', 4.00, 'ASISTIO', NULL, 21, '2026-03-01 22:51:19', '2026-03-01 22:51:19');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `beneficiario`
--

CREATE TABLE `beneficiario` (
  `id_beneficiario` int(11) NOT NULL,
  `estado` varchar(30) DEFAULT 'activo',
  `organizacion` varchar(150) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `distrito` varchar(100) DEFAULT NULL,
  `necesidad_principal` varchar(100) DEFAULT NULL,
  `observaciones` text DEFAULT NULL,
  `nombre_responsable` varchar(100) DEFAULT NULL,
  `apellidos_responsable` varchar(100) DEFAULT NULL,
  `dni` varchar(20) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `id_usuario` int(11) DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `beneficiario`
--

INSERT INTO `beneficiario` (`id_beneficiario`, `estado`, `organizacion`, `direccion`, `distrito`, `necesidad_principal`, `observaciones`, `nombre_responsable`, `apellidos_responsable`, `dni`, `telefono`, `id_usuario`, `creado_en`) VALUES
(9, 'ACTIVO', 'Asociación Vecinal “Nueva Esperanza”', 'Jr. Los Jazmines 245', 'José Leonardo Ortiz', 'ALIMENTACIÓN', 'Vive con 2 hijos menores. Requiere apoyo mensual de alimentos básicos.', 'VIVIANA', 'FERNANDEZ ALVARADO', '74251836', '965852147', 21, '2026-03-01 19:08:22'),
(10, 'ACTIVO', 'Asociación Solidaria Nuevo Amanecer', 'Calle Los Olivos 245', 'Chiclayo', 'Alimentos no perecibles y kits de higiene', 'La organización atiende a familias en situación de vulnerabilidad. Requieren apoyo mensual.', 'FRANK DIEGO', 'MONTENEGRO SANCHEZ', '71458521', '987123852', 21, '2026-03-01 19:57:06'),
(11, 'ACTIVO', 'Campaña de Limpieza del Río La Leche', 'Ribera del Río La Leche, Chiclayo', 'Chiclayo', 'Materiales de limpieza y apoyo logístico', 'Grupo ambiental comunitario dedicado a la conservación del río', 'JOSH NICKSON', 'BARDALES SUAREZ', '71458543', '987456320', 21, '2026-03-01 20:21:29');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categoria_inventario`
--

CREATE TABLE `categoria_inventario` (
  `id_categoria` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `color` varchar(20) DEFAULT '#6366f1',
  `icono` varchar(50) DEFAULT 'fa-box',
  `creado_en` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `categoria_inventario`
--

INSERT INTO `categoria_inventario` (`id_categoria`, `nombre`, `descripcion`, `color`, `icono`, `creado_en`) VALUES
(1, 'Alimentos', 'Productos alimenticios', '#f59e0b', 'fa-utensils', '2026-02-20 14:38:32'),
(2, 'Ropa', 'Prendas de vestir', '#8b5cf6', 'fa-shirt', '2026-02-20 14:38:32'),
(3, 'Utiles Escolares', 'Materiales educativos', '#3b82f6', 'fa-pencil', '2026-02-20 14:38:32'),
(4, 'Medicinas', 'Productos farmaceuticos', '#ef4444', 'fa-pills', '2026-02-20 14:38:32'),
(5, 'Higiene', 'Productos de aseo', '#10b981', 'fa-pump-soap', '2026-02-20 14:38:32'),
(6, 'Otros', 'Articulos varios', '#6b7280', 'fa-box', '2026-02-20 14:38:32');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `certificados`
--

CREATE TABLE `certificados` (
  `id_certificado` int(11) NOT NULL,
  `codigo_certificado` varchar(50) NOT NULL,
  `id_voluntario` int(11) NOT NULL,
  `id_actividad` int(11) NOT NULL,
  `horas_voluntariado` int(11) NOT NULL,
  `fecha_emision` date NOT NULL,
  `estado` enum('EMITIDO','ANULADO') DEFAULT 'EMITIDO',
  `observaciones` text DEFAULT NULL,
  `id_usuario_emite` int(11) NOT NULL,
  `fecha_anulacion` date DEFAULT NULL,
  `motivo_anulacion` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `certificados`
--

INSERT INTO `certificados` (`id_certificado`, `codigo_certificado`, `id_voluntario`, `id_actividad`, `horas_voluntariado`, `fecha_emision`, `estado`, `observaciones`, `id_usuario_emite`, `fecha_anulacion`, `motivo_anulacion`, `created_at`, `updated_at`) VALUES
(7, 'CERT-2026-0001', 57, 20, 4, '2026-03-01', 'EMITIDO', '', 21, NULL, NULL, '2026-03-01 22:51:28', '2026-03-01 22:51:28');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `donacion`
--

CREATE TABLE `donacion` (
  `id_donacion` int(11) NOT NULL,
  `cantidad` decimal(10,2) DEFAULT NULL,
  `descripcion` varchar(150) DEFAULT NULL,
  `id_tipo_donacion` int(11) DEFAULT NULL,
  `subtipo_donacion` varchar(50) DEFAULT NULL,
  `id_actividad` int(11) DEFAULT NULL,
  `id_usuario_registro` int(11) DEFAULT NULL,
  `registrado_en` datetime DEFAULT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'ACTIVO',
  `anulado_en` datetime DEFAULT NULL,
  `id_usuario_anula` int(11) DEFAULT NULL,
  `motivo_anulacion` varchar(255) DEFAULT NULL,
  `actualizado_en` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `donacion`
--

INSERT INTO `donacion` (`id_donacion`, `cantidad`, `descripcion`, `id_tipo_donacion`, `subtipo_donacion`, `id_actividad`, `id_usuario_registro`, `registrado_en`, `estado`, `anulado_en`, `id_usuario_anula`, `motivo_anulacion`, `actualizado_en`) VALUES
(44, 500.00, 'Donación en efectivo para apoyar la Campaña de Limpieza del Río La Leche', 1, 'Yape/Plin', 20, 21, '2026-03-01 17:57:49', 'CONFIRMADO', NULL, NULL, NULL, '2026-03-01 17:57:55'),
(49, 1200.00, 'Donación para apoyo general de campañas sociales', 1, 'Efectivo', 20, 21, '2026-03-01 21:43:00', 'CONFIRMADO', NULL, NULL, NULL, '2026-03-01 21:43:02'),
(50, 900.00, ': Donación para compra de materiales de apoyo', 1, 'Yape/Plin', 21, 21, '2026-03-01 21:47:54', 'CONFIRMADO', NULL, NULL, NULL, '2026-03-01 21:47:57');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `donacion_detalle`
--

CREATE TABLE `donacion_detalle` (
  `id_donacion_detalle` int(11) NOT NULL,
  `id_donacion` int(11) NOT NULL,
  `id_item` int(11) NOT NULL,
  `cantidad` decimal(10,2) NOT NULL,
  `observacion` varchar(255) DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `donacion_donante`
--

CREATE TABLE `donacion_donante` (
  `id_donacion_donante` int(11) NOT NULL,
  `id_donacion` int(11) DEFAULT NULL,
  `id_donante` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `donacion_donante`
--

INSERT INTO `donacion_donante` (`id_donacion_donante`, `id_donacion`, `id_donante`) VALUES
(12, 44, 10),
(13, 50, 11);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `donante`
--

CREATE TABLE `donante` (
  `id_donante` int(11) NOT NULL,
  `tipo` enum('Persona','Empresa','Grupo') DEFAULT NULL,
  `nombre` varchar(150) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `telefono` varchar(30) DEFAULT NULL,
  `ruc` varchar(20) DEFAULT NULL,
  `dni` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `donante`
--

INSERT INTO `donante` (`id_donante`, `tipo`, `nombre`, `correo`, `telefono`, `ruc`, `dni`) VALUES
(1, 'Persona', 'juan', 'juan@gmail.com', '965741214', NULL, NULL),
(2, 'Persona', 'jose', 'jose@gmail.com', '963852147', NULL, NULL),
(3, 'Persona', 'lucia', 'lucia@gmail.com', '965745852', NULL, NULL),
(4, 'Empresa', 'SOY VOLUNTARIO LAMBAYEQUE', NULL, '0196548522', '20605005994', NULL),
(5, 'Persona', 'MARICIELO BECERRA GUEVARA', 'maricie@gmail.com', '965741521', NULL, NULL),
(6, 'Persona', 'HAYDE MARTINEZ CASTILLO', 'hay@gmail.com', '987456321', NULL, NULL),
(7, 'Persona', 'VANNIA LIZBETH TANTALEAN CHINCHAY', 'vania@gmail.com', '987456325', NULL, NULL),
(8, 'Empresa', 'ASOCIACION VIDA Y VOLUNTARIADO - VIVOL', 'asociacion@gmail.com', '987456378', '20602952801', NULL),
(10, 'Persona', 'EDWIN JESUS FLORES VASQUEZ', 'edwin@gmail.com', '987478123', NULL, '71853011'),
(11, 'Persona', 'SAMUEL ARMANDO OCAMPO LOPEZ', 'samuel@gmail.com', '987852741', NULL, '71854145');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `eventos_calendario`
--

CREATE TABLE `eventos_calendario` (
  `id_evento` int(11) NOT NULL,
  `titulo` varchar(200) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date DEFAULT NULL,
  `color` varchar(20) DEFAULT '#6366f1',
  `id_usuario` int(11) DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `eventos_calendario`
--

INSERT INTO `eventos_calendario` (`id_evento`, `titulo`, `descripcion`, `fecha_inicio`, `fecha_fin`, `color`, `id_usuario`, `creado_en`) VALUES
(3, 'pago de luz', '', '2026-03-01', '2026-03-01', '#eab308', 21, '2026-03-02 03:02:28');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `gasto_donacion`
--

CREATE TABLE `gasto_donacion` (
  `id_gasto_donacion` int(11) NOT NULL,
  `id_movimiento` int(11) NOT NULL,
  `id_donacion` int(11) NOT NULL,
  `monto` decimal(12,2) NOT NULL,
  `creado_en` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `inventario_item`
--

CREATE TABLE `inventario_item` (
  `id_item` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `categoria` varchar(50) NOT NULL,
  `unidad_medida` varchar(30) NOT NULL,
  `stock_actual` decimal(10,2) NOT NULL DEFAULT 0.00,
  `stock_minimo` decimal(10,2) NOT NULL DEFAULT 0.00,
  `estado` varchar(20) NOT NULL DEFAULT 'ACTIVO',
  `observacion` varchar(255) DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT current_timestamp(),
  `actualizado_en` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `inventario_item`
--

INSERT INTO `inventario_item` (`id_item`, `nombre`, `categoria`, `unidad_medida`, `stock_actual`, `stock_minimo`, `estado`, `observacion`, `creado_en`, `actualizado_en`) VALUES
(1, 'Arroz Costeño', 'ALIMENTOS', 'kg', 11.00, 90.00, 'ACTIVO', '', '2026-02-14 23:19:51', '2026-02-24 09:28:57'),
(2, 'Agua mineral 500ml', 'ALIMENTOS', 'Unidad', 120.00, 50.00, 'ACTIVO', 'Botellas de agua para actividades de campo', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(3, 'Galletas integrales', 'ALIMENTOS', 'Paquete', 80.00, 30.00, 'ACTIVO', 'Paquetes individuales para refrigerios', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(4, 'Leche evaporada', 'ALIMENTOS', 'Lata', 45.00, 20.00, 'ACTIVO', 'Latas de 400g para donaciones alimentarias', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(5, 'Frazadas polares', 'ROPA', 'Unidad', 35.00, 15.00, 'ACTIVO', 'Frazadas de polar para campanas de frio', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(6, 'Polos blancos talla M', 'ROPA', 'Unidad', 60.00, 20.00, 'ACTIVO', 'Polos para voluntarios en eventos', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(7, 'Botiquin de primeros auxilios', 'MEDICAMENTOS', 'Unidad', 10.00, 5.00, 'ACTIVO', 'Botiquines completos para brigadas de salud', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(8, 'Alcohol en gel 500ml', 'MEDICAMENTOS', 'Frasco', 50.00, 25.00, 'ACTIVO', 'Frascos de alcohol gel para higiene', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(9, 'Mascarillas descartables', 'MEDICAMENTOS', 'Caja', 30.00, 10.00, 'ACTIVO', 'Cajas de 50 unidades cada una', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(10, 'Carpas 3x3m', 'LOGISTICA', 'Unidad', 8.00, 3.00, 'ACTIVO', 'Carpas plegables para stands en eventos', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(11, 'Sillas plasticas', 'LOGISTICA', 'Unidad', 40.00, 15.00, 'ACTIVO', 'Sillas apilables para actividades comunitarias', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(12, 'Megafono portatil', 'LOGISTICA', 'Unidad', 5.00, 2.00, 'ACTIVO', 'Megafonos con bateria recargable', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(13, 'Cuadernos A4', 'UTILES', 'Unidad', 200.00, 50.00, 'ACTIVO', 'Cuadernos rayados para donaciones escolares', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(14, 'Lapices 2B', 'UTILES', 'Caja', 25.00, 10.00, 'ACTIVO', 'Cajas de 12 lapices para campanas educativas', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(15, 'Colchonetas plegables', 'LOGISTICA', 'Unidad', 15.00, 5.00, 'ACTIVO', 'Colchonetas para albergues temporales', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(16, 'Bolsas de viveres 5kg', 'ALIMENTOS', 'Unidad', 55.00, 20.00, 'ACTIVO', 'Bolsas armadas con arroz, azucar, aceite y fideos', '2026-03-01 21:50:56', '2026-03-01 21:50:56'),
(17, 'Botellas de agua (500ml)', 'BEBIDAS', 'unidades', 240.00, 80.00, 'ACTIVO', 'Para hidratacion en campanas', '2026-03-01 08:00:00', '2026-03-01 08:00:00'),
(18, 'Kits de primeros auxilios', 'SALUD', 'unidades', 35.00, 15.00, 'ACTIVO', 'Contiene vendas, alcohol y gasas', '2026-03-01 08:00:00', '2026-03-01 08:00:00'),
(19, 'Chalecos reflectivos', 'SEGURIDAD', 'unidades', 120.00, 40.00, 'ACTIVO', 'Para uso en trabajo de campo', '2026-03-01 08:00:00', '2026-03-01 08:00:00'),
(20, 'Guantes de trabajo', 'PROTECCION', 'pares', 300.00, 100.00, 'ACTIVO', 'Talla unica, latex resistente', '2026-03-01 08:00:00', '2026-03-01 08:00:00'),
(21, 'Linternas recargables', 'EQUIPAMIENTO', 'unidades', 45.00, 20.00, 'ACTIVO', 'Autonomia de 8 horas', '2026-03-01 08:00:00', '2026-03-01 08:00:00'),
(22, 'Extensiones electricas', 'INFRAESTRUCTURA', 'unidades', 28.00, 10.00, 'ACTIVO', 'Extension 5m con proteccion', '2026-03-01 08:00:00', '2026-03-01 08:00:00'),
(23, 'Mantas termicas', 'EMERGENCIA', 'unidades', 95.00, 30.00, 'ACTIVO', 'Para zonas de bajas temperaturas', '2026-03-01 08:00:00', '2026-03-01 08:00:00'),
(24, 'Cuadernos para registro', 'OFICINA', 'unidades', 180.00, 60.00, 'ACTIVO', 'Cuaderno A4, 100 hojas cuadriculado', '2026-03-01 08:00:00', '2026-03-01 08:00:00'),
(25, 'Radios portatiles', 'COMUNICACION', 'unidades', 22.00, 8.00, 'ACTIVO', 'Frecuencia VHF, bateria incluida', '2026-03-01 08:00:00', '2026-03-01 08:00:00'),
(26, 'Toldos impermeables 4x6m', 'LOGISTICA', 'unidades', 12.00, 5.00, 'ACTIVO', 'Para cobertura en zonas de lluvia', '2026-03-01 08:00:00', '2026-03-01 08:00:00');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `inventario_movimiento`
--

CREATE TABLE `inventario_movimiento` (
  `id_movimiento` int(11) NOT NULL,
  `id_item` int(11) NOT NULL,
  `tipo_movimiento` varchar(20) NOT NULL,
  `motivo` varchar(30) NOT NULL,
  `cantidad` decimal(10,2) NOT NULL,
  `stock_anterior` decimal(10,2) NOT NULL,
  `stock_nuevo` decimal(10,2) NOT NULL,
  `id_referencia` int(11) DEFAULT NULL,
  `tabla_referencia` varchar(40) DEFAULT NULL,
  `observacion` varchar(255) DEFAULT NULL,
  `id_usuario` int(11) DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `lugar`
--

CREATE TABLE `lugar` (
  `id_lugar` int(11) NOT NULL,
  `departamento` varchar(100) DEFAULT NULL,
  `provincia` varchar(100) DEFAULT NULL,
  `distrito` varchar(100) DEFAULT NULL,
  `direccion_referencia` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `lugar`
--

INSERT INTO `lugar` (`id_lugar`, `departamento`, `provincia`, `distrito`, `direccion_referencia`) VALUES
(1, 'Lambayeque', 'Chiclayo', 'Pimentel', 'Playa de Pimentel - Zona de dunas costeras norte'),
(2, 'Lambayeque', 'Chiclayo', 'Pimentel', 'Parque ecologico municipal de Pimentel'),
(3, 'Lambayeque', 'Chiclayo', 'Pimentel', 'Ribera del rio Lambayeque - Sector Las Rocas'),
(4, '', '', 'Parque Central del distrito', ''),
(5, '', '', 'Centro comunal del distrito', ''),
(6, '', '', 'Auditorio municipal', '');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `movimiento_financiero`
--

CREATE TABLE `movimiento_financiero` (
  `id_movimiento` int(11) NOT NULL,
  `tipo` enum('INGRESO','GASTO') NOT NULL,
  `monto` decimal(12,2) NOT NULL,
  `descripcion` varchar(255) NOT NULL,
  `categoria` varchar(60) NOT NULL,
  `comprobante` varchar(100) DEFAULT NULL,
  `fecha_movimiento` date NOT NULL,
  `id_actividad` int(11) DEFAULT NULL,
  `id_usuario` int(11) NOT NULL,
  `creado_en` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `movimiento_financiero`
--

INSERT INTO `movimiento_financiero` (`id_movimiento`, `tipo`, `monto`, `descripcion`, `categoria`, `comprobante`, `fecha_movimiento`, `id_actividad`, `id_usuario`, `creado_en`) VALUES
(57, 'INGRESO', 500.00, 'Donaci¾n: Donación en efectivo para apoyar la Campaña de Limpieza del Río La Leche (Donacion #44)', 'Donaciones', 'BOLETA-44', '2026-03-01', 20, 21, '2026-03-01 18:09:57'),
(59, 'INGRESO', 200.00, 'Donación: Donación económica destinada a la compra de materiales de limpieza y bolsas biodegradables para la campaña. (Donacion #45)', 'Donaciones', 'BOLETA-45', '2026-03-01', 20, 21, '2026-03-01 18:34:22'),
(60, 'INGRESO', 500.00, 'Donación: donacion  (Donacion #46)', 'Donaciones', 'BOLETA-46', '2026-03-01', 20, 21, '2026-03-01 19:01:12'),
(61, 'GASTO', 1150.00, 'Compra de impresora modelo EcoTank L3250 para uso administrativo y apoyo en campañas.', 'Equipos / Activos', 'F001-000458', '2026-03-02', 20, 21, '2026-03-01 19:02:43'),
(62, 'GASTO', 500.00, 'Salida de Donaci¾n #3 (Donaci¾n #44): campa±a de limpieza', 'Salidas de Donaciones', NULL, '2026-03-01', 20, 21, '2026-03-01 19:59:32'),
(63, 'INGRESO', 1200.00, 'Donación: donaciones (Donacion #48)', 'Donaciones', 'BOLETA-48', '2026-03-01', 20, 21, '2026-03-01 20:01:45'),
(64, 'INGRESO', 1500.00, 'Donación: donaciones  (Donacion #47)', 'Donaciones', 'BOLETA-47', '2026-03-01', 20, 21, '2026-03-01 21:20:12'),
(65, 'INGRESO', 1200.00, 'Donación: Donación para apoyo general de campañas sociales (Donacion #49)', 'Donaciones', 'BOLETA-49', '2026-03-01', 20, 21, '2026-03-01 21:43:03'),
(66, 'INGRESO', 900.00, 'Donación: : Donación para compra de materiales de apoyo (Donacion #50)', 'Donaciones', 'BOLETA-50', '2026-03-01', 21, 21, '2026-03-01 21:47:57'),
(67, 'GASTO', 600.00, 'Salida de Donación #4 (Donación #50): campaña de limpieza', 'Salidas de Donaciones', NULL, '2026-03-01', 21, 21, '2026-03-01 21:48:51'),
(68, 'GASTO', 300.00, 'Salida de Donación #5 (Donación #49): campaña de limpieza', 'Salidas de Donaciones', NULL, '2026-03-01', 21, 21, '2026-03-01 21:48:55');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `notificaciones`
--

CREATE TABLE `notificaciones` (
  `id_notificacion` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `tipo` varchar(30) NOT NULL COMMENT 'ACTIVIDAD_HOY, BIENVENIDA, EVENTO_CALENDARIO',
  `titulo` varchar(200) NOT NULL,
  `mensaje` text DEFAULT NULL,
  `icono` varchar(50) DEFAULT 'fa-bell',
  `color` varchar(20) DEFAULT '#6366f1',
  `leida` tinyint(1) DEFAULT 0,
  `referencia_id` int(11) DEFAULT NULL COMMENT 'ID de actividad, evento, etc.',
  `fecha_creacion` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `notificaciones`
--

INSERT INTO `notificaciones` (`id_notificacion`, `id_usuario`, `tipo`, `titulo`, `mensaje`, `icono`, `color`, `leida`, `referencia_id`, `fecha_creacion`) VALUES
(11, 29, 'ACTIVIDAD_HOY', '📋 Actividad hoy: Campaña de Limpieza del Río La Leche', 'La actividad \"Campaña de Limpieza del Río La Leche\" está programada para hoy en Ribera del Río La Leche, Chiclayo.', 'fa-calendar-check', '#10b981', 0, 20, '2026-03-01 14:58:20');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `participacion`
--

CREATE TABLE `participacion` (
  `id_participacion` int(11) NOT NULL,
  `id_voluntario` int(11) DEFAULT NULL,
  `id_actividad` int(11) DEFAULT NULL,
  `id_rol_actividad` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `participacion`
--

INSERT INTO `participacion` (`id_participacion`, `id_voluntario`, `id_actividad`, `id_rol_actividad`) VALUES
(17, 57, 20, NULL),
(18, 56, 21, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `permiso`
--

CREATE TABLE `permiso` (
  `id_permiso` int(11) NOT NULL,
  `nombre_permiso` varchar(80) DEFAULT NULL,
  `descripcion` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `permiso`
--

INSERT INTO `permiso` (`id_permiso`, `nombre_permiso`, `descripcion`) VALUES
(1, 'usuarios.ver', 'Gestionar usuarios del sistema'),
(2, 'voluntarios.ver', 'Gestionar voluntarios'),
(3, 'beneficiarios.ver', 'Gestionar beneficiarios'),
(4, 'actividades.ver', 'Gestionar actividades'),
(5, 'asistencias.ver', 'Gestionar asistencias'),
(6, 'certificados.ver', 'Gestionar certificados'),
(7, 'calendario.ver', 'Ver calendario de eventos'),
(8, 'donaciones.ver', 'Gestionar donaciones'),
(9, 'inventario.ver', 'Gestionar inventario'),
(10, 'tesoreria.ver', 'Ver tesoreria y movimientos'),
(11, 'reportes.ver', 'Ver reportes del sistema'),
(12, 'salidas_donaciones.ver', 'Gestionar salidas de donaciones'),
(13, 'salidas_inventario.ver', 'Gestionar salidas de inventario'),
(14, 'recursos_campana.ver', 'Gestionar stock de recursos por campa?a');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `recurso`
--

CREATE TABLE `recurso` (
  `id_recurso` int(11) NOT NULL,
  `nombre` varchar(120) DEFAULT NULL,
  `unidad_medida` varchar(30) DEFAULT NULL,
  `tipo_recurso` varchar(30) DEFAULT NULL,
  `descripcion` varchar(150) DEFAULT NULL,
  `cantidad_total` double NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `recurso`
--

INSERT INTO `recurso` (`id_recurso`, `nombre`, `unidad_medida`, `tipo_recurso`, `descripcion`, `cantidad_total`) VALUES
(20, 'Bolsas de basura resistentes', 'Unidad', 'Material', 'Bolsas grandes para recolección de residuos en campañas de limpieza', 200),
(21, 'Botellas de agua (500ml)', 'unidad', 'MATERIAL', 'Para hidratacion de voluntarios en campo', 240),
(22, 'Kits de primeros auxilios', 'unidad', 'EQUIPO', 'Contiene vendas, alcohol, gasas y tijeras', 35),
(23, 'Chalecos reflectivos', 'unidad', 'EQUIPO', 'Para identificacion y seguridad en campo', 120),
(24, 'Guantes de proteccion', 'par', 'EQUIPO', 'Guantes resistentes para trabajo de campo', 300),
(25, 'Linternas recargables', 'unidad', 'EQUIPO', 'Autonomia de 8 horas, incluye cable USB', 45),
(26, 'Extensiones electricas 5m', 'unidad', 'EQUIPO', 'Extension con proteccion contra sobrecarga', 28),
(27, 'Mantas termicas', 'unidad', 'MATERIAL', 'Para zonas de emergencia y bajas temperaturas', 95),
(28, 'Cuadernos de registro A4', 'unidad', 'MATERIAL', 'Cuaderno 100 hojas cuadriculado para informes', 180),
(29, 'Radios portatiles VHF', 'unidad', 'EQUIPO', 'Comunicacion en campo, bateria incluida', 22),
(30, 'Toldos impermeables 4x6m', 'unidad', 'EQUIPO', 'Cobertura en zonas de lluvia y emergencia', 12);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `recurso_campana`
--

CREATE TABLE `recurso_campana` (
  `id_recurso_campana` int(11) NOT NULL,
  `id_actividad` int(11) NOT NULL,
  `id_item` int(11) NOT NULL,
  `cantidad_requerida` double NOT NULL DEFAULT 0,
  `cantidad_cubierta` double NOT NULL DEFAULT 0,
  `unidad` varchar(50) DEFAULT 'Unidad',
  `estado` varchar(20) DEFAULT 'PENDIENTE',
  `observaciones` text DEFAULT NULL,
  `fecha_registro` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `rol_actividad`
--

CREATE TABLE `rol_actividad` (
  `id_rol_actividad` int(11) NOT NULL,
  `nombre_rol` varchar(50) DEFAULT NULL,
  `descripcion` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `rol_actividad`
--

INSERT INTO `rol_actividad` (`id_rol_actividad`, `nombre_rol`, `descripcion`) VALUES
(1, 'Coordinador de Actividad', 'Dirige la actividad'),
(2, 'Voluntario Operativo', 'Participa en la actividad'),
(3, 'Responsable Logística', 'Coordina recursos'),
(4, 'Responsable Reporte', 'Documenta la actividad'),
(5, 'Coordinador de Actividad', 'Dirige la actividad'),
(6, 'Voluntario Operativo', 'Participa en la actividad'),
(7, 'Responsable Logística', 'Coordina recursos'),
(8, 'Responsable Reporte', 'Documenta la actividad'),
(9, 'Voluntario', 'Participante en actividades de voluntariado'),
(10, 'Líder de Equipo', 'Lidera y coordina equipos de voluntarios'),
(11, 'Encargado de Logística', 'Gestiona recursos y logística de actividades'),
(12, 'Coordinador de Proyecto', 'Coordina y supervisa proyectos completos'),
(13, 'Administrador del Sistema', 'Acceso completo al sistema de voluntariado');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `rol_sistema`
--

CREATE TABLE `rol_sistema` (
  `id_rol_sistema` int(11) NOT NULL,
  `nombre_rol` varchar(50) DEFAULT NULL,
  `descripcion` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `rol_sistema`
--

INSERT INTO `rol_sistema` (`id_rol_sistema`, `nombre_rol`, `descripcion`) VALUES
(1, 'Lider de Equipo', 'Lider de equipo con acceso al sistema'),
(2, 'Encargado de Logistica', 'Encargado de logistica con acceso al sistema'),
(3, 'Coordinador de Proyecto', 'Coordinador de proyecto con acceso al sistema'),
(4, 'Administrador del Sistema', 'Administrador con acceso completo al sistema');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `salida_donacion`
--

CREATE TABLE `salida_donacion` (
  `id_salida` int(11) NOT NULL,
  `id_donacion` int(11) NOT NULL,
  `id_actividad` int(11) NOT NULL,
  `tipo_salida` varchar(20) NOT NULL DEFAULT 'DINERO' COMMENT 'DINERO | ESPECIE',
  `cantidad` double NOT NULL,
  `descripcion` text DEFAULT NULL,
  `id_item` int(11) DEFAULT NULL COMMENT 'Solo para salidas en especie',
  `cantidad_item` double DEFAULT NULL COMMENT 'Cantidad de items distribuidos',
  `id_usuario_registro` int(11) NOT NULL,
  `registrado_en` datetime DEFAULT current_timestamp(),
  `estado` varchar(20) DEFAULT 'PENDIENTE' COMMENT 'PENDIENTE | CONFIRMADO | ANULADO',
  `anulado_en` datetime DEFAULT NULL,
  `id_usuario_anula` int(11) DEFAULT NULL,
  `motivo_anulacion` varchar(250) DEFAULT NULL,
  `actualizado_en` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `salida_donacion`
--

INSERT INTO `salida_donacion` (`id_salida`, `id_donacion`, `id_actividad`, `tipo_salida`, `cantidad`, `descripcion`, `id_item`, `cantidad_item`, `id_usuario_registro`, `registrado_en`, `estado`, `anulado_en`, `id_usuario_anula`, `motivo_anulacion`, `actualizado_en`) VALUES
(3, 44, 20, 'DINERO', 500, 'campaña de limpieza', NULL, NULL, 21, '2026-03-01 19:45:17', 'CONFIRMADO', NULL, NULL, NULL, '2026-03-01 19:50:02'),
(4, 50, 21, 'DINERO', 600, 'campaña de limpieza', NULL, NULL, 21, '2026-03-01 21:48:43', 'CONFIRMADO', NULL, NULL, NULL, '2026-03-01 21:48:51'),
(5, 49, 21, 'DINERO', 300, 'campaña de limpieza', NULL, NULL, 21, '2026-03-01 21:48:43', 'CONFIRMADO', NULL, NULL, NULL, '2026-03-01 21:48:55');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `salida_inventario`
--

CREATE TABLE `salida_inventario` (
  `id_salida_inv` int(11) NOT NULL,
  `id_actividad` int(11) DEFAULT NULL,
  `motivo` varchar(255) NOT NULL,
  `observacion` varchar(500) DEFAULT NULL,
  `id_usuario_registro` int(11) NOT NULL,
  `registrado_en` datetime NOT NULL DEFAULT current_timestamp(),
  `estado` varchar(20) NOT NULL DEFAULT 'CONFIRMADO',
  `anulado_en` datetime DEFAULT NULL,
  `motivo_anulacion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `salida_inventario_detalle`
--

CREATE TABLE `salida_inventario_detalle` (
  `id_detalle` int(11) NOT NULL,
  `id_salida_inv` int(11) NOT NULL,
  `id_item` int(11) NOT NULL,
  `cantidad` decimal(10,2) NOT NULL,
  `stock_antes` decimal(10,2) NOT NULL DEFAULT 0.00,
  `stock_despues` decimal(10,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipo_donacion`
--

CREATE TABLE `tipo_donacion` (
  `id_tipo_donacion` int(11) NOT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `descripcion` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `tipo_donacion`
--

INSERT INTO `tipo_donacion` (`id_tipo_donacion`, `nombre`, `descripcion`) VALUES
(1, 'DINERO', 'Donación monetaria'),
(2, 'OBJETO', 'Donación de objetos o materiales');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario`
--

CREATE TABLE `usuario` (
  `id_usuario` int(11) NOT NULL,
  `nombres` varchar(100) DEFAULT NULL,
  `apellidos` varchar(100) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `username` varchar(60) DEFAULT NULL,
  `dni` varchar(20) DEFAULT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `foto_perfil` varchar(255) DEFAULT NULL,
  `estado` varchar(20) DEFAULT NULL,
  `creado_en` datetime DEFAULT NULL,
  `actualizado_en` datetime DEFAULT NULL,
  `intentos_fallidos` int(11) NOT NULL DEFAULT 0,
  `bloqueado_hasta` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuario`
--

INSERT INTO `usuario` (`id_usuario`, `nombres`, `apellidos`, `correo`, `username`, `dni`, `password_hash`, `foto_perfil`, `estado`, `creado_en`, `actualizado_en`, `intentos_fallidos`, `bloqueado_hasta`) VALUES
(21, 'luis', 'goerdy', 'tchi@gamil.com', 'geordy', NULL, '$2a$10$gF/BXO.egDt/oEeSSZMzMu7IE5VWf9BmvIateoBiud8OiUNSPqFie', 'img/perfil_21.webp', 'ACTIVO', '2026-02-04 01:41:12', '2026-02-12 00:26:32', 0, NULL),
(29, 'FRANS ELISEO', 'PEREA POQUIS', 'frans@gmail.com', 'frans', '74852547', '$2a$10$Os7DG9qDHf/jDDHihGUOz.ujXgD09lJtC6GOLjqZldGIBF3kQTWAy', NULL, 'ACTIVO', '2026-03-01 14:58:07', NULL, 0, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario_permiso`
--

CREATE TABLE `usuario_permiso` (
  `id_usuario_permiso` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `id_permiso` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuario_permiso`
--

INSERT INTO `usuario_permiso` (`id_usuario_permiso`, `id_usuario`, `id_permiso`) VALUES
(70, 21, 1),
(71, 21, 2),
(72, 21, 3),
(73, 21, 4),
(74, 21, 5),
(75, 21, 6),
(76, 21, 7),
(77, 21, 8),
(78, 21, 9),
(79, 21, 10),
(80, 21, 11),
(88, 21, 12),
(89, 21, 13),
(90, 21, 14),
(81, 29, 3),
(82, 29, 4),
(83, 29, 7),
(84, 29, 8);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario_rol`
--

CREATE TABLE `usuario_rol` (
  `id_usuario_rol` int(11) NOT NULL,
  `id_usuario` int(11) DEFAULT NULL,
  `id_rol_sistema` int(11) DEFAULT NULL,
  `asignado_en` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `voluntario`
--

CREATE TABLE `voluntario` (
  `id_voluntario` int(11) NOT NULL,
  `nombres` varchar(100) DEFAULT NULL,
  `apellidos` varchar(100) DEFAULT NULL,
  `dni` varchar(20) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `carrera` varchar(100) DEFAULT NULL,
  `cargo` varchar(50) DEFAULT 'Voluntario',
  `acceso_sistema` tinyint(1) NOT NULL DEFAULT 0,
  `estado` varchar(20) DEFAULT NULL,
  `id_usuario` int(11) DEFAULT NULL,
  `id_rol_actividad` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `voluntario`
--

INSERT INTO `voluntario` (`id_voluntario`, `nombres`, `apellidos`, `dni`, `correo`, `telefono`, `carrera`, `cargo`, `acceso_sistema`, `estado`, `id_usuario`, `id_rol_actividad`) VALUES
(52, 'DELINA LINDA', 'VISITACION MORALES', '71853622', 'delina@gmail.com', '965741456', 'contadora', 'Voluntario', 0, 'ACTIVO', NULL, NULL),
(56, 'FRANS ELISEO', 'PEREA POQUIS', '74852547', 'frans@gmail.com', '987417852', 'Ingeneria civil', 'Coordinador', 1, 'ACTIVO', 29, NULL),
(57, 'JHONEIL ALEXANDER', 'FLORES PEREZ', '71852255', 'johoneil@gmail.com', '987417123', 'tecnico en computacion', 'Voluntario', 0, 'ACTIVO', NULL, NULL);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `actividades`
--
ALTER TABLE `actividades`
  ADD PRIMARY KEY (`id_actividad`),
  ADD KEY `fk_actividad_usuario` (`id_usuario`);

--
-- Indices de la tabla `actividad_beneficiario`
--
ALTER TABLE `actividad_beneficiario`
  ADD PRIMARY KEY (`id_actividad_beneficiario`),
  ADD KEY `idx_ab_act` (`id_actividad`),
  ADD KEY `idx_ab_ben` (`id_beneficiario`);

--
-- Indices de la tabla `actividad_lugar`
--
ALTER TABLE `actividad_lugar`
  ADD PRIMARY KEY (`id_actividad_lugar`),
  ADD KEY `id_actividad` (`id_actividad`),
  ADD KEY `id_lugar` (`id_lugar`);

--
-- Indices de la tabla `actividad_recurso`
--
ALTER TABLE `actividad_recurso`
  ADD PRIMARY KEY (`id_actividad_recurso`),
  ADD KEY `id_actividad` (`id_actividad`),
  ADD KEY `id_recurso` (`id_recurso`);

--
-- Indices de la tabla `asistencias`
--
ALTER TABLE `asistencias`
  ADD PRIMARY KEY (`id_asistencia`),
  ADD UNIQUE KEY `uk_asistencia_voluntario_actividad_fecha` (`id_voluntario`,`id_actividad`,`fecha`),
  ADD KEY `idx_asistencia_voluntario` (`id_voluntario`),
  ADD KEY `idx_asistencia_actividad` (`id_actividad`),
  ADD KEY `idx_asistencia_fecha` (`fecha`),
  ADD KEY `idx_asistencia_estado` (`estado`),
  ADD KEY `fk_asistencia_usuario` (`id_usuario_registro`);

--
-- Indices de la tabla `beneficiario`
--
ALTER TABLE `beneficiario`
  ADD PRIMARY KEY (`id_beneficiario`);

--
-- Indices de la tabla `categoria_inventario`
--
ALTER TABLE `categoria_inventario`
  ADD PRIMARY KEY (`id_categoria`);

--
-- Indices de la tabla `certificados`
--
ALTER TABLE `certificados`
  ADD PRIMARY KEY (`id_certificado`),
  ADD UNIQUE KEY `codigo_certificado` (`codigo_certificado`),
  ADD KEY `idx_codigo` (`codigo_certificado`),
  ADD KEY `idx_voluntario` (`id_voluntario`),
  ADD KEY `idx_actividad` (`id_actividad`),
  ADD KEY `idx_estado` (`estado`),
  ADD KEY `fk_cert_usuario` (`id_usuario_emite`);

--
-- Indices de la tabla `donacion`
--
ALTER TABLE `donacion`
  ADD PRIMARY KEY (`id_donacion`),
  ADD KEY `id_tipo_donacion` (`id_tipo_donacion`),
  ADD KEY `id_actividad` (`id_actividad`),
  ADD KEY `id_usuario_registro` (`id_usuario_registro`),
  ADD KEY `fk_donacion_usuario_anula` (`id_usuario_anula`),
  ADD KEY `idx_donacion_estado` (`estado`);

--
-- Indices de la tabla `donacion_detalle`
--
ALTER TABLE `donacion_detalle`
  ADD PRIMARY KEY (`id_donacion_detalle`),
  ADD KEY `idx_donacion_detalle_donacion` (`id_donacion`),
  ADD KEY `idx_donacion_detalle_item` (`id_item`);

--
-- Indices de la tabla `donacion_donante`
--
ALTER TABLE `donacion_donante`
  ADD PRIMARY KEY (`id_donacion_donante`),
  ADD KEY `id_donacion` (`id_donacion`),
  ADD KEY `id_donante` (`id_donante`);

--
-- Indices de la tabla `donante`
--
ALTER TABLE `donante`
  ADD PRIMARY KEY (`id_donante`);

--
-- Indices de la tabla `eventos_calendario`
--
ALTER TABLE `eventos_calendario`
  ADD PRIMARY KEY (`id_evento`),
  ADD KEY `fk_evento_usuario` (`id_usuario`);

--
-- Indices de la tabla `gasto_donacion`
--
ALTER TABLE `gasto_donacion`
  ADD PRIMARY KEY (`id_gasto_donacion`),
  ADD KEY `id_movimiento` (`id_movimiento`),
  ADD KEY `id_donacion` (`id_donacion`);

--
-- Indices de la tabla `inventario_item`
--
ALTER TABLE `inventario_item`
  ADD PRIMARY KEY (`id_item`),
  ADD UNIQUE KEY `uk_inventario_item_nombre_categoria_unidad` (`nombre`,`categoria`,`unidad_medida`),
  ADD KEY `idx_inventario_estado` (`estado`),
  ADD KEY `idx_inventario_categoria` (`categoria`),
  ADD KEY `idx_inventario_nombre` (`nombre`);

--
-- Indices de la tabla `inventario_movimiento`
--
ALTER TABLE `inventario_movimiento`
  ADD PRIMARY KEY (`id_movimiento`),
  ADD KEY `idx_movimiento_item` (`id_item`),
  ADD KEY `idx_movimiento_creado_en` (`creado_en`),
  ADD KEY `fk_movimiento_usuario` (`id_usuario`);

--
-- Indices de la tabla `lugar`
--
ALTER TABLE `lugar`
  ADD PRIMARY KEY (`id_lugar`);

--
-- Indices de la tabla `movimiento_financiero`
--
ALTER TABLE `movimiento_financiero`
  ADD PRIMARY KEY (`id_movimiento`),
  ADD KEY `fk_mov_actividad` (`id_actividad`),
  ADD KEY `fk_mov_usuario` (`id_usuario`);

--
-- Indices de la tabla `notificaciones`
--
ALTER TABLE `notificaciones`
  ADD PRIMARY KEY (`id_notificacion`),
  ADD KEY `id_usuario` (`id_usuario`);

--
-- Indices de la tabla `participacion`
--
ALTER TABLE `participacion`
  ADD PRIMARY KEY (`id_participacion`),
  ADD KEY `id_voluntario` (`id_voluntario`),
  ADD KEY `id_actividad` (`id_actividad`),
  ADD KEY `id_rol_actividad` (`id_rol_actividad`);

--
-- Indices de la tabla `permiso`
--
ALTER TABLE `permiso`
  ADD PRIMARY KEY (`id_permiso`);

--
-- Indices de la tabla `recurso`
--
ALTER TABLE `recurso`
  ADD PRIMARY KEY (`id_recurso`);

--
-- Indices de la tabla `recurso_campana`
--
ALTER TABLE `recurso_campana`
  ADD PRIMARY KEY (`id_recurso_campana`),
  ADD KEY `fk_rc_actividad` (`id_actividad`),
  ADD KEY `fk_rc_item` (`id_item`);

--
-- Indices de la tabla `rol_actividad`
--
ALTER TABLE `rol_actividad`
  ADD PRIMARY KEY (`id_rol_actividad`);

--
-- Indices de la tabla `rol_sistema`
--
ALTER TABLE `rol_sistema`
  ADD PRIMARY KEY (`id_rol_sistema`);

--
-- Indices de la tabla `salida_donacion`
--
ALTER TABLE `salida_donacion`
  ADD PRIMARY KEY (`id_salida`),
  ADD KEY `id_donacion` (`id_donacion`),
  ADD KEY `id_actividad` (`id_actividad`);

--
-- Indices de la tabla `salida_inventario`
--
ALTER TABLE `salida_inventario`
  ADD PRIMARY KEY (`id_salida_inv`),
  ADD KEY `id_usuario_registro` (`id_usuario_registro`),
  ADD KEY `salida_inventario_ibfk_1` (`id_actividad`);

--
-- Indices de la tabla `salida_inventario_detalle`
--
ALTER TABLE `salida_inventario_detalle`
  ADD PRIMARY KEY (`id_detalle`),
  ADD KEY `id_salida_inv` (`id_salida_inv`),
  ADD KEY `id_item` (`id_item`);

--
-- Indices de la tabla `tipo_donacion`
--
ALTER TABLE `tipo_donacion`
  ADD PRIMARY KEY (`id_tipo_donacion`);

--
-- Indices de la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`id_usuario`);

--
-- Indices de la tabla `usuario_permiso`
--
ALTER TABLE `usuario_permiso`
  ADD PRIMARY KEY (`id_usuario_permiso`),
  ADD UNIQUE KEY `uniq_usuario_permiso` (`id_usuario`,`id_permiso`),
  ADD KEY `up_ibfk_2` (`id_permiso`);

--
-- Indices de la tabla `usuario_rol`
--
ALTER TABLE `usuario_rol`
  ADD PRIMARY KEY (`id_usuario_rol`),
  ADD KEY `id_usuario` (`id_usuario`),
  ADD KEY `id_rol_sistema` (`id_rol_sistema`);

--
-- Indices de la tabla `voluntario`
--
ALTER TABLE `voluntario`
  ADD PRIMARY KEY (`id_voluntario`),
  ADD UNIQUE KEY `uq_voluntario_dni` (`dni`),
  ADD UNIQUE KEY `uq_voluntario_correo` (`correo`),
  ADD UNIQUE KEY `uq_voluntario_telefono` (`telefono`),
  ADD KEY `id_usuario` (`id_usuario`),
  ADD KEY `fk_voluntario_rol` (`id_rol_actividad`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `actividades`
--
ALTER TABLE `actividades`
  MODIFY `id_actividad` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT de la tabla `actividad_beneficiario`
--
ALTER TABLE `actividad_beneficiario`
  MODIFY `id_actividad_beneficiario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `actividad_lugar`
--
ALTER TABLE `actividad_lugar`
  MODIFY `id_actividad_lugar` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `actividad_recurso`
--
ALTER TABLE `actividad_recurso`
  MODIFY `id_actividad_recurso` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT de la tabla `asistencias`
--
ALTER TABLE `asistencias`
  MODIFY `id_asistencia` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT de la tabla `beneficiario`
--
ALTER TABLE `beneficiario`
  MODIFY `id_beneficiario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT de la tabla `categoria_inventario`
--
ALTER TABLE `categoria_inventario`
  MODIFY `id_categoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `certificados`
--
ALTER TABLE `certificados`
  MODIFY `id_certificado` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `donacion`
--
ALTER TABLE `donacion`
  MODIFY `id_donacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=51;

--
-- AUTO_INCREMENT de la tabla `donacion_detalle`
--
ALTER TABLE `donacion_detalle`
  MODIFY `id_donacion_detalle` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `donacion_donante`
--
ALTER TABLE `donacion_donante`
  MODIFY `id_donacion_donante` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT de la tabla `donante`
--
ALTER TABLE `donante`
  MODIFY `id_donante` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT de la tabla `eventos_calendario`
--
ALTER TABLE `eventos_calendario`
  MODIFY `id_evento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `gasto_donacion`
--
ALTER TABLE `gasto_donacion`
  MODIFY `id_gasto_donacion` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `inventario_item`
--
ALTER TABLE `inventario_item`
  MODIFY `id_item` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT de la tabla `inventario_movimiento`
--
ALTER TABLE `inventario_movimiento`
  MODIFY `id_movimiento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `lugar`
--
ALTER TABLE `lugar`
  MODIFY `id_lugar` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `movimiento_financiero`
--
ALTER TABLE `movimiento_financiero`
  MODIFY `id_movimiento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=69;

--
-- AUTO_INCREMENT de la tabla `notificaciones`
--
ALTER TABLE `notificaciones`
  MODIFY `id_notificacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT de la tabla `participacion`
--
ALTER TABLE `participacion`
  MODIFY `id_participacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT de la tabla `permiso`
--
ALTER TABLE `permiso`
  MODIFY `id_permiso` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT de la tabla `recurso`
--
ALTER TABLE `recurso`
  MODIFY `id_recurso` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT de la tabla `recurso_campana`
--
ALTER TABLE `recurso_campana`
  MODIFY `id_recurso_campana` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `rol_actividad`
--
ALTER TABLE `rol_actividad`
  MODIFY `id_rol_actividad` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT de la tabla `rol_sistema`
--
ALTER TABLE `rol_sistema`
  MODIFY `id_rol_sistema` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `salida_donacion`
--
ALTER TABLE `salida_donacion`
  MODIFY `id_salida` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `salida_inventario`
--
ALTER TABLE `salida_inventario`
  MODIFY `id_salida_inv` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `salida_inventario_detalle`
--
ALTER TABLE `salida_inventario_detalle`
  MODIFY `id_detalle` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `tipo_donacion`
--
ALTER TABLE `tipo_donacion`
  MODIFY `id_tipo_donacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `usuario`
--
ALTER TABLE `usuario`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT de la tabla `usuario_permiso`
--
ALTER TABLE `usuario_permiso`
  MODIFY `id_usuario_permiso` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=91;

--
-- AUTO_INCREMENT de la tabla `usuario_rol`
--
ALTER TABLE `usuario_rol`
  MODIFY `id_usuario_rol` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT de la tabla `voluntario`
--
ALTER TABLE `voluntario`
  MODIFY `id_voluntario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=58;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `actividades`
--
ALTER TABLE `actividades`
  ADD CONSTRAINT `fk_actividad_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Filtros para la tabla `actividad_beneficiario`
--
ALTER TABLE `actividad_beneficiario`
  ADD CONSTRAINT `fk_ab_actividad` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_ab_beneficiario` FOREIGN KEY (`id_beneficiario`) REFERENCES `beneficiario` (`id_beneficiario`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `actividad_lugar`
--
ALTER TABLE `actividad_lugar`
  ADD CONSTRAINT `actividad_lugar_ibfk_1` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`),
  ADD CONSTRAINT `actividad_lugar_ibfk_2` FOREIGN KEY (`id_lugar`) REFERENCES `lugar` (`id_lugar`);

--
-- Filtros para la tabla `actividad_recurso`
--
ALTER TABLE `actividad_recurso`
  ADD CONSTRAINT `actividad_recurso_ibfk_1` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`),
  ADD CONSTRAINT `actividad_recurso_ibfk_2` FOREIGN KEY (`id_recurso`) REFERENCES `recurso` (`id_recurso`);

--
-- Filtros para la tabla `asistencias`
--
ALTER TABLE `asistencias`
  ADD CONSTRAINT `fk_asistencia_actividad` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`),
  ADD CONSTRAINT `fk_asistencia_usuario` FOREIGN KEY (`id_usuario_registro`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_asistencia_voluntario` FOREIGN KEY (`id_voluntario`) REFERENCES `voluntario` (`id_voluntario`);

--
-- Filtros para la tabla `certificados`
--
ALTER TABLE `certificados`
  ADD CONSTRAINT `fk_cert_actividad` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`),
  ADD CONSTRAINT `fk_cert_usuario` FOREIGN KEY (`id_usuario_emite`) REFERENCES `usuario` (`id_usuario`),
  ADD CONSTRAINT `fk_cert_voluntario` FOREIGN KEY (`id_voluntario`) REFERENCES `voluntario` (`id_voluntario`);

--
-- Filtros para la tabla `donacion`
--
ALTER TABLE `donacion`
  ADD CONSTRAINT `donacion_fk_actividades` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `donacion_ibfk_1` FOREIGN KEY (`id_tipo_donacion`) REFERENCES `tipo_donacion` (`id_tipo_donacion`),
  ADD CONSTRAINT `donacion_ibfk_3` FOREIGN KEY (`id_usuario_registro`) REFERENCES `usuario` (`id_usuario`),
  ADD CONSTRAINT `fk_donacion_usuario_anula` FOREIGN KEY (`id_usuario_anula`) REFERENCES `usuario` (`id_usuario`);

--
-- Filtros para la tabla `donacion_detalle`
--
ALTER TABLE `donacion_detalle`
  ADD CONSTRAINT `fk_donacion_detalle_donacion` FOREIGN KEY (`id_donacion`) REFERENCES `donacion` (`id_donacion`),
  ADD CONSTRAINT `fk_donacion_detalle_item` FOREIGN KEY (`id_item`) REFERENCES `inventario_item` (`id_item`);

--
-- Filtros para la tabla `donacion_donante`
--
ALTER TABLE `donacion_donante`
  ADD CONSTRAINT `donacion_donante_ibfk_1` FOREIGN KEY (`id_donacion`) REFERENCES `donacion` (`id_donacion`),
  ADD CONSTRAINT `donacion_donante_ibfk_2` FOREIGN KEY (`id_donante`) REFERENCES `donante` (`id_donante`);

--
-- Filtros para la tabla `eventos_calendario`
--
ALTER TABLE `eventos_calendario`
  ADD CONSTRAINT `fk_evento_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL;

--
-- Filtros para la tabla `gasto_donacion`
--
ALTER TABLE `gasto_donacion`
  ADD CONSTRAINT `gasto_donacion_ibfk_1` FOREIGN KEY (`id_movimiento`) REFERENCES `movimiento_financiero` (`id_movimiento`) ON DELETE CASCADE,
  ADD CONSTRAINT `gasto_donacion_ibfk_2` FOREIGN KEY (`id_donacion`) REFERENCES `donacion` (`id_donacion`);

--
-- Filtros para la tabla `inventario_movimiento`
--
ALTER TABLE `inventario_movimiento`
  ADD CONSTRAINT `fk_movimiento_item` FOREIGN KEY (`id_item`) REFERENCES `inventario_item` (`id_item`),
  ADD CONSTRAINT `fk_movimiento_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`);

--
-- Filtros para la tabla `movimiento_financiero`
--
ALTER TABLE `movimiento_financiero`
  ADD CONSTRAINT `fk_mov_actividad` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_mov_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`);

--
-- Filtros para la tabla `notificaciones`
--
ALTER TABLE `notificaciones`
  ADD CONSTRAINT `notificaciones_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE;

--
-- Filtros para la tabla `participacion`
--
ALTER TABLE `participacion`
  ADD CONSTRAINT `participacion_ibfk_1` FOREIGN KEY (`id_voluntario`) REFERENCES `voluntario` (`id_voluntario`),
  ADD CONSTRAINT `participacion_ibfk_2` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`),
  ADD CONSTRAINT `participacion_ibfk_3` FOREIGN KEY (`id_rol_actividad`) REFERENCES `rol_actividad` (`id_rol_actividad`);

--
-- Filtros para la tabla `recurso_campana`
--
ALTER TABLE `recurso_campana`
  ADD CONSTRAINT `fk_rc_actividad` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`),
  ADD CONSTRAINT `fk_rc_item` FOREIGN KEY (`id_item`) REFERENCES `inventario_item` (`id_item`);

--
-- Filtros para la tabla `salida_donacion`
--
ALTER TABLE `salida_donacion`
  ADD CONSTRAINT `salida_donacion_ibfk_1` FOREIGN KEY (`id_donacion`) REFERENCES `donacion` (`id_donacion`),
  ADD CONSTRAINT `salida_donacion_ibfk_2` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`);

--
-- Filtros para la tabla `salida_inventario`
--
ALTER TABLE `salida_inventario`
  ADD CONSTRAINT `salida_inventario_ibfk_1` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`),
  ADD CONSTRAINT `salida_inventario_ibfk_2` FOREIGN KEY (`id_usuario_registro`) REFERENCES `usuario` (`id_usuario`);

--
-- Filtros para la tabla `salida_inventario_detalle`
--
ALTER TABLE `salida_inventario_detalle`
  ADD CONSTRAINT `salida_inventario_detalle_ibfk_1` FOREIGN KEY (`id_salida_inv`) REFERENCES `salida_inventario` (`id_salida_inv`),
  ADD CONSTRAINT `salida_inventario_detalle_ibfk_2` FOREIGN KEY (`id_item`) REFERENCES `inventario_item` (`id_item`);

--
-- Filtros para la tabla `usuario_permiso`
--
ALTER TABLE `usuario_permiso`
  ADD CONSTRAINT `up_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE,
  ADD CONSTRAINT `up_ibfk_2` FOREIGN KEY (`id_permiso`) REFERENCES `permiso` (`id_permiso`) ON DELETE CASCADE;

--
-- Filtros para la tabla `usuario_rol`
--
ALTER TABLE `usuario_rol`
  ADD CONSTRAINT `usuario_rol_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`),
  ADD CONSTRAINT `usuario_rol_ibfk_2` FOREIGN KEY (`id_rol_sistema`) REFERENCES `rol_sistema` (`id_rol_sistema`);

--
-- Filtros para la tabla `voluntario`
--
ALTER TABLE `voluntario`
  ADD CONSTRAINT `fk_voluntario_rol` FOREIGN KEY (`id_rol_actividad`) REFERENCES `rol_actividad` (`id_rol_actividad`),
  ADD CONSTRAINT `voluntario_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
