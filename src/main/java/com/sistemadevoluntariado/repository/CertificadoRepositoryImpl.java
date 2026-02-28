package com.sistemadevoluntariado.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sistemadevoluntariado.entity.Certificado;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;

public class CertificadoRepositoryImpl implements CertificadoRepositoryCustom {

    private static final Logger logger = Logger.getLogger(CertificadoRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean crearCertificado(Certificado c) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_crear_certificado");
            spq.registerStoredProcedureParameter("p_codigo_certificado", String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_id_voluntario",      Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_id_actividad",       Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_horas_voluntariado", Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_observaciones",      String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_id_usuario_emite",   Integer.class, ParameterMode.IN);
            spq.setParameter("p_codigo_certificado", c.getCodigoCertificado());
            spq.setParameter("p_id_voluntario",      c.getIdVoluntario());
            spq.setParameter("p_id_actividad",       c.getIdActividad());
            spq.setParameter("p_horas_voluntariado", c.getHorasVoluntariado());
            spq.setParameter("p_observaciones",      c.getObservaciones());
            spq.setParameter("p_id_usuario_emite",   c.getIdUsuarioEmite());
            spq.execute();
            logger.info("✓ Certificado creado correctamente");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al crear certificado: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Certificado> obtenerTodosCertificados() {
        List<Certificado> lista = new ArrayList<>();
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listar_certificados()").getResultList();
            for (Object[] row : rows) {
                lista.add(mapRow(row));
            }
            logger.info("✓ Se obtuvieron " + lista.size() + " certificados");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al obtener certificados", e);
        }
        return lista;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Certificado obtenerCertificadoPorId(int id) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_obtener_certificado_por_id(:id)")
                    .setParameter("id", id)
                    .getResultList();
            return rows.isEmpty() ? null : mapRow(rows.get(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al obtener certificado por ID", e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Certificado obtenerCertificadoPorCodigo(String codigo) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_obtener_certificado_por_codigo(:codigo)")
                    .setParameter("codigo", codigo)
                    .getResultList();
            return rows.isEmpty() ? null : mapRow(rows.get(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al obtener certificado por código", e);
            return null;
        }
    }

    @Override
    public boolean anularCertificado(int id, String motivo) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_anular_certificado");
            spq.registerStoredProcedureParameter("p_id_certificado",   Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_motivo_anulacion", String.class,  ParameterMode.IN);
            spq.setParameter("p_id_certificado",   id);
            spq.setParameter("p_motivo_anulacion", motivo);
            spq.execute();
            logger.info("✓ Certificado anulado correctamente ID: " + id);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al anular certificado", e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Certificado> obtenerCertificadosPorVoluntario(int idVoluntario) {
        List<Certificado> lista = new ArrayList<>();
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_certificados_por_voluntario(:id)")
                    .setParameter("id", idVoluntario)
                    .getResultList();
            for (Object[] row : rows) {
                lista.add(mapRow(row));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al obtener certificados por voluntario", e);
        }
        return lista;
    }

    private Certificado mapRow(Object[] row) {
        Certificado c = new Certificado();
        c.setIdCertificado(row[0] != null ? ((Number) row[0]).intValue() : 0);
        c.setCodigoCertificado(row[1] != null ? row[1].toString() : null);
        c.setIdVoluntario(row[2] != null ? ((Number) row[2]).intValue() : 0);
        c.setIdActividad(row[3] != null ? ((Number) row[3]).intValue() : 0);
        c.setHorasVoluntariado(row[4] != null ? ((Number) row[4]).intValue() : 0);
        c.setFechaEmision(row[5] != null ? row[5].toString() : null);
        c.setEstado(row[6] != null ? row[6].toString() : null);
        c.setObservaciones(row[7] != null ? row[7].toString() : null);
        c.setIdUsuarioEmite(row[8] != null ? ((Number) row[8]).intValue() : 0);
        c.setNombreVoluntario(row[9] != null ? row[9].toString() : "");
        c.setDniVoluntario(row[10] != null ? row[10].toString() : "");
        c.setNombreActividad(row[11] != null ? row[11].toString() : "");
        c.setUsuarioEmite(row[12] != null ? row[12].toString() : "");
        return c;
    }
}
