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
            for (Object[] row : rows) lista.add(mapearMovimiento(row));
            logger.info("Se listaron " + lista.size() + " movimientos financieros");
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar movimientos", e);
            return List.of();
        }
    }

    @Override
    public Map<String, Double> obtenerBalance() {
        try {
            Object[] row = (Object[]) em.createNativeQuery("CALL sp_obtenerBalance()")
                    .getSingleResult();
            Map<String, Double> balance = new HashMap<>();
            balance.put("ingresos", ((Number) row[0]).doubleValue());
            balance.put("gastos",   ((Number) row[1]).doubleValue());
            balance.put("saldo",    ((Number) row[2]).doubleValue());
            return balance;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener balance", e);
            Map<String, Double> vacio = new HashMap<>();
            vacio.put("ingresos", 0.0);
            vacio.put("gastos",   0.0);
            vacio.put("saldo",    0.0);
            return vacio;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MovimientoFinanciero> filtrar(String tipo, String categoria, String fechaInicio, String fechaFin) {
        try {
            String pTipo = tipo != null && !tipo.isEmpty() ? tipo : "";
            String pCategoria = categoria != null && !categoria.isEmpty() ? categoria : "";
            // Para fechas null, usamos fecha extrema para que el filtro no aplique
            java.util.Date pFechaIni = (fechaInicio != null && !fechaInicio.isEmpty())
                    ? java.sql.Date.valueOf(fechaInicio)
                    : java.sql.Date.valueOf("1900-01-01");
            java.util.Date pFechaFin = (fechaFin != null && !fechaFin.isEmpty())
                    ? java.sql.Date.valueOf(fechaFin)
                    : java.sql.Date.valueOf("2099-12-31");

            List<Object[]> rows = em.createNativeQuery("CALL sp_filtrarMovimientos(?1, ?2, ?3, ?4)")
                    .setParameter(1, pTipo)
                    .setParameter(2, pCategoria)
                    .setParameter(3, pFechaIni, jakarta.persistence.TemporalType.DATE)
                    .setParameter(4, pFechaFin, jakarta.persistence.TemporalType.DATE)
                    .getResultList();
            List<MovimientoFinanciero> lista = new ArrayList<>();
            for (Object[] row : rows) lista.add(mapearMovimiento(row));
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
                m.put("anio", row[0] != null ? ((Number) row[0]).intValue() : 0);
                m.put("mes", row[1] != null ? ((Number) row[1]).intValue() : 0);
                m.put("ingresos", row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
                m.put("gastos", row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
                resultado.add(m);
            }
            return resultado;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener resumen mensual", e);
            return List.of();
        }
    }

    // ── HELPER ──────────────────────────────────────────────

    // SP devuelve: id_movimiento[0], tipo[1], monto[2], descripcion[3], categoria[4],
    //   comprobante[5], fecha_movimiento[6], actividad[7], id_actividad[8],
    //   usuario_registro[9], creado_en[10]
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
