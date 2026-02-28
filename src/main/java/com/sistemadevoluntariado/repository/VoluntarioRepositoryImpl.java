package com.sistemadevoluntariado.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Voluntario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;

public class VoluntarioRepositoryImpl implements VoluntarioRepositoryCustom {

    private static final Logger logger = Logger.getLogger(VoluntarioRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean crearVoluntario(Voluntario voluntario) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_crear_voluntario");
            spq.registerStoredProcedureParameter("p_nombres",         String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_apellidos",       String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_dni",             String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_correo",          String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_telefono",        String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_carrera",         String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_id_usuario",      Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_cargo",           String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_acceso_sistema",  Integer.class, ParameterMode.IN);
            spq.setParameter("p_nombres",        voluntario.getNombres());
            spq.setParameter("p_apellidos",      voluntario.getApellidos());
            spq.setParameter("p_dni",            voluntario.getDni());
            spq.setParameter("p_correo",         voluntario.getCorreo());
            spq.setParameter("p_telefono",       voluntario.getTelefono());
            spq.setParameter("p_carrera",        voluntario.getCarrera());
            spq.setParameter("p_id_usuario",     voluntario.getIdUsuario() != null ? voluntario.getIdUsuario() : 0);
            spq.setParameter("p_cargo",          voluntario.getCargo() != null ? voluntario.getCargo() : "Voluntario");
            spq.setParameter("p_acceso_sistema", voluntario.isAccesoSistema() ? 1 : 0);
            spq.execute();
            logger.info("✓ Voluntario creado: " + voluntario.getNombres());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al crear voluntario: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean actualizarVoluntario(Voluntario voluntario) {
        try {
            // 1) Llamar al SP existente para los campos básicos
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_actualizar_voluntario");
            spq.registerStoredProcedureParameter("p_id_voluntario", Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_nombres",       String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_apellidos",     String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_dni",           String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_correo",        String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_telefono",      String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_carrera",       String.class,  ParameterMode.IN);
            spq.setParameter("p_id_voluntario", voluntario.getIdVoluntario());
            spq.setParameter("p_nombres",       voluntario.getNombres());
            spq.setParameter("p_apellidos",     voluntario.getApellidos());
            spq.setParameter("p_dni",           voluntario.getDni());
            spq.setParameter("p_correo",        voluntario.getCorreo());
            spq.setParameter("p_telefono",      voluntario.getTelefono());
            spq.setParameter("p_carrera",       voluntario.getCarrera());
            spq.execute();

            // 2) Actualizar cargo y acceso_sistema (campos no incluidos en el SP)
            em.createNativeQuery("UPDATE voluntario SET cargo = :cargo, acceso_sistema = :acceso WHERE id_voluntario = :id")
              .setParameter("cargo",  voluntario.getCargo() != null ? voluntario.getCargo() : "Voluntario")
              .setParameter("acceso", voluntario.isAccesoSistema() ? 1 : 0)
              .setParameter("id",     voluntario.getIdVoluntario())
              .executeUpdate();

            logger.info("✓ Voluntario actualizado (cargo=" + voluntario.getCargo()
                      + ", accesoSistema=" + voluntario.isAccesoSistema() + "): " + voluntario.getNombres());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al actualizar voluntario", e);
            return false;
        }
    }

    @Override
    public boolean cambiarEstado(int idVoluntario, String nuevoEstado) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_cambiar_estado_voluntario");
            spq.registerStoredProcedureParameter("p_id_voluntario", Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_estado",        String.class,  ParameterMode.IN);
            spq.setParameter("p_id_voluntario", idVoluntario);
            spq.setParameter("p_estado",        nuevoEstado);
            spq.execute();
            logger.info("✓ Estado del voluntario actualizado a: " + nuevoEstado);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al cambiar estado del voluntario", e);
            return false;
        }
    }

    @Override
    public boolean eliminarVoluntario(int idVoluntario) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_eliminar_voluntario");
            spq.registerStoredProcedureParameter("p_id_voluntario", Integer.class, ParameterMode.IN);
            spq.setParameter("p_id_voluntario", idVoluntario);
            spq.execute();
            logger.info("✓ Voluntario eliminado correctamente ID: " + idVoluntario);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al eliminar voluntario", e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional
    public List<Voluntario> obtenerVoluntariosConAsistencia() {
        List<Voluntario> lista = new ArrayList<>();
        try {
            List<Object[]> rows = em.createNativeQuery(
                "SELECT DISTINCT v.id_voluntario, v.nombres, v.apellidos, v.dni " +
                "FROM voluntario v INNER JOIN asistencias a ON v.id_voluntario = a.id_voluntario " +
                "WHERE a.estado IN ('ASISTIO','TARDANZA') AND v.estado = 'ACTIVO' " +
                "ORDER BY v.apellidos, v.nombres")
                .getResultList();
            for (Object[] row : rows) {
                Voluntario vol = new Voluntario();
                vol.setIdVoluntario(row[0] != null ? ((Number) row[0]).intValue() : 0);
                vol.setNombres(row[1] != null ? row[1].toString() : "");
                vol.setApellidos(row[2] != null ? row[2].toString() : "");
                vol.setDni(row[3] != null ? row[3].toString() : "");
                vol.setEstado("ACTIVO");
                lista.add(vol);
            }
            logger.info("✓ Voluntarios con asistencia: " + lista.size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al obtener voluntarios con asistencia", e);
        }
        return lista;
    }
}
