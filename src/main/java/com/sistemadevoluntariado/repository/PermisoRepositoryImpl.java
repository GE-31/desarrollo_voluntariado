package com.sistemadevoluntariado.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;

public class PermisoRepositoryImpl implements PermisoRepositoryCustom {

    private static final Logger logger = Logger.getLogger(PermisoRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<Integer> obtenerPermisosDeUsuario(int idUsuario) {
        List<Integer> ids = new ArrayList<>();
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_obtener_permisos_usuario");
            spq.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
            spq.setParameter(1, idUsuario);
            // Hibernate 7.x: llamar getResultList() directamente, NO execute() primero
            List<?> results = spq.getResultList();
            for (Object row : results) {
                if (row instanceof Object[] arr) {
                    ids.add(((Number) arr[0]).intValue());
                } else {
                    ids.add(((Number) row).intValue());
                }
            }
            logger.info("✓ Usuario " + idUsuario + " tiene " + ids.size() + " permisos: " + ids);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error en sp_obtener_permisos_usuario para usuario " + idUsuario, e);
        }
        return ids;
    }

    @Override
    public boolean tienePermiso(int idUsuario, String nombrePermiso) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_tiene_permiso");
            spq.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter(2, String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter(3, Integer.class, ParameterMode.OUT);
            spq.setParameter(1, idUsuario);
            spq.setParameter(2, nombrePermiso);
            spq.execute();
            int resultado = (Integer) spq.getOutputParameterValue(3);
            return resultado > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error en sp_tiene_permiso para '" + nombrePermiso + "', usuario " + idUsuario, e);
            return false;
        }
    }

    @Override
    public boolean guardarPermisosUsuario(int idUsuario, List<Integer> idsPermisos) {
        String csv = (idsPermisos == null || idsPermisos.isEmpty())
                ? ""
                : idsPermisos.stream()
                             .map(String::valueOf)
                             .reduce((a, b) -> a + "," + b)
                             .orElse("");
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_guardar_permisos_usuario");
            spq.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter(2, String.class,  ParameterMode.IN);
            spq.setParameter(1, idUsuario);
            spq.setParameter(2, csv);
            spq.execute();
            logger.info("✓ Permisos guardados para usuario ID " + idUsuario + ": [" + csv + "]");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error en sp_guardar_permisos_usuario para usuario " + idUsuario, e);
            return false;
        }
    }

    @Override
    public boolean eliminarPermisosUsuario(int idUsuario) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_eliminar_permisos_usuario");
            spq.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
            spq.setParameter(1, idUsuario);
            spq.execute();
            logger.info("✓ Permisos eliminados para usuario ID " + idUsuario);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error en sp_eliminar_permisos_usuario para usuario " + idUsuario, e);
            return false;
        }
    }
}
