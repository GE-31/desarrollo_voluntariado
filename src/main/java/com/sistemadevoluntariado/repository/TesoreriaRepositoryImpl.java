package com.sistemadevoluntariado.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sistemadevoluntariado.entity.MovimientoFinanciero;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class TesoreriaRepositoryImpl implements TesoreriaRepositoryCustom {

    private static final Logger logger = Logger.getLogger(TesoreriaRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<MovimientoFinanciero> listarConJoins() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listarMovimientos()")
                    .getResultList();
            List<MovimientoFinanciero> lista = new ArrayList<>();
            for (Object[] row : rows)
                lista.add(mapearMovimiento(row));
            logger.info("Se listaron " + lista.size() + " movimientos financieros");
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar movimientos", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Double> obtenerBalance() {
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT " +
                    "IFNULL(SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE 0 END), 0) AS total_ingresos, " +
                    "IFNULL(SUM(CASE WHEN tipo = 'GASTO'   THEN monto ELSE 0 END), 0) AS total_gastos, " +
                    "IFNULL(SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE -monto END), 0) AS saldo " +
                    "FROM movimiento_financiero")
                    .getResultList();
            Map<String, Double> balance = new HashMap<>();
            if (!rows.isEmpty()) {
                Object[] row = rows.get(0);
                balance.put("ingresos", row[0] != null ? ((Number) row[0]).doubleValue() : 0.0);
                balance.put("gastos",   row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
                balance.put("saldo",    row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
            } else {
                balance.put("ingresos", 0.0);
                balance.put("gastos", 0.0);
                balance.put("saldo", 0.0);
            }
            return balance;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener balance", e);
            Map<String, Double> vacio = new HashMap<>();
            vacio.put("ingresos", 0.0);
            vacio.put("gastos", 0.0);
            vacio.put("saldo", 0.0);
            return vacio;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MovimientoFinanciero> filtrar(String tipo, String categoria, String fechaInicio, String fechaFin, String busqueda) {
        try {
            String pTipo = tipo != null && !tipo.trim().isEmpty() ? tipo : null;
            String pCategoria = categoria != null && !categoria.trim().isEmpty() ? categoria : null;
            String pBusqueda = busqueda != null && !busqueda.trim().isEmpty() ? busqueda.trim() : null;

            java.sql.Date pFechaIni = (fechaInicio != null && !fechaInicio.trim().isEmpty())
                    ? java.sql.Date.valueOf(fechaInicio)
                    : null;
            java.sql.Date pFechaFin = (fechaFin != null && !fechaFin.trim().isEmpty())
                    ? java.sql.Date.valueOf(fechaFin)
                    : null;

            List<Object[]> rows = em.createNativeQuery("CALL sp_filtrarMovimientos(?1, ?2, ?3, ?4, ?5)")
                    .setParameter(1, pTipo)
                    .setParameter(2, pCategoria)
                    .setParameter(3, pFechaIni, jakarta.persistence.TemporalType.DATE)
                    .setParameter(4, pFechaFin, jakarta.persistence.TemporalType.DATE)
                    .setParameter(5, pBusqueda)
                    .getResultList();
            List<MovimientoFinanciero> lista = new ArrayList<>();
            for (Object[] row : rows)
                lista.add(mapearMovimiento(row));
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al filtrar movimientos", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> resumenPorCategoria() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_resumenPorCategoria()")
                    .getResultList();
            List<Map<String, Object>> resultado = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("categoria", row[0]);
                m.put("tipo", row[1]);
                m.put("total", row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
                m.put("cantidad", row[3] != null ? ((Number) row[3]).intValue() : 0);
                resultado.add(m);
            }
            return resultado;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener resumen por categoria", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> resumenMensual() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_resumenMensual()")
                    .getResultList();
            List<Map<String, Object>> resultado = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("mes", row[0] != null ? row[0].toString() : "");
                m.put("ingresos", row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
                m.put("gastos", row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
                resultado.add(m);
            }
            return resultado;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener resumen mensual", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> donacionesPorCampana() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_donacionesPorCampana()")
                    .getResultList();
            List<Map<String, Object>> resultado = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("campana", row[0] != null ? row[0].toString() : "Sin actividad");
                m.put("montoConfirmado", row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
                m.put("montoPendiente", row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
                m.put("totalDonaciones", row[3] != null ? ((Number) row[3]).intValue() : 0);
                resultado.add(m);
            }
            return resultado;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener donaciones por campaña", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> donacionesDisponibles() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_donaciones_disponibles_tesoreria(NULL)")
                    .getResultList();
            List<Map<String, Object>> resultado = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("idDonacion", row[0] != null ? ((Number) row[0]).intValue() : 0);
                m.put("montoOriginal", row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
                m.put("donante", row[2] != null ? row[2].toString() : "ANÓNIMO");
                m.put("dni", row[3] != null ? row[3].toString() : null);
                m.put("ruc", row[4] != null ? row[4].toString() : null);
                m.put("actividadOrigen", row[5] != null ? row[5].toString() : null);
                m.put("saldoDisponible", row[6] != null ? ((Number) row[6]).doubleValue() : 0.0);
                resultado.add(m);
            }
            return resultado;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar donaciones disponibles", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerSaldoDonacion(int idDonacion) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_obtener_saldo_donacion(?1)")
                    .setParameter(1, idDonacion)
                    .getResultList();
            if (!rows.isEmpty()) {
                Object[] row = rows.get(0);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("idDonacion", ((Number) row[0]).intValue());
                m.put("montoOriginal", ((Number) row[1]).doubleValue());
                m.put("donante", row[2] != null ? row[2].toString() : "ANÓNIMO");
                m.put("saldoDisponible", row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
                return m;
            }
            return Map.of();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener saldo donación #" + idDonacion, e);
            return Map.of();
        }
    }

    @Override
    public void registrarGastoDonacion(int idMovimiento, int idDonacion, double monto) {
        try {
            em.createNativeQuery("INSERT INTO gasto_donacion (id_movimiento, id_donacion, monto) VALUES (?1, ?2, ?3)")
                    .setParameter(1, idMovimiento)
                    .setParameter(2, idDonacion)
                    .setParameter(3, monto)
                    .executeUpdate();
            logger.info("✓ Gasto-Donación registrado: mov #" + idMovimiento + " → donación #" + idDonacion + " por S/ " + monto);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al registrar gasto_donacion", e);
        }
    }

    @Override
    public void eliminarGastoDonacion(int idMovimiento) {
        try {
            em.createNativeQuery("DELETE FROM gasto_donacion WHERE id_movimiento = ?1")
                    .setParameter(1, idMovimiento)
                    .executeUpdate();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al eliminar gasto_donacion", e);
        }
    }

    // ── HELPER ──────────────────────────────────────────────

    // SP devuelve: id_movimiento[0], tipo[1], monto[2], descripcion[3],
    // categoria[4],
    // comprobante[5], fecha_movimiento[6], actividad[7], id_actividad[8],
    // usuario_registro[9], creado_en[10]
    private MovimientoFinanciero mapearMovimiento(Object[] row) {
        MovimientoFinanciero mf = new MovimientoFinanciero();
        mf.setIdMovimiento(row[0] != null ? ((Number) row[0]).intValue() : 0);
        mf.setTipo((String) row[1]);
        mf.setMonto(row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
        mf.setDescripcion((String) row[3]);
        mf.setCategoria((String) row[4]);
        mf.setComprobante((String) row[5]);
        mf.setFechaMovimiento(row[6] != null ? row[6].toString() : null);
        mf.setActividad((String) row[7]);
        mf.setIdActividad(row[8] != null ? ((Number) row[8]).intValue() : 0);
        mf.setUsuarioRegistro((String) row[9]);
        mf.setCreadoEn(row[10] != null ? row[10].toString() : null);
        return mf;
    }
}
