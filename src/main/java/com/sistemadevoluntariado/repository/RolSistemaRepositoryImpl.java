package com.sistemadevoluntariado.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class RolSistemaRepositoryImpl implements RolSistemaRepositoryCustom {

    private static final Logger logger = Logger.getLogger(RolSistemaRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public String obtenerNombreRolDeUsuario(int idUsuario) {
        try {
            List<?> resultado = em.createNativeQuery("CALL sp_obtener_nombre_rol_usuario(?1)")
                    .setParameter(1, idUsuario)
                    .getResultList();
            return resultado.isEmpty() ? null : (String) resultado.get(0);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener nombre de rol de usuario", e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Integer, String> obtenerRolesPorUsuario() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_obtener_roles_por_usuario()")
                    .getResultList();
            Map<Integer, String> mapa = new HashMap<>();
            for (Object[] row : rows) {
                mapa.put(((Number) row[0]).intValue(), (String) row[1]);
            }
            return mapa;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener roles por usuario", e);
            return new HashMap<>();
        }
    }
}
