-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 27-02-2026 a las 00:37:49
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_beneficiario` (IN `p_id_beneficiario` INT, IN `p_nombres` VARCHAR(100), IN `p_apellidos` VARCHAR(100), IN `p_dni` VARCHAR(20), IN `p_fecha_nacimiento` DATE, IN `p_telefono` VARCHAR(20), IN `p_direccion` VARCHAR(255), IN `p_distrito` VARCHAR(100), IN `p_tipo_beneficiario` VARCHAR(20), IN `p_necesidad_principal` VARCHAR(30), IN `p_observaciones` TEXT)   BEGIN
    UPDATE beneficiario
    SET nombres             = p_nombres,
        apellidos           = p_apellidos,
        dni                 = p_dni,
        fecha_nacimiento    = p_fecha_nacimiento,
        telefono            = p_telefono,
        direccion           = p_direccion,
        distrito            = p_distrito,
        tipo_beneficiario   = p_tipo_beneficiario,
        necesidad_principal = p_necesidad_principal,
        observaciones       = p_observaciones
    WHERE id_beneficiario   = p_id_beneficiario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_detalle_especie` (IN `p_id_donacion` INT, IN `p_cantidad` DECIMAL(10,2), IN `p_observacion` VARCHAR(255))   BEGIN
    UPDATE donacion_detalle
    SET cantidad = p_cantidad,
        observacion = p_observacion
    WHERE id_donacion = p_id_donacion;

    SELECT ROW_COUNT() AS filas_afectadas;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_donacion_inventario` (IN `p_id_donacion` INT, IN `p_cantidad` DECIMAL(10,2), IN `p_descripcion` VARCHAR(150), IN `p_id_actividad` INT, IN `p_donacion_anonima` TINYINT, IN `p_donante_tipo` VARCHAR(20), IN `p_donante_nombre` VARCHAR(150), IN `p_donante_correo` VARCHAR(100), IN `p_donante_telefono` VARCHAR(30), IN `p_donante_dni` VARCHAR(20), IN `p_donante_ruc` VARCHAR(20), IN `p_id_usuario_edicion` INT, IN `p_motivo_edicion` VARCHAR(255))   BEGIN
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_salida_donacion` (IN `p_id_salida` INT, IN `p_id_actividad` INT, IN `p_cantidad` DOUBLE, IN `p_descripcion` TEXT, IN `p_id_item` INT, IN `p_cantidad_item` DOUBLE)   BEGIN
    UPDATE salida_donacion
    SET id_actividad = p_id_actividad,
        cantidad = p_cantidad,
        descripcion = p_descripcion,
        id_item = IF(p_id_item = 0, NULL, p_id_item),
        cantidad_item = IF(p_cantidad_item = 0, NULL, p_cantidad_item),
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_beneficiario` (IN `p_nombres` VARCHAR(100), IN `p_apellidos` VARCHAR(100), IN `p_dni` VARCHAR(20), IN `p_fecha_nacimiento` DATE, IN `p_telefono` VARCHAR(20), IN `p_direccion` VARCHAR(255), IN `p_distrito` VARCHAR(100), IN `p_tipo_beneficiario` VARCHAR(20), IN `p_necesidad_principal` VARCHAR(30), IN `p_observaciones` TEXT, IN `p_id_usuario` INT)   BEGIN
    INSERT INTO beneficiario (nombres, apellidos, dni, fecha_nacimiento, telefono,
                              direccion, distrito, tipo_beneficiario,
                              necesidad_principal, observaciones, id_usuario)
    VALUES (p_nombres, p_apellidos, p_dni, p_fecha_nacimiento, p_telefono,
            p_direccion, p_distrito, p_tipo_beneficiario,
            p_necesidad_principal, p_observaciones, p_id_usuario);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_beneficiario_adaptado` (IN `p_nombres` VARCHAR(100), IN `p_apellidos` VARCHAR(100), IN `p_dni` VARCHAR(20), IN `p_fecha_nacimiento` DATE, IN `p_telefono` VARCHAR(20), IN `p_direccion` VARCHAR(255), IN `p_distrito` VARCHAR(100), IN `p_tipo_beneficiario` VARCHAR(100), IN `p_necesidad_principal` VARCHAR(100), IN `p_observaciones` TEXT, IN `p_id_usuario` INT)   BEGIN
  INSERT INTO beneficiario (
    nombre,
    descripcion,
    estado,
    id_tipo_beneficiario
  ) VALUES (
    CONCAT(TRIM(p_nombres), ' ', TRIM(p_apellidos)),
    COALESCE(NULLIF(p_observaciones, ''), p_necesidad_principal),
    'activo',
    NULL
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
    INSERT INTO usuario (nombres, apellidos, correo, username, dni, password_hash, estado, creado_en)
    VALUES (p_nombres, p_apellidos, p_correo, p_username, p_dni, p_password_hash, 'ACTIVO', NOW());
    
    SELECT LAST_INSERT_ID() AS id_usuario;
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
        (SELECT COUNT(*) FROM donacion WHERE estado <> 'ANULADO') AS total_donaciones,
        COALESCE((SELECT SUM(d.cantidad)
                  FROM donacion d
                  INNER JOIN tipo_donacion td ON d.id_tipo_donacion = td.id_tipo_donacion
                  WHERE td.nombre = 'DINERO' AND d.estado <> 'ANULADO'), 0) AS monto_donaciones,
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_evento` (IN `p_id_evento` INT)   BEGIN
    DELETE FROM eventos_calendario WHERE id_evento = p_id_evento;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_permisos_usuario` (IN `p_id_usuario` INT)   BEGIN
    DELETE FROM usuario_permiso WHERE id_usuario = p_id_usuario;
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_filtrarMovimientos` (IN `p_tipo` VARCHAR(10), IN `p_categoria` VARCHAR(60), IN `p_fecha_ini` DATE, IN `p_fecha_fin` DATE)   BEGIN
    SELECT m.id_movimiento, m.tipo, m.monto, m.descripcion,
           m.categoria, m.comprobante, m.fecha_movimiento,
           IFNULL(a.nombre, '???') AS actividad,
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
    INSERT INTO notificaciones (id_usuario, titulo, mensaje, tipo, leida, creada_en)
    SELECT 
        p_id_usuario,
        CONCAT('📅 Evento hoy: ', e.titulo),
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
            AND n.titulo = CONCAT('📅 Evento hoy: ', e.titulo)
            AND DATE(n.creada_en) = CURDATE()
      );
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
        dnt.telefono AS telefonoDonante
    FROM donacion d
    LEFT JOIN tipo_donacion td ON d.id_tipo_donacion = td.id_tipo_donacion
    LEFT JOIN actividades a ON d.id_actividad = a.id_actividad
    LEFT JOIN usuario u ON d.id_usuario_registro = u.id_usuario
    LEFT JOIN donacion_donante ddon ON d.id_donacion = ddon.id_donacion
    LEFT JOIN donante dnt ON ddon.id_donante = dnt.id_donante
    LEFT JOIN donacion_detalle ddet ON d.id_donacion = ddet.id_donacion
    LEFT JOIN inventario_item ii ON ddet.id_item = ii.id_item
    WHERE COALESCE(d.estado, 'PENDIENTE') NOT IN ('ANULADO')
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
        CONCAT(u.nombre, ' ', u.apellido) AS usuario_registro,
        DATE_FORMAT(si.registrado_en, '%d/%m/%Y %H:%i') AS registrado_en,
        si.estado,
        CASE WHEN si.anulado_en IS NOT NULL 
             THEN DATE_FORMAT(si.anulado_en, '%d/%m/%Y %H:%i') 
             ELSE NULL END AS anulado_en,
        si.motivo_anulacion,
        COALESCE(det.total_items, 0) AS total_items,
        COALESCE(det.total_cantidad, 0) AS total_cantidad
    FROM salida_inventario si
    LEFT JOIN actividad a ON si.id_actividad = a.id_actividad
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
           (SELECT COUNT(*) FROM participacion p
            WHERE p.id_actividad = a.id_actividad) AS inscritos,
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
        dnt.telefono AS telefonoDonante
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_roles_por_usuario` ()   BEGIN
    SELECT ur.id_usuario, rs.nombre_rol
    FROM usuario_rol ur
    INNER JOIN rol_sistema rs ON ur.id_rol_sistema = rs.id_rol_sistema
    ORDER BY ur.id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_saldo_donacion` (IN `p_id_donacion` INT)   BEGIN
    SELECT
        d.cantidad AS cantidad_original,
        d.cantidad - COALESCE(
            (SELECT SUM(s.cantidad) FROM salida_donacion s
             WHERE s.id_donacion = d.id_donacion AND s.estado != 'ANULADO'), 0
        ) AS saldo_disponible,
        td.nombre AS tipo_donacion
    FROM donacion d
    INNER JOIN tipo_donacion td ON td.id_tipo_donacion = d.id_tipo_donacion
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
        d.descripcion AS donacion_descripcion
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
        CONCAT(u.nombre, ' ', u.apellido) AS usuario_registro,
        DATE_FORMAT(si.registrado_en, '%d/%m/%Y %H:%i') AS registrado_en,
        si.estado,
        CASE WHEN si.anulado_en IS NOT NULL 
             THEN DATE_FORMAT(si.anulado_en, '%d/%m/%Y %H:%i') 
             ELSE NULL END AS anulado_en,
        si.motivo_anulacion
    FROM salida_inventario si
    LEFT JOIN actividad a ON si.id_actividad = a.id_actividad
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
           (SELECT COUNT(*) FROM participacion p
            WHERE p.id_actividad = a.id_actividad) AS inscritos,
           a.estado, a.id_usuario, a.creado_en
    FROM actividades a
    ORDER BY a.creado_en DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_todos_beneficiarios` ()   BEGIN
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_donacion_inventario` (IN `p_cantidad` DECIMAL(10,2), IN `p_descripcion` VARCHAR(150), IN `p_id_tipo_donacion` INT, IN `p_id_actividad` INT, IN `p_id_usuario_registro` INT, IN `p_id_item` INT, IN `p_crear_nuevo_item` TINYINT, IN `p_item_nombre` VARCHAR(150), IN `p_item_categoria` VARCHAR(50), IN `p_item_unidad_medida` VARCHAR(30), IN `p_item_stock_minimo` DECIMAL(10,2), IN `p_donacion_anonima` TINYINT, IN `p_donante_tipo` VARCHAR(20), IN `p_donante_nombre` VARCHAR(150), IN `p_donante_correo` VARCHAR(100), IN `p_donante_telefono` VARCHAR(30), IN `p_donante_dni` VARCHAR(20), IN `p_donante_ruc` VARCHAR(20))   BEGIN
    DECLARE v_id_donacion INT;
    DECLARE v_id_item INT DEFAULT NULL;
    DECLARE v_id_donante INT DEFAULT NULL;
    DECLARE v_tipo_donante VARCHAR(20) DEFAULT NULL;
    DECLARE v_stock_anterior DECIMAL(10,2) DEFAULT 0;
    DECLARE v_stock_nuevo DECIMAL(10,2) DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF p_cantidad IS NULL OR p_cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad/monto de donacion debe ser mayor a cero.';
    END IF;

    INSERT INTO donacion(cantidad, descripcion, id_tipo_donacion, id_actividad, id_usuario_registro, registrado_en, estado)
    VALUES(p_cantidad, p_descripcion, p_id_tipo_donacion, p_id_actividad, p_id_usuario_registro, NOW(), 'PENDIENTE');

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

    
    IF p_id_tipo_donacion = 2 THEN
        SET @dummy = 1;
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_salida_donacion` (IN `p_id_donacion` INT, IN `p_id_actividad` INT, IN `p_tipo_salida` VARCHAR(20), IN `p_cantidad` DOUBLE, IN `p_descripcion` TEXT, IN `p_id_item` INT, IN `p_cantidad_item` DOUBLE, IN `p_id_usuario` INT)   BEGIN
    INSERT INTO salida_donacion (
        id_donacion, id_actividad, tipo_salida, cantidad,
        descripcion, id_item, cantidad_item, id_usuario_registro, estado
    ) VALUES (
        p_id_donacion, p_id_actividad, p_tipo_salida, p_cantidad,
        p_descripcion,
        IF(p_id_item = 0, NULL, p_id_item),
        IF(p_cantidad_item = 0, NULL, p_cantidad_item),
        p_id_usuario, 'PENDIENTE'
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
-- Estructura de tabla para la tabla `actividad`
--

CREATE TABLE `actividad` (
  `id_actividad` int(11) NOT NULL,
  `nombre_actividad` varchar(150) DEFAULT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `estado` varchar(30) DEFAULT NULL,
  `id_beneficiario` int(11) DEFAULT NULL,
  `id_usuario_creador` int(11) DEFAULT NULL,
  `creado_en` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
(11, 'campaña de donacion de sangre', 'campaña de donacion de sangre', '2026-02-11', '2026-02-12', 'uss', 50, 0, 'ACTIVO', 21, '2026-02-11 21:07:39'),
(12, 'Campaña de Reforestacion en Pimentel', 'Jornada de plantacion de Arboles nativos en las zonas costeras de Pimentel para combatir la desertificacion y promover la conciencia ambiental en la comunidad.', '2025-07-15', '2025-07-15', 'Playa Pimentel, Chiclayo, Lambayeque', 2, 2, 'ACTIVO', NULL, '2026-02-23 15:53:10'),
(13, 'Donacion de utiles Escolares - Josu Leonardo Ortiz', 'Recoleccion y entrega de Utiles escolares a niños de bajos recursos del distrito Josu Leonardo Ortiz, en coordinacion con instituciones educativas locales.', '2025-07-20', '2025-07-20', 'I.E. Karl Weiss, Josu Leonardo Ortiz, Chiclayo', 30, 2, 'ACTIVO', NULL, '2026-02-23 15:53:10'),
(14, 'Operativo Medico Gratuito en La Victoria', 'Atencion medica gratuita (medicina general, odontologia, oftalmologia) para familias vulnerables del distrito La Victoria de Chiclayo.', '2025-08-02', '2025-08-02', 'Centro Comunal La Victoria, Chiclayo, Lambayeque', 50, 0, 'ACTIVO', NULL, '2026-02-23 15:53:10'),
(15, 'Limpieza del Dren 4000 - Chiclayo', 'Jornada de limpieza y concientizacion ambiental en las riberas del Dren 4000, uno de los principales canales de drenaje de la ciudad de Chiclayo.', '2025-08-10', '2025-08-10', 'Dren 4000, Av. Chinchaysuyo, Chiclayo', 35, 0, 'ACTIVO', NULL, '2026-02-23 15:53:10'),
(16, 'Taller de Capacitacion Digital para Adultos Mayores', 'Taller de alfabetizacion digital para adultos mayores, enseñando uso de celulares, redes sociales y tramites en linea, en el centro del adulto mayor de Chiclayo.', '2025-08-16', '2025-08-16', 'Centro del Adulto Mayor - EsSalud, Chiclayo, Lambayeque', 25, 0, 'ACTIVO', NULL, '2026-02-23 15:53:10'),
(18, 'hh', 'hhh', '2026-02-24', '2026-02-26', 'hh', 10, 0, 'ACTIVO', 21, '2026-02-24 14:06:56');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `actividad_lugar`
--

CREATE TABLE `actividad_lugar` (
  `id_actividad_lugar` int(11) NOT NULL,
  `id_actividad` int(11) DEFAULT NULL,
  `id_lugar` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
(6, 24, 12, '2025-07-15', '07:00:00', '13:00:00', 6.00, 'ASISTIO', NULL, 21, '2026-02-24 06:15:25', '2026-02-24 06:15:25'),
(7, 25, 12, '2025-07-15', '07:00:00', '11:00:00', 4.00, 'ASISTIO', NULL, 21, '2026-02-24 06:39:09', '2026-02-24 06:39:09'),
(8, 26, 13, '2025-07-20', '07:00:00', '12:00:00', 5.00, 'ASISTIO', NULL, 21, '2026-02-24 13:37:05', '2026-02-24 13:37:05'),
(9, 25, 13, '2025-07-20', '07:00:00', '11:00:00', 4.00, 'ASISTIO', NULL, 21, '2026-02-24 15:28:04', '2026-02-24 15:28:04');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `beneficiario`
--

CREATE TABLE `beneficiario` (
  `id_beneficiario` int(11) NOT NULL,
  `nombre` varchar(150) DEFAULT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  `estado` varchar(30) DEFAULT 'activo',
  `id_tipo_beneficiario` int(11) DEFAULT NULL,
  `nombres` varchar(100) DEFAULT NULL,
  `apellidos` varchar(100) DEFAULT NULL,
  `dni` varchar(20) DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `distrito` varchar(100) DEFAULT NULL,
  `tipo_beneficiario` varchar(100) DEFAULT NULL,
  `necesidad_principal` varchar(100) DEFAULT NULL,
  `observaciones` text DEFAULT NULL,
  `id_usuario` int(11) DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `beneficiario`
--

INSERT INTO `beneficiario` (`id_beneficiario`, `nombre`, `descripcion`, `estado`, `id_tipo_beneficiario`, `nombres`, `apellidos`, `dni`, `fecha_nacimiento`, `telefono`, `direccion`, `distrito`, `tipo_beneficiario`, `necesidad_principal`, `observaciones`, `id_usuario`, `creado_en`) VALUES
(3, NULL, NULL, 'ACTIVO', NULL, 'María Elena', 'Sánchez Torres', '43512678', '1985-03-12', '974123456', 'Av. Balta 456, Chiclayo', 'CHICLAYO', 'INDIVIDUAL', 'SALUD', 'Madre soltera con 3 hijos, requiere apoyo médico.', 1, '2026-02-18 04:53:09'),
(4, NULL, NULL, 'ACTIVO', NULL, 'Carlos Jesús', 'Burga Díaz', '51234987', '1972-07-25', '961234789', 'Jr. Elías Aguirre 234, La Victoria', 'LA VICTORIA', 'FAMILIA', 'ALIMENTACIÓN', 'Familia de 5 personas con bajos recursos, sin trabajo estable.', 1, '2026-02-18 04:53:09'),
(5, NULL, NULL, 'ACTIVO', NULL, 'Rosa Amalia', 'Chafloque Llanos', '47896321', '1990-11-08', '943876541', 'Calle Los Álamos 89, José L. Ortiz', 'J. L. ORTIZ', 'INDIVIDUAL', 'EDUCACIÓN', 'Joven con discapacidad visual, busca apoyo para continuar estudios.', 1, '2026-02-18 04:53:09'),
(6, NULL, NULL, 'ACTIVO', NULL, 'Pedro Antonio', 'Puyen Montenegro', '38741256', '1960-05-30', '987654310', 'Av. Grau 712, Chiclayo', 'CHICLAYO', 'COMUNIDAD', 'VIVIENDA', 'Adulto mayor sin vivienda propia, vive en situación de precariedad.', 1, '2026-02-18 04:53:09'),
(7, NULL, NULL, 'ACTIVO', NULL, 'Lucía del Pilar', 'Llontop Vera', '55231478', '2000-02-14', '956321470', 'Mz. D Lt. 8 AA.HH. Túpac Amaru', 'POMALCA', 'FAMILIA', 'OTRO', 'Joven universitaria con familia numerosa, requiere apoyo multisectorial.', 1, '2026-02-18 04:53:09');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `beneficiario_old`
--

CREATE TABLE `beneficiario_old` (
  `id_beneficiario` int(11) NOT NULL,
  `nombre` varchar(150) DEFAULT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  `estado` varchar(30) DEFAULT NULL,
  `id_tipo_beneficiario` int(11) DEFAULT NULL,
  `nombres` varchar(100) DEFAULT NULL,
  `apellidos` varchar(100) DEFAULT NULL,
  `dni` varchar(20) DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `distrito` varchar(100) DEFAULT NULL,
  `tipo_beneficiario` varchar(100) DEFAULT NULL,
  `necesidad_principal` varchar(100) DEFAULT NULL,
  `observaciones` text DEFAULT NULL,
  `id_usuario` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `beneficiario_old`
--

INSERT INTO `beneficiario_old` (`id_beneficiario`, `nombre`, `descripcion`, `estado`, `id_tipo_beneficiario`, `nombres`, `apellidos`, `dni`, `fecha_nacimiento`, `telefono`, `direccion`, `distrito`, `tipo_beneficiario`, `necesidad_principal`, `observaciones`, `id_usuario`) VALUES
(1, 'Juan Pérez', 'Beneficiario de prueba en Chiclayo', 'activo', NULL, 'Juan', 'Pérez', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Beneficiario de prueba en Chiclayo', NULL),
(2, NULL, NULL, 'activo', NULL, 'Prueba', 'Usuario', '87654321', '1980-01-01', '999888777', 'Calle Falsa 123', 'Chiclayo', 'Adulto mayor', 'Alimentos', 'Registro de prueba', 1);

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
(4, 'CERT-2026-0001', 24, 12, 6, '2026-02-24', 'EMITIDO', '', 21, NULL, NULL, '2026-02-24 06:37:33', '2026-02-24 06:37:33'),
(5, 'CERT-2026-0002', 26, 13, 5, '2026-02-24', 'EMITIDO', '', 21, NULL, NULL, '2026-02-24 13:37:51', '2026-02-24 13:37:51');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `donacion`
--

CREATE TABLE `donacion` (
  `id_donacion` int(11) NOT NULL,
  `cantidad` decimal(10,2) DEFAULT NULL,
  `descripcion` varchar(150) DEFAULT NULL,
  `id_tipo_donacion` int(11) DEFAULT NULL,
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

INSERT INTO `donacion` (`id_donacion`, `cantidad`, `descripcion`, `id_tipo_donacion`, `id_actividad`, `id_usuario_registro`, `registrado_en`, `estado`, `anulado_en`, `id_usuario_anula`, `motivo_anulacion`, `actualizado_en`) VALUES
(2, 2.00, 'arroz', 2, NULL, 21, '2026-02-14 23:21:01', 'CONFIRMADO', NULL, NULL, NULL, '2026-02-14 23:22:06'),
(3, 8.00, 'donaciones', 2, NULL, 21, '2026-02-14 23:25:30', 'ANULADO', NULL, NULL, NULL, NULL),
(4, 3000.00, 'donaciones', 1, 11, 21, '2026-02-14 23:38:45', 'CONFIRMADO', NULL, NULL, NULL, NULL),
(5, 2500.00, 'donacion', 1, 11, 21, '2026-02-15 00:47:10', 'CONFIRMADO', NULL, NULL, NULL, NULL),
(6, 300.00, 'donacion', 1, NULL, 21, '2026-02-15 02:37:34', 'CONFIRMADO', NULL, NULL, NULL, NULL),
(7, 500.00, 'donacion', 1, NULL, 21, '2026-02-15 03:03:42', 'CONFIRMADO', NULL, NULL, NULL, NULL),
(8, 900.00, 'donaciones', 1, 11, 21, '2026-02-16 08:13:37', 'CONFIRMADO', NULL, NULL, NULL, NULL),
(9, 6.00, 'ggggg', 2, 11, 21, '2026-02-16 08:18:13', 'CONFIRMADO', NULL, NULL, NULL, NULL),
(38, 4555.00, 'dine', 1, 15, 21, '2026-02-25 16:44:03', 'CONFIRMADO', NULL, NULL, NULL, '2026-02-25 16:55:48');

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

--
-- Volcado de datos para la tabla `donacion_detalle`
--

INSERT INTO `donacion_detalle` (`id_donacion_detalle`, `id_donacion`, `id_item`, `cantidad`, `observacion`, `creado_en`) VALUES
(1, 2, 1, 2.00, 'arroz', '2026-02-14 23:21:01'),
(2, 3, 1, 8.00, 'donaciones', '2026-02-14 23:25:30');

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
(3, 2, 1),
(4, 3, 2),
(5, 4, 3),
(6, 5, 4),
(7, 6, 5),
(8, 7, 6),
(9, 8, 7),
(10, 9, 8);

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
(8, 'Empresa', 'ASOCIACION VIDA Y VOLUNTARIADO - VIVOL', 'asociacion@gmail.com', '987456378', '20602952801', NULL);

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
(1, 'cumpleños', 'de luis', '2026-02-12', '2026-02-12', '#eab308', 21, '2026-02-12 03:21:53');

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
(1, 'Arroz Costeño', 'ALIMENTOS', 'kg', 11.00, 90.00, 'ACTIVO', '', '2026-02-14 23:19:51', '2026-02-24 09:28:57');

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

--
-- Volcado de datos para la tabla `inventario_movimiento`
--

INSERT INTO `inventario_movimiento` (`id_movimiento`, `id_item`, `tipo_movimiento`, `motivo`, `cantidad`, `stock_anterior`, `stock_nuevo`, `id_referencia`, `tabla_referencia`, `observacion`, `id_usuario`, `creado_en`) VALUES
(1, 1, 'ENTRADA', 'DONACION', 2.00, 0.00, 2.00, 2, 'donacion', 'arroz', 21, '2026-02-14 23:21:01'),
(2, 1, 'ENTRADA', 'DONACION', 8.00, 2.00, 10.00, 3, 'donacion', 'donaciones', 21, '2026-02-14 23:25:30'),
(3, 1, 'ENTRADA', 'CONSUMO', 1.00, 10.00, 11.00, NULL, NULL, '', 21, '2026-02-15 02:18:48');

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
(1, 'GASTO', 200.00, 'pasajes', 'Transporte', '002', '2026-02-12', 11, 21, '2026-02-12 09:58:55'),
(2, 'INGRESO', 3000.00, 'Donación: donaciones (Donacion #4)', 'Donaciones', NULL, '2026-02-14', 11, 21, '2026-02-14 23:38:45'),
(3, 'INGRESO', 2500.00, 'Donación: donacion (Donacion #5)', 'Donaciones', '20605005994', '2026-02-15', 11, 21, '2026-02-15 00:47:10'),
(4, 'INGRESO', 300.00, 'Donación: donacion (Donacion #6)', 'Donaciones', 'BOLETA-6', '2026-02-15', NULL, 21, '2026-02-15 02:37:34'),
(5, 'INGRESO', 500.00, 'Donación: donacion (Donacion #7)', 'Donaciones', 'BOLETA-7', '2026-02-15', NULL, 21, '2026-02-15 03:03:52'),
(6, 'INGRESO', 900.00, 'Donación: donaciones (Donacion #8)', 'Donaciones', 'BOLETA-8', '2026-02-16', 11, 21, '2026-02-16 08:14:21'),
(7, 'GASTO', 2500.00, 'gastos', 'Materiales', '002', '2026-02-17', 11, 21, '2026-02-17 11:04:03'),
(8, 'GASTO', 500.00, 'hh', 'Donaciones', '001', '2026-02-24', 14, 21, NULL),
(9, 'GASTO', 500.00, 'hh', 'Donaciones', '001', '2026-02-24', 14, 21, NULL),
(10, 'INGRESO', 500.00, 'hh', 'Donaciones', '001', '2026-02-24', 14, 21, NULL),
(11, 'GASTO', 800.00, 'gg', 'Materiales', '002', '2026-02-24', 15, 21, '2026-02-24 09:19:26');

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
(3, 21, 'ACTIVIDAD_HOY', '📋 Actividad hoy: campaña de donacion de sangre', 'La actividad \"campaña de donacion de sangre\" está programada para hoy en uss.', 'fa-calendar-check', '#10b981', 1, 11, '2026-02-11 16:07:46'),
(5, 21, 'ACTIVIDAD_HOY', 'Actividad hoy: Taller de Primeros Auxilios', 'La actividad \"Taller de Primeros Auxilios\" está programada para hoy en Centro Comunitario San Martín.', 'fa-calendar-check', '#10b981', 1, 2, '2026-02-20 01:53:35'),
(6, 21, 'ACTIVIDAD_HOY', 'Actividad hoy: Taller de Primeros Auxilios', 'La actividad \"Taller de Primeros Auxilios\" está programada para hoy en Centro Comunitario San Martín.', 'fa-calendar-check', '#10b981', 1, 7, '2026-02-20 01:53:35'),
(7, 21, 'ACTIVIDAD_HOY', '📋 Actividad hoy: hola', 'La actividad \"hola\" está programada para hoy en jjj.', 'fa-calendar-check', '#10b981', 0, 17, '2026-02-24 09:01:47'),
(8, 21, 'ACTIVIDAD_HOY', '📋 Actividad hoy: hh', 'La actividad \"hh\" está programada para hoy en hh.', 'fa-calendar-check', '#10b981', 0, 18, '2026-02-24 09:17:33');

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
(12, 'salidas_donaciones.ver', NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `programacion_donacion`
--

CREATE TABLE `programacion_donacion` (
  `id_programacion` int(11) NOT NULL,
  `fecha_programada` date DEFAULT NULL,
  `estado` varchar(30) DEFAULT NULL,
  `id_donacion` int(11) DEFAULT NULL,
  `id_voluntario` int(11) DEFAULT NULL,
  `id_usuario_registro` int(11) DEFAULT NULL,
  `id_beneficiario` int(11) DEFAULT NULL,
  `id_lugar` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `recurso`
--

CREATE TABLE `recurso` (
  `id_recurso` int(11) NOT NULL,
  `nombre` varchar(120) DEFAULT NULL,
  `unidad_medida` varchar(30) DEFAULT NULL,
  `tipo_recurso` varchar(30) DEFAULT NULL,
  `descripcion` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `responsable_beneficiario`
--

CREATE TABLE `responsable_beneficiario` (
  `id_responsable` int(11) NOT NULL,
  `nombres` varchar(100) DEFAULT NULL,
  `apellidos` varchar(100) DEFAULT NULL,
  `cargo` varchar(100) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `id_beneficiario` int(11) DEFAULT NULL
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
-- Estructura de tabla para la tabla `rol_permiso`
--

CREATE TABLE `rol_permiso` (
  `id_rol_permiso` int(11) NOT NULL,
  `id_rol_sistema` int(11) DEFAULT NULL,
  `id_permiso` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `rol_permiso`
--

INSERT INTO `rol_permiso` (`id_rol_permiso`, `id_rol_sistema`, `id_permiso`) VALUES
(1, 1, 1),
(2, 1, 2),
(3, 1, 3),
(12, 1, 3),
(14, 1, 4),
(16, 1, 5),
(18, 1, 6),
(20, 1, 7),
(22, 1, 8),
(24, 1, 9),
(26, 1, 10),
(28, 1, 11),
(30, 1, 12),
(32, 1, 13),
(34, 1, 14),
(36, 1, 12),
(37, 1, 12),
(38, 1, 12),
(39, 1, 12);

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
(1, 8, 12, 'DINERO', 500, '', NULL, NULL, 21, '2026-02-25 00:26:42', 'CONFIRMADO', NULL, NULL, NULL, '2026-02-25 00:27:04'),
(2, 7, 15, 'DINERO', 500, '', NULL, NULL, 21, '2026-02-25 08:54:05', 'CONFIRMADO', NULL, NULL, NULL, '2026-02-25 08:54:11');

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
-- Estructura de tabla para la tabla `tipo_beneficiario`
--

CREATE TABLE `tipo_beneficiario` (
  `id_tipo_beneficiario` int(11) NOT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `descripcion` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `tipo_beneficiario`
--

INSERT INTO `tipo_beneficiario` (`id_tipo_beneficiario`, `nombre`, `descripcion`) VALUES
(1, 'Orfanato', 'Centro de atención a menores sin familia'),
(2, 'Asilo', 'Centro para personas adultas mayores'),
(3, 'Comunidad Indígena', 'Comunidades originarias'),
(4, 'Zona Rural', 'Área rural necesitada'),
(5, 'Orfanato', 'Centro de atención a menores sin familia'),
(6, 'Asilo', 'Centro para personas adultas mayores'),
(7, 'Comunidad Indígena', 'Comunidades originarias'),
(8, 'Zona Rural', 'Área rural necesitada');

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
(28, 'ROSA FIORELLA', 'VICUÑA MUNAYCO', 'rosa@gmail.con', 'rosa', NULL, '$2a$10$ALUOyQo6GeWeys6g3KTNUOTUtIB07EkIJgkqzWwEn0T81nmpcVMJ6', NULL, 'ACTIVO', '2026-02-23 10:45:16', '2026-02-23 10:45:16', 0, NULL);

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
(57, 28, 6),
(58, 28, 7);

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

--
-- Volcado de datos para la tabla `usuario_rol`
--

INSERT INTO `usuario_rol` (`id_usuario_rol`, `id_usuario`, `id_rol_sistema`, `asignado_en`) VALUES
(29, 21, 4, NULL),
(31, 28, 3, '2026-02-23 10:45:16');

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
(10, 'Luis', 'chinchay', '71852009', 'Geordy_31_71@hotmail.com', '967271494', 'sistemas', 'Voluntario', 0, 'ACTIVO', NULL, NULL),
(23, 'KELLY PAOLA', 'ESPINOZA ROJAS', '46401524', 'keyla@gmail.com', '987456741', 'contadora', 'Líder de Equipo', 1, 'ACTIVO', NULL, NULL),
(24, 'ROSA FIORELLA', 'VICUÑA MUNAYCO', '71854125', 'rosa@gmail.con', '987456214', 'Enfermeria', 'Coordinador de Proyecto', 1, 'ACTIVO', 28, NULL),
(25, 'MANUEL', 'RODRIGUEZ MOLINA', '78451240', 'manuel@gmail.com', '963258741', 'Ingeneria Civil', 'Voluntario', 0, 'ACTIVO', NULL, NULL),
(26, 'JESUS JUNIOR', 'GONZALES RAMOS', '71965478', 'jesus@gmail.com', '963852147', 'ingeneria civil', 'Encargado de Logística', 0, 'ACTIVO', 21, NULL);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `actividad`
--
ALTER TABLE `actividad`
  ADD PRIMARY KEY (`id_actividad`),
  ADD KEY `id_beneficiario` (`id_beneficiario`),
  ADD KEY `id_usuario_creador` (`id_usuario_creador`);

--
-- Indices de la tabla `actividades`
--
ALTER TABLE `actividades`
  ADD PRIMARY KEY (`id_actividad`),
  ADD KEY `fk_actividad_usuario` (`id_usuario`);

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
-- Indices de la tabla `beneficiario_old`
--
ALTER TABLE `beneficiario_old`
  ADD PRIMARY KEY (`id_beneficiario`),
  ADD KEY `id_tipo_beneficiario` (`id_tipo_beneficiario`),
  ADD KEY `idx_beneficiario_id_usuario` (`id_usuario`);

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
-- Indices de la tabla `programacion_donacion`
--
ALTER TABLE `programacion_donacion`
  ADD PRIMARY KEY (`id_programacion`),
  ADD KEY `id_donacion` (`id_donacion`),
  ADD KEY `id_voluntario` (`id_voluntario`),
  ADD KEY `id_usuario_registro` (`id_usuario_registro`),
  ADD KEY `id_beneficiario` (`id_beneficiario`),
  ADD KEY `id_lugar` (`id_lugar`);

--
-- Indices de la tabla `recurso`
--
ALTER TABLE `recurso`
  ADD PRIMARY KEY (`id_recurso`);

--
-- Indices de la tabla `responsable_beneficiario`
--
ALTER TABLE `responsable_beneficiario`
  ADD PRIMARY KEY (`id_responsable`),
  ADD KEY `id_beneficiario` (`id_beneficiario`);

--
-- Indices de la tabla `rol_actividad`
--
ALTER TABLE `rol_actividad`
  ADD PRIMARY KEY (`id_rol_actividad`);

--
-- Indices de la tabla `rol_permiso`
--
ALTER TABLE `rol_permiso`
  ADD PRIMARY KEY (`id_rol_permiso`),
  ADD KEY `id_rol_sistema` (`id_rol_sistema`),
  ADD KEY `id_permiso` (`id_permiso`);

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
  ADD KEY `id_actividad` (`id_actividad`),
  ADD KEY `id_usuario_registro` (`id_usuario_registro`);

--
-- Indices de la tabla `salida_inventario_detalle`
--
ALTER TABLE `salida_inventario_detalle`
  ADD PRIMARY KEY (`id_detalle`),
  ADD KEY `id_salida_inv` (`id_salida_inv`),
  ADD KEY `id_item` (`id_item`);

--
-- Indices de la tabla `tipo_beneficiario`
--
ALTER TABLE `tipo_beneficiario`
  ADD PRIMARY KEY (`id_tipo_beneficiario`);

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
  ADD KEY `id_usuario` (`id_usuario`),
  ADD KEY `fk_voluntario_rol` (`id_rol_actividad`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `actividad`
--
ALTER TABLE `actividad`
  MODIFY `id_actividad` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `actividades`
--
ALTER TABLE `actividades`
  MODIFY `id_actividad` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT de la tabla `actividad_lugar`
--
ALTER TABLE `actividad_lugar`
  MODIFY `id_actividad_lugar` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `actividad_recurso`
--
ALTER TABLE `actividad_recurso`
  MODIFY `id_actividad_recurso` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `asistencias`
--
ALTER TABLE `asistencias`
  MODIFY `id_asistencia` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `beneficiario`
--
ALTER TABLE `beneficiario`
  MODIFY `id_beneficiario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `beneficiario_old`
--
ALTER TABLE `beneficiario_old`
  MODIFY `id_beneficiario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `categoria_inventario`
--
ALTER TABLE `categoria_inventario`
  MODIFY `id_categoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `certificados`
--
ALTER TABLE `certificados`
  MODIFY `id_certificado` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `donacion`
--
ALTER TABLE `donacion`
  MODIFY `id_donacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=39;

--
-- AUTO_INCREMENT de la tabla `donacion_detalle`
--
ALTER TABLE `donacion_detalle`
  MODIFY `id_donacion_detalle` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `donacion_donante`
--
ALTER TABLE `donacion_donante`
  MODIFY `id_donacion_donante` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT de la tabla `donante`
--
ALTER TABLE `donante`
  MODIFY `id_donante` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `eventos_calendario`
--
ALTER TABLE `eventos_calendario`
  MODIFY `id_evento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `inventario_item`
--
ALTER TABLE `inventario_item`
  MODIFY `id_item` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `inventario_movimiento`
--
ALTER TABLE `inventario_movimiento`
  MODIFY `id_movimiento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `lugar`
--
ALTER TABLE `lugar`
  MODIFY `id_lugar` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `movimiento_financiero`
--
ALTER TABLE `movimiento_financiero`
  MODIFY `id_movimiento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=55;

--
-- AUTO_INCREMENT de la tabla `notificaciones`
--
ALTER TABLE `notificaciones`
  MODIFY `id_notificacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `participacion`
--
ALTER TABLE `participacion`
  MODIFY `id_participacion` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `permiso`
--
ALTER TABLE `permiso`
  MODIFY `id_permiso` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT de la tabla `programacion_donacion`
--
ALTER TABLE `programacion_donacion`
  MODIFY `id_programacion` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `recurso`
--
ALTER TABLE `recurso`
  MODIFY `id_recurso` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `responsable_beneficiario`
--
ALTER TABLE `responsable_beneficiario`
  MODIFY `id_responsable` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `rol_actividad`
--
ALTER TABLE `rol_actividad`
  MODIFY `id_rol_actividad` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT de la tabla `rol_permiso`
--
ALTER TABLE `rol_permiso`
  MODIFY `id_rol_permiso` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=40;

--
-- AUTO_INCREMENT de la tabla `rol_sistema`
--
ALTER TABLE `rol_sistema`
  MODIFY `id_rol_sistema` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `salida_donacion`
--
ALTER TABLE `salida_donacion`
  MODIFY `id_salida` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

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
-- AUTO_INCREMENT de la tabla `tipo_beneficiario`
--
ALTER TABLE `tipo_beneficiario`
  MODIFY `id_tipo_beneficiario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `tipo_donacion`
--
ALTER TABLE `tipo_donacion`
  MODIFY `id_tipo_donacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `usuario`
--
ALTER TABLE `usuario`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT de la tabla `usuario_permiso`
--
ALTER TABLE `usuario_permiso`
  MODIFY `id_usuario_permiso` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=81;

--
-- AUTO_INCREMENT de la tabla `usuario_rol`
--
ALTER TABLE `usuario_rol`
  MODIFY `id_usuario_rol` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT de la tabla `voluntario`
--
ALTER TABLE `voluntario`
  MODIFY `id_voluntario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `actividad`
--
ALTER TABLE `actividad`
  ADD CONSTRAINT `actividad_ibfk_1` FOREIGN KEY (`id_beneficiario`) REFERENCES `beneficiario_old` (`id_beneficiario`),
  ADD CONSTRAINT `actividad_ibfk_2` FOREIGN KEY (`id_usuario_creador`) REFERENCES `usuario` (`id_usuario`);

--
-- Filtros para la tabla `actividades`
--
ALTER TABLE `actividades`
  ADD CONSTRAINT `fk_actividad_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Filtros para la tabla `actividad_lugar`
--
ALTER TABLE `actividad_lugar`
  ADD CONSTRAINT `actividad_lugar_ibfk_1` FOREIGN KEY (`id_actividad`) REFERENCES `actividad` (`id_actividad`),
  ADD CONSTRAINT `actividad_lugar_ibfk_2` FOREIGN KEY (`id_lugar`) REFERENCES `lugar` (`id_lugar`);

--
-- Filtros para la tabla `actividad_recurso`
--
ALTER TABLE `actividad_recurso`
  ADD CONSTRAINT `actividad_recurso_ibfk_1` FOREIGN KEY (`id_actividad`) REFERENCES `actividad` (`id_actividad`),
  ADD CONSTRAINT `actividad_recurso_ibfk_2` FOREIGN KEY (`id_recurso`) REFERENCES `recurso` (`id_recurso`);

--
-- Filtros para la tabla `asistencias`
--
ALTER TABLE `asistencias`
  ADD CONSTRAINT `fk_asistencia_actividad` FOREIGN KEY (`id_actividad`) REFERENCES `actividades` (`id_actividad`),
  ADD CONSTRAINT `fk_asistencia_usuario` FOREIGN KEY (`id_usuario_registro`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_asistencia_voluntario` FOREIGN KEY (`id_voluntario`) REFERENCES `voluntario` (`id_voluntario`);

--
-- Filtros para la tabla `beneficiario_old`
--
ALTER TABLE `beneficiario_old`
  ADD CONSTRAINT `beneficiario_old_ibfk_1` FOREIGN KEY (`id_tipo_beneficiario`) REFERENCES `tipo_beneficiario` (`id_tipo_beneficiario`);

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
  ADD CONSTRAINT `participacion_ibfk_2` FOREIGN KEY (`id_actividad`) REFERENCES `actividad` (`id_actividad`),
  ADD CONSTRAINT `participacion_ibfk_3` FOREIGN KEY (`id_rol_actividad`) REFERENCES `rol_actividad` (`id_rol_actividad`);

--
-- Filtros para la tabla `programacion_donacion`
--
ALTER TABLE `programacion_donacion`
  ADD CONSTRAINT `programacion_donacion_ibfk_1` FOREIGN KEY (`id_donacion`) REFERENCES `donacion` (`id_donacion`),
  ADD CONSTRAINT `programacion_donacion_ibfk_2` FOREIGN KEY (`id_voluntario`) REFERENCES `voluntario` (`id_voluntario`),
  ADD CONSTRAINT `programacion_donacion_ibfk_3` FOREIGN KEY (`id_usuario_registro`) REFERENCES `usuario` (`id_usuario`),
  ADD CONSTRAINT `programacion_donacion_ibfk_4` FOREIGN KEY (`id_beneficiario`) REFERENCES `beneficiario_old` (`id_beneficiario`),
  ADD CONSTRAINT `programacion_donacion_ibfk_5` FOREIGN KEY (`id_lugar`) REFERENCES `lugar` (`id_lugar`);

--
-- Filtros para la tabla `responsable_beneficiario`
--
ALTER TABLE `responsable_beneficiario`
  ADD CONSTRAINT `responsable_beneficiario_ibfk_1` FOREIGN KEY (`id_beneficiario`) REFERENCES `beneficiario_old` (`id_beneficiario`);

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
  ADD CONSTRAINT `salida_inventario_ibfk_1` FOREIGN KEY (`id_actividad`) REFERENCES `actividad` (`id_actividad`),
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
