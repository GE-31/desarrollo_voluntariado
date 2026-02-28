package com.sistemadevoluntariado.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sistemadevoluntariado.entity.Notificacion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class NotificacionRepositoryImpl implements NotificacionRepositoryCustom {

    private static final Logger logger = Logger.getLogger(NotificacionRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<Notificacion> listarPorUsuario(int idUsuario) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listar_notificaciones(?1)")
                    .setParameter(1, idUsuario)
                    .getResultList();
            List<Notificacion> lista = new ArrayList<>();
            for (Object[] row : rows) {
                Notificacion n = new Notificacion();
                n.setIdNotificacion(((Number) row[0]).intValue());
                n.setIdUsuario(((Number) row[1]).intValue());
                n.setTipo((String) row[2]);
                n.setTitulo((String) row[3]);
                n.setMensaje((String) row[4]);
                n.setIcono((String) row[5]);
                n.setColor((String) row[6]);
                n.setLeida(row[7] != null && ((Number) row[7]).intValue() == 1);
                n.setReferenciaId(row[8] != null ? ((Number) row[8]).intValue() : 0);
                n.setFechaCreacion(row[9] != null ? row[9].toString() : null);
                lista.add(n);
            }
            logger.info("Se listaron " + lista.size() + " notificaciones para usuario " + idUsuario);
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar notificaciones", e);
            return List.of();
        }
    }

    @Override
    public int contarNoLeidas(int idUsuario) {
        try {
            Object result = em.createNativeQuery("CALL sp_contar_notificaciones_no_leidas(?1)")
                    .setParameter(1, idUsuario)
                    .getSingleResult();
            return result != null ? ((Number) result).intValue() : 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al contar notificaciones no leidas", e);
            return 0;
        }
    }

    @Override
    public void marcarLeida(int idNotificacion) {
        try {
            em.createNativeQuery("CALL sp_marcar_notificacion_leida(?1)")
                    .setParameter(1, idNotificacion)
                    .executeUpdate();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al marcar notificacion leida", e);
        }
    }

    @Override
    public void marcarTodasLeidas(int idUsuario) {
        try {
            em.createNativeQuery("CALL sp_marcar_todas_leidas(?1)")
                    .setParameter(1, idUsuario)
                    .executeUpdate();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al marcar todas leidas", e);
        }
    }

    @Override
    public void generarNotificacionesActividadesHoy(int idUsuario) {
        try {
            em.createNativeQuery("CALL sp_generar_notificaciones_actividades_hoy(?1)")
                    .setParameter(1, idUsuario)
                    .executeUpdate();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al generar notificaciones de actividades: " + e.getMessage());
        }
    }

    @Override
    public void generarNotificacionesEventosHoy(int idUsuario) {
        try {
            em.createNativeQuery("CALL sp_generar_notificaciones_eventos_hoy(?1)")
                    .setParameter(1, idUsuario)
                    .executeUpdate();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al generar notificaciones de eventos: " + e.getMessage());
        }
    }
}
