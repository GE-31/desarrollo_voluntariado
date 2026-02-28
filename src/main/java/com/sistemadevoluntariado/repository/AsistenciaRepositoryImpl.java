package com.sistemadevoluntariado.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sistemadevoluntariado.entity.Asistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;

public class AsistenciaRepositoryImpl implements AsistenciaRepositoryCustom {

    private static final Logger logger = Logger.getLogger(AsistenciaRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean registrarAsistencia(Asistencia asistencia) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_registrar_asistencia");
            spq.registerStoredProcedureParameter("p_id_voluntario",       Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_id_actividad",        Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_fecha",               Date.class,    ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_hora_entrada",        Time.class,    ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_hora_salida",         Time.class,    ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_estado",              String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_observaciones",       String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_id_usuario_registro", Integer.class, ParameterMode.IN);
            spq.setParameter("p_id_voluntario",       asistencia.getIdVoluntario());
            spq.setParameter("p_id_actividad",        asistencia.getIdActividad());
            spq.setParameter("p_fecha",               asistencia.getFecha() != null ? Date.valueOf(asistencia.getFecha()) : null);
            spq.setParameter("p_hora_entrada",        parseTime(asistencia.getHoraEntrada()));
            spq.setParameter("p_hora_salida",         parseTime(asistencia.getHoraSalida()));
            spq.setParameter("p_estado",              asistencia.getEstado());
            spq.setParameter("p_observaciones",       asistencia.getObservaciones());
            spq.setParameter("p_id_usuario_registro", asistencia.getIdUsuarioRegistro());
            spq.execute();
            logger.info("✓ Asistencia registrada correctamente");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al registrar asistencia: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Asistencia> listarAsistencias() {
        List<Asistencia> lista = new ArrayList<>();
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listar_asistencias()").getResultList();
            for (Object[] row : rows) {
                lista.add(mapRowFull(row));
            }
            logger.info("✓ Se obtuvieron " + lista.size() + " asistencias");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al listar asistencias", e);
        }
        return lista;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Asistencia obtenerPorId(int idAsistencia) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_obtener_asistencia_por_id(:id)")
                    .setParameter("id", idAsistencia)
                    .getResultList();
            return rows.isEmpty() ? null : mapRowFull(rows.get(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al obtener asistencia por ID", e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Asistencia> listarPorActividad(int idActividad) {
        List<Asistencia> lista = new ArrayList<>();
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listar_asistencias_por_actividad(:id)")
                    .setParameter("id", idActividad)
                    .getResultList();
            for (Object[] row : rows) {
                Asistencia a = new Asistencia();
                a.setIdAsistencia(row[0] != null ? ((Number) row[0]).intValue() : 0);
                a.setIdVoluntario(row[1] != null ? ((Number) row[1]).intValue() : 0);
                a.setNombreVoluntario(row[2] != null ? row[2].toString() : "");
                a.setDniVoluntario(row[3] != null ? row[3].toString() : "");
                a.setIdActividad(row[4] != null ? ((Number) row[4]).intValue() : 0);
                a.setNombreActividad(row[5] != null ? row[5].toString() : "");
                a.setFecha(row[6] != null ? row[6].toString() : null);
                a.setHoraEntrada(row[7] != null ? row[7].toString() : null);
                a.setHoraSalida(row[8] != null ? row[8].toString() : null);
                a.setHorasTotales(row[9] != null ? new BigDecimal(row[9].toString()) : null);
                a.setEstado(row[10] != null ? row[10].toString() : null);
                a.setObservaciones(row[11] != null ? row[11].toString() : null);
                a.setCreadoEn(row[12] != null ? row[12].toString() : null);
                lista.add(a);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al listar asistencias por actividad", e);
        }
        return lista;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Asistencia> listarPorVoluntario(int idVoluntario) {
        List<Asistencia> lista = new ArrayList<>();
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listar_asistencias_por_voluntario(:id)")
                    .setParameter("id", idVoluntario)
                    .getResultList();
            for (Object[] row : rows) {
                Asistencia a = new Asistencia();
                a.setIdAsistencia(row[0] != null ? ((Number) row[0]).intValue() : 0);
                a.setIdVoluntario(row[1] != null ? ((Number) row[1]).intValue() : 0);
                a.setNombreVoluntario(row[2] != null ? row[2].toString() : "");
                a.setIdActividad(row[3] != null ? ((Number) row[3]).intValue() : 0);
                a.setNombreActividad(row[4] != null ? row[4].toString() : "");
                a.setFecha(row[5] != null ? row[5].toString() : null);
                a.setHoraEntrada(row[6] != null ? row[6].toString() : null);
                a.setHoraSalida(row[7] != null ? row[7].toString() : null);
                a.setHorasTotales(row[8] != null ? new BigDecimal(row[8].toString()) : null);
                a.setEstado(row[9] != null ? row[9].toString() : null);
                a.setObservaciones(row[10] != null ? row[10].toString() : null);
                a.setCreadoEn(row[11] != null ? row[11].toString() : null);
                lista.add(a);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al listar asistencias por voluntario", e);
        }
        return lista;
    }

    @Override
    public boolean actualizarAsistencia(Asistencia asistencia) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_actualizar_asistencia");
            spq.registerStoredProcedureParameter("p_id_asistencia",  Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_hora_entrada",   Time.class,    ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_hora_salida",    Time.class,    ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_estado",         String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_observaciones",  String.class,  ParameterMode.IN);
            spq.setParameter("p_id_asistencia",  asistencia.getIdAsistencia());
            spq.setParameter("p_hora_entrada",   parseTime(asistencia.getHoraEntrada()));
            spq.setParameter("p_hora_salida",    parseTime(asistencia.getHoraSalida()));
            spq.setParameter("p_estado",         asistencia.getEstado());
            spq.setParameter("p_observaciones",  asistencia.getObservaciones());
            spq.execute();
            logger.info("✓ Asistencia actualizada correctamente ID: " + asistencia.getIdAsistencia());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al actualizar asistencia", e);
            return false;
        }
    }

    @Override
    public boolean eliminarAsistencia(int idAsistencia) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_eliminar_asistencia");
            spq.registerStoredProcedureParameter("p_id_asistencia", Integer.class, ParameterMode.IN);
            spq.setParameter("p_id_asistencia", idAsistencia);
            spq.execute();
            logger.info("✓ Asistencia eliminada correctamente ID: " + idAsistencia);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al eliminar asistencia", e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public BigDecimal obtenerHorasVoluntarioActividad(int idVoluntario, int idActividad) {
        try {
            List<Object> results = em.createNativeQuery(
                "SELECT COALESCE(SUM(horas_totales), 0) FROM asistencias " +
                "WHERE id_voluntario = ?1 AND id_actividad = ?2 AND estado IN ('ASISTIO','TARDANZA')")
                .setParameter(1, idVoluntario)
                .setParameter(2, idActividad)
                .getResultList();
            if (!results.isEmpty() && results.get(0) != null) {
                return new BigDecimal(results.get(0).toString());
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al obtener horas voluntario-actividad", e);
            return BigDecimal.ZERO;
        }
    }

    // ── HELPERS ─────────────────────────────────────────────

    private Asistencia mapRowFull(Object[] row) {
        Asistencia a = new Asistencia();
        a.setIdAsistencia(row[0] != null ? ((Number) row[0]).intValue() : 0);
        a.setIdVoluntario(row[1] != null ? ((Number) row[1]).intValue() : 0);
        a.setNombreVoluntario(row[2] != null ? row[2].toString() : "");
        a.setDniVoluntario(row[3] != null ? row[3].toString() : "");
        a.setIdActividad(row[4] != null ? ((Number) row[4]).intValue() : 0);
        a.setNombreActividad(row[5] != null ? row[5].toString() : "");
        a.setFecha(row[6] != null ? row[6].toString() : null);
        a.setHoraEntrada(row[7] != null ? row[7].toString() : null);
        a.setHoraSalida(row[8] != null ? row[8].toString() : null);
        a.setHorasTotales(row[9] != null ? new BigDecimal(row[9].toString()) : null);
        a.setEstado(row[10] != null ? row[10].toString() : null);
        a.setObservaciones(row[11] != null ? row[11].toString() : null);
        a.setIdUsuarioRegistro(row[12] != null ? ((Number) row[12]).intValue() : 0);
        a.setUsuarioRegistro(row[13] != null ? row[13].toString() : null);
        a.setCreadoEn(row[14] != null ? row[14].toString() : null);
        return a;
    }

    private Time parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return null;
        String t = timeStr.trim();
        if (t.matches("\\d{2}:\\d{2}")) {
            t = t + ":00";
        }
        return Time.valueOf(t);
    }
}
