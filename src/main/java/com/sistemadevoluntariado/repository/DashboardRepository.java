package com.sistemadevoluntariado.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class DashboardRepository {

    private static final Logger logger = Logger.getLogger(DashboardRepository.class.getName());

    @PersistenceContext
    private EntityManager em;

    /**
     * Llama a sp_dashboard_estadisticas() que retorna una fila con 7 columnas:
     * total_voluntarios, voluntarios_activos, voluntarios_inactivos,
     * total_actividades, total_donaciones, monto_donaciones, total_beneficiarios
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_dashboard_estadisticas()")
                    .getResultList();
            if (!rows.isEmpty()) {
                Object[] r = rows.get(0);
                stats.put("totalVoluntarios",   r[0] != null ? ((Number) r[0]).intValue() : 0);
                stats.put("voluntariosActivos",  r[1] != null ? ((Number) r[1]).intValue() : 0);
                stats.put("voluntariosInactivos",r[2] != null ? ((Number) r[2]).intValue() : 0);
                stats.put("totalActividades",    r[3] != null ? ((Number) r[3]).intValue() : 0);
                stats.put("totalDonaciones",     r[4] != null ? ((Number) r[4]).intValue() : 0);
                stats.put("montoDonaciones",     r[5] != null ? ((Number) r[5]).doubleValue() : 0.0);
                stats.put("totalBeneficiarios",  r[6] != null ? ((Number) r[6]).intValue() : 0);
                return stats;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sp_dashboard_estadisticas", e);
        }
        stats.put("totalVoluntarios", 0);
        stats.put("voluntariosActivos", 0);
        stats.put("voluntariosInactivos", 0);
        stats.put("totalActividades", 0);
        stats.put("totalDonaciones", 0);
        stats.put("montoDonaciones", 0.0);
        stats.put("totalBeneficiarios", 0);
        return stats;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerActividadesPorMes() {
        Map<String, Object> resultado = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_dashboard_actividades_por_mes()")
                    .getResultList();
            for (Object[] row : rows) {
                labels.add((String) row[0]);
                data.add(row[1] != null ? ((Number) row[1]).intValue() : 0);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sp_dashboard_actividades_por_mes", e);
        }
        resultado.put("labels", labels);
        resultado.put("data", data);
        return resultado;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerHorasVoluntariasPorActividad() {
        Map<String, Object> resultado = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_dashboard_horas_por_actividad()")
                    .getResultList();
            for (Object[] row : rows) {
                labels.add((String) row[0]);
                data.add(row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sp_dashboard_horas_por_actividad", e);
        }
        resultado.put("labels", labels);
        resultado.put("data", data);
        return resultado;
    }

    public double obtenerTotalHorasVoluntarias() {
        try {
            Object result = em.createNativeQuery("CALL sp_dashboard_total_horas()")
                    .getSingleResult();
            return result != null ? ((Number) result).doubleValue() : 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sp_dashboard_total_horas", e);
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> obtenerProximaActividad() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_dashboard_proxima_actividad()")
                    .getResultList();
            if (!rows.isEmpty()) {
                Object[] row = rows.get(0);
                Map<String, String> actividad = new LinkedHashMap<>();
                actividad.put("nombre",    row[0] != null ? row[0].toString() : "");
                actividad.put("fecha",     row[1] != null ? row[1].toString() : "");
                actividad.put("ubicacion", row[2] != null ? row[2].toString() : "");
                return actividad;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sp_dashboard_proxima_actividad", e);
        }
        return null;
    }
}
