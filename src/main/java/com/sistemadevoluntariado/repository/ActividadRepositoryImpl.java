package com.sistemadevoluntariado.repository;

import java.sql.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sistemadevoluntariado.entity.Actividad;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;

public class ActividadRepositoryImpl implements ActividadRepositoryCustom {

    private static final Logger logger = Logger.getLogger(ActividadRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean crearActividad(Actividad actividad) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_crear_actividad");
            spq.registerStoredProcedureParameter("p_nombre",       String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_descripcion",  String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_fecha_inicio", Date.class,    ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_fecha_fin",    Date.class,    ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_ubicacion",    String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_cupo_maximo",  Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_id_usuario",   Integer.class, ParameterMode.IN);
            spq.setParameter("p_nombre",       actividad.getNombre());
            spq.setParameter("p_descripcion",  actividad.getDescripcion());
            spq.setParameter("p_fecha_inicio", actividad.getFechaInicio() != null ? Date.valueOf(actividad.getFechaInicio()) : null);
            spq.setParameter("p_fecha_fin",    actividad.getFechaFin()    != null ? Date.valueOf(actividad.getFechaFin())    : null);
            spq.setParameter("p_ubicacion",    actividad.getUbicacion());
            spq.setParameter("p_cupo_maximo",  actividad.getCupoMaximo());
            spq.setParameter("p_id_usuario",   actividad.getIdUsuario());
            spq.execute();
            logger.info("✓ Actividad creada correctamente: " + actividad.getNombre());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al crear actividad: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Actividad> obtenerTodasActividades() {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_obtener_todas_actividades", Actividad.class);
            // Hibernate 7.x: getResultList() ejecuta internamente, NO llamar execute() antes
            List<Actividad> actividades = spq.getResultList();
            logger.info("✓ Se obtuvieron " + actividades.size() + " actividades");
            return actividades;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al obtener actividades", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Actividad obtenerActividadPorId(int idActividad) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_obtener_actividad_por_id", Actividad.class);
            spq.registerStoredProcedureParameter("p_id", Integer.class, ParameterMode.IN);
            spq.setParameter("p_id", idActividad);
            List<?> result = spq.getResultList();
            if (!result.isEmpty()) {
                logger.info("✓ Actividad obtenida con ID: " + idActividad);
                return (Actividad) result.get(0);
            }
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al obtener actividad", e);
            return null;
        }
    }

    @Override
    public boolean actualizarActividad(Actividad actividad) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_actualizar_actividad");
            spq.registerStoredProcedureParameter("p_id",           Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_nombre",       String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_descripcion",  String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_fecha_inicio", Date.class,    ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_fecha_fin",    Date.class,    ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_ubicacion",    String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_cupo_maximo",  Integer.class, ParameterMode.IN);
            spq.setParameter("p_id",           actividad.getIdActividad());
            spq.setParameter("p_nombre",       actividad.getNombre());
            spq.setParameter("p_descripcion",  actividad.getDescripcion());
            spq.setParameter("p_fecha_inicio", actividad.getFechaInicio() != null ? Date.valueOf(actividad.getFechaInicio()) : null);
            spq.setParameter("p_fecha_fin",    actividad.getFechaFin()    != null ? Date.valueOf(actividad.getFechaFin())    : null);
            spq.setParameter("p_ubicacion",    actividad.getUbicacion());
            spq.setParameter("p_cupo_maximo",  actividad.getCupoMaximo());
            spq.execute();
            logger.info("✓ Actividad actualizada correctamente: " + actividad.getNombre());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al actualizar actividad", e);
            return false;
        }
    }

    @Override
    public boolean cambiarEstado(int idActividad, String nuevoEstado) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_cambiar_estado_actividad");
            spq.registerStoredProcedureParameter("p_id",     Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_estado", String.class,  ParameterMode.IN);
            spq.setParameter("p_id",     idActividad);
            spq.setParameter("p_estado", nuevoEstado);
            spq.execute();
            logger.info("✓ Estado de actividad actualizado a: " + nuevoEstado);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al cambiar estado de actividad", e);
            return false;
        }
    }

    @Override
    public boolean eliminarActividad(int idActividad) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_eliminar_actividad");
            spq.registerStoredProcedureParameter("p_id", Integer.class, ParameterMode.IN);
            spq.setParameter("p_id", idActividad);
            spq.execute();
            logger.info("✓ Actividad eliminada correctamente");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al eliminar actividad", e);
            return false;
        }
    }

    @Override
    public boolean tieneCupoDisponible(int idActividad) {
        try {
            Object[] result = (Object[]) em.createNativeQuery(
                "SELECT a.cupo_maximo, " +
                "(SELECT COUNT(DISTINCT asi.id_voluntario) FROM asistencias asi " +
                " WHERE asi.id_actividad = a.id_actividad AND asi.estado IN ('ASISTIO','TARDANZA')) AS inscritos_actuales " +
                "FROM actividades a WHERE a.id_actividad = ?1")
                .setParameter(1, idActividad)
                .getSingleResult();
            int cupoMaximo = ((Number) result[0]).intValue();
            int inscritosActuales = ((Number) result[1]).intValue();
            return inscritosActuales < cupoMaximo;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al verificar cupo de actividad", e);
            return true;
        }
    }

    @Override
    public boolean actualizarInscritosPorAsistencia(int idActividad) {
        try {
            em.createNativeQuery(
                "UPDATE actividades SET inscritos = (" +
                "  SELECT COUNT(DISTINCT id_voluntario) FROM asistencias " +
                "  WHERE id_actividad = ?1 AND estado IN ('ASISTIO','TARDANZA')" +
                ") WHERE id_actividad = ?1")
                .setParameter(1, idActividad)
                .executeUpdate();
            logger.info("✓ Inscritos actualizados para actividad ID: " + idActividad);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al actualizar inscritos de actividad", e);
            return false;
        }
    }

    @Override
    public boolean actualizarInscritosPorParticipacion(int idActividad) {
        try {
            em.createNativeQuery(
                "UPDATE actividades SET inscritos = (" +
                "  SELECT COUNT(*) FROM participacion " +
                "  WHERE id_actividad = ?1" +
                ") WHERE id_actividad = ?1")
                .setParameter(1, idActividad)
                .executeUpdate();
            logger.info("✓ Inscritos (participación) actualizados para actividad ID: " + idActividad);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al actualizar inscritos por participación", e);
            return false;
        }
    }
}

// LLAMA A PROCEDIMIENTOS ALMACENADOS PARA CREAR, OBTENER, 
// ACTUALIZAR Y ELIMINAR ACTIVIDADES, ASÍ COMO VERIFICAR CUPO 
// DISPONIBLE Y ACTUALIZAR INSCRITOS POR ASISTENCIA. INCLUYE MANEJO DE
//  EXCEPCIONES Y LOGGING DETALLADO.