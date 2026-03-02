package com.sistemadevoluntariado.service;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.MovimientoFinanciero;
import com.sistemadevoluntariado.entity.SalidaDonacion;
import com.sistemadevoluntariado.repository.SalidaDonacionRepository;
import com.sistemadevoluntariado.repository.TesoreriaRepositoryCustom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class SalidaDonacionService {

    private static final Logger logger = Logger.getLogger(SalidaDonacionService.class.getName());

    @Autowired
    private SalidaDonacionRepository salidaDonacionRepository;

    @Autowired
    private TesoreriaRepositoryCustom tesoreriaRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public List<SalidaDonacion> listarTodos() {
        return salidaDonacionRepository.listar();
    }

    @Transactional
    public SalidaDonacion obtenerPorId(int id) {
        return salidaDonacionRepository.obtenerPorId(id);
    }

    @Transactional
    public boolean guardar(SalidaDonacion s) {
        try {
            return salidaDonacionRepository.guardar(s);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar salida de donacion", e);
            return false;
        }
    }

    @Transactional
    public boolean actualizar(SalidaDonacion s) {
        try {
            return salidaDonacionRepository.actualizar(s);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar salida de donacion", e);
            return false;
        }
    }

    @Transactional
    public boolean anular(int id, int idUsuario, String motivo) {
        try {
            // Obtener la salida antes de anular para eliminar el gasto en tesorería
            SalidaDonacion salida = salidaDonacionRepository.obtenerPorId(id);
            boolean ok = salidaDonacionRepository.anular(id, idUsuario, motivo);
            if (ok && salida != null && "CONFIRMADO".equalsIgnoreCase(salida.getEstado())) {
                eliminarGastoTesoreria(id);
            }
            return ok;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al anular salida de donacion", e);
            return false;
        }
    }

    /**
     * Cambia el estado de una salida. Retorna null si éxito, o un mensaje de error.
     * Cuando el estado es CONFIRMADO, genera automáticamente el número de boleta.
     */
    @Transactional
    public String cambiarEstado(int id, String estado) {
        try {
            // Validar saldo antes de confirmar una salida tipo DINERO
            if ("CONFIRMADO".equalsIgnoreCase(estado)) {
                SalidaDonacion salida = salidaDonacionRepository.obtenerPorId(id);
                if (salida != null && "DINERO".equalsIgnoreCase(salida.getTipoSalida())) {
                    Map<String, Double> balance = tesoreriaRepository.obtenerBalance();
                    double saldo = balance.getOrDefault("saldo", 0.0);
                    if (saldo < salida.getCantidad()) {
                        return "Saldo insuficiente en tesorería. Saldo actual: S/ "
                                + String.format("%.2f", saldo)
                                + ", monto de salida: S/ "
                                + String.format("%.2f", salida.getCantidad());
                    }
                }
            }

            boolean ok = salidaDonacionRepository.cambiarEstado(id, estado);
            if (!ok) return "No se pudo actualizar el estado";

            if ("CONFIRMADO".equalsIgnoreCase(estado)) {
                // Generar número de boleta automático
                try {
                    Number maxNum = (Number) em.createNativeQuery(
                            "SELECT COALESCE(MAX(CAST(SUBSTRING(comprobante, 6) AS UNSIGNED)), 0) " +
                            "FROM salida_donacion WHERE comprobante LIKE 'B001-%'")
                            .getSingleResult();
                    long siguiente = maxNum.longValue() + 1;
                    String boleta = String.format("B001-%08d", siguiente);
                    em.createNativeQuery(
                            "UPDATE salida_donacion SET comprobante = ?1 WHERE id_salida = ?2")
                            .setParameter(1, boleta)
                            .setParameter(2, id)
                            .executeUpdate();
                    // Actualizar también el comprobante en tesorería si ya existe el movimiento
                    em.createNativeQuery(
                            "UPDATE movimiento_financiero SET comprobante = ?1 " +
                            "WHERE categoria = 'Salidas de Donaciones' " +
                            "AND descripcion LIKE ?2")
                            .setParameter(1, boleta)
                            .setParameter(2, "Salida de Donación #" + id + "%")
                            .executeUpdate();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "No se pudo generar boleta para salida #" + id, e);
                }

                SalidaDonacion salida = salidaDonacionRepository.obtenerPorId(id);
                if (salida != null && "DINERO".equalsIgnoreCase(salida.getTipoSalida())) {
                    registrarGastoTesoreria(salida);
                }
            }
            return null; // éxito
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cambiar estado de salida", e);
            return "Error interno al cambiar estado";
        }
    }

    /**
     * Registra un GASTO en tesorería cuando se confirma una salida de donación tipo DINERO.
     * Usa SQL directo para evitar problemas con stored procedures en transacciones Hibernate.
     */
    @SuppressWarnings("unchecked")
    private void registrarGastoTesoreria(SalidaDonacion salida) {
        try {
            String descripcionBase = "Salida de Donación #" + salida.getIdSalida();
            String descripcion = descripcionBase
                    + " (Donación #" + salida.getIdDonacion() + ")"
                    + (salida.getDescripcion() != null && !salida.getDescripcion().isEmpty()
                    ? ": " + salida.getDescripcion() : "");

            // Verificar que no exista ya un movimiento para esta salida usando SQL directo
            List<?> existentes = em.createNativeQuery(
                    "SELECT id_movimiento FROM movimiento_financiero " +
                    "WHERE tipo = 'GASTO' AND categoria = 'Salidas de Donaciones' " +
                    "AND descripcion LIKE ?1 LIMIT 1")
                    .setParameter(1, descripcionBase + "%")
                    .getResultList();
            if (!existentes.isEmpty()) {
                logger.info("Ya existe movimiento en tesorería para salida #" + salida.getIdSalida());
                return;
            }

            // Insertar gasto directamente sin SP para evitar conflictos de transacción
            em.createNativeQuery(
                    "INSERT INTO movimiento_financiero " +
                    "(tipo, monto, descripcion, categoria, comprobante, fecha_movimiento, id_actividad, id_usuario) " +
                    "VALUES (?1, ?2, ?3, ?4, ?5, ?6, NULLIF(?7, 0), ?8)")
                    .setParameter(1, "GASTO")
                    .setParameter(2, salida.getCantidad())
                    .setParameter(3, descripcion)
                    .setParameter(4, "Salidas de Donaciones")
                    .setParameter(5, salida.getComprobante())
                    .setParameter(6, java.time.LocalDate.now())
                    .setParameter(7, salida.getIdActividad())
                    .setParameter(8, salida.getIdUsuarioRegistro())
                    .executeUpdate();
            logger.info("✓ Gasto registrado en tesorería para salida #" + salida.getIdSalida());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al registrar gasto en tesorería para salida #" + salida.getIdSalida(), e);
        }
    }

    /**
     * Elimina el GASTO de tesorería cuando se anula una salida de donación.
     */
    private void eliminarGastoTesoreria(int idSalida) {
        try {
            List<MovimientoFinanciero> movimientos = tesoreriaRepository.filtrar(
                    "GASTO", "Salidas de Donaciones", null, null, "Salida de Donación #" + idSalida);
            for (MovimientoFinanciero mv : movimientos) {
                em.createNativeQuery("DELETE FROM movimiento_financiero WHERE id_movimiento = ?1")
                        .setParameter(1, mv.getIdMovimiento())
                        .executeUpdate();
                logger.info("✓ Gasto eliminado de tesorería (mov #" + mv.getIdMovimiento() + ") por anulación de salida #" + idSalida);
            }
            em.clear();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al eliminar gasto de tesorería para salida #" + idSalida, e);
        }
    }

    @Transactional
    public List<Map<String, Object>> listarDonacionesDisponibles() {
        return salidaDonacionRepository.listarDonacionesDisponibles();
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public List<Map<String, Object>> buscarDonacionesDisponibles(String query) {
        return salidaDonacionRepository.buscarDonacionesDisponibles(query);
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public Map<String, Object> obtenerSaldoDisponible(int idDonacion) {
        return salidaDonacionRepository.obtenerSaldoDisponible(idDonacion);
    }
}
