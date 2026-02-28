package com.sistemadevoluntariado.repository;

import java.sql.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sistemadevoluntariado.entity.Beneficiario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;

public class BeneficiarioRepositoryImpl implements BeneficiarioRepositoryCustom {

    private static final Logger logger = Logger.getLogger(BeneficiarioRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public int crearBeneficiario(Beneficiario b) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_crear_beneficiario_adaptado");
            spq.registerStoredProcedureParameter("p_nombres",             String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_apellidos",           String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_dni",                 String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_fecha_nacimiento",    Date.class,    ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_telefono",            String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_direccion",           String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_distrito",            String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_tipo_beneficiario",   String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_necesidad_principal", String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_observaciones",       String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_id_usuario",          Integer.class, ParameterMode.IN);
            spq.setParameter("p_nombres",             b.getNombres());
            spq.setParameter("p_apellidos",           b.getApellidos());
            spq.setParameter("p_dni",                 b.getDni());
            spq.setParameter("p_fecha_nacimiento",    b.getFechaNacimiento() != null ? Date.valueOf(b.getFechaNacimiento()) : null);
            spq.setParameter("p_telefono",            b.getTelefono());
            spq.setParameter("p_direccion",           b.getDireccion());
            spq.setParameter("p_distrito",            b.getDistrito());
            spq.setParameter("p_tipo_beneficiario",   b.getTipoBeneficiario());
            spq.setParameter("p_necesidad_principal", b.getNecesidadPrincipal());
            spq.setParameter("p_observaciones",       b.getObservaciones());
            spq.setParameter("p_id_usuario",          b.getIdUsuario() != null ? b.getIdUsuario() : 0);
            @SuppressWarnings("unchecked")
            List<Object[]> rows = spq.getResultList();
            int newId = 0;
            if (!rows.isEmpty()) {
                Object val = rows.get(0);
                if (val instanceof Object[]) newId = ((Number)((Object[])val)[0]).intValue();
                else newId = ((Number)val).intValue();
            }
            logger.info("✓ Beneficiario creado (ID=" + newId + "): " + b.getNombres() + " " + b.getApellidos());
            return newId;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al crear beneficiario: " + e.getMessage(), e);
            return 0;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Beneficiario> obtenerTodosBeneficiarios() {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_obtener_todos_beneficiarios", Beneficiario.class);
            // Hibernate 7.x: getResultList() ejecuta internamente, NO llamar execute() antes
            List<Beneficiario> lista = spq.getResultList();
            logger.info("✓ Se obtuvieron " + lista.size() + " beneficiarios");
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al obtener beneficiarios", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Beneficiario obtenerBeneficiarioPorId(int idBeneficiario) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_obtener_beneficiario_por_id", Beneficiario.class);
            spq.registerStoredProcedureParameter("p_id_beneficiario", Integer.class, ParameterMode.IN);
            spq.setParameter("p_id_beneficiario", idBeneficiario);
            List<?> result = spq.getResultList();
            return result.isEmpty() ? null : (Beneficiario) result.get(0);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al obtener beneficiario por ID", e);
            return null;
        }
    }

    @Override
    public boolean actualizarBeneficiario(Beneficiario b) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_actualizar_beneficiario");
            spq.registerStoredProcedureParameter("p_id_beneficiario",    Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_nombres",            String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_apellidos",          String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_dni",                String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_fecha_nacimiento",   Date.class,    ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_telefono",           String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_direccion",          String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_distrito",           String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_tipo_beneficiario",  String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_necesidad_principal",String.class,  ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_observaciones",      String.class,  ParameterMode.IN);
            spq.setParameter("p_id_beneficiario",    b.getIdBeneficiario());
            spq.setParameter("p_nombres",            b.getNombres());
            spq.setParameter("p_apellidos",          b.getApellidos());
            spq.setParameter("p_dni",                b.getDni());
            spq.setParameter("p_fecha_nacimiento",   b.getFechaNacimiento() != null ? Date.valueOf(b.getFechaNacimiento()) : null);
            spq.setParameter("p_telefono",           b.getTelefono());
            spq.setParameter("p_direccion",          b.getDireccion());
            spq.setParameter("p_distrito",           b.getDistrito());
            spq.setParameter("p_tipo_beneficiario",  b.getTipoBeneficiario());
            spq.setParameter("p_necesidad_principal",b.getNecesidadPrincipal());
            spq.setParameter("p_observaciones",      b.getObservaciones());
            spq.execute();
            logger.info("✓ Beneficiario actualizado correctamente: " + b.getNombres());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al actualizar beneficiario", e);
            return false;
        }
    }

    @Override
    public boolean cambiarEstado(int idBeneficiario, String nuevoEstado) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_cambiar_estado_beneficiario");
            spq.registerStoredProcedureParameter("p_id_beneficiario", Integer.class, ParameterMode.IN);
            spq.registerStoredProcedureParameter("p_estado",          String.class,  ParameterMode.IN);
            spq.setParameter("p_id_beneficiario", idBeneficiario);
            spq.setParameter("p_estado",          nuevoEstado);
            spq.execute();
            logger.info("✓ Estado del beneficiario actualizado a: " + nuevoEstado);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al cambiar estado del beneficiario", e);
            return false;
        }
    }

    @Override
    public boolean eliminarBeneficiario(int idBeneficiario) {
        try {
            StoredProcedureQuery spq = em.createStoredProcedureQuery("sp_eliminar_beneficiario");
            spq.registerStoredProcedureParameter("p_id_beneficiario", Integer.class, ParameterMode.IN);
            spq.setParameter("p_id_beneficiario", idBeneficiario);
            spq.execute();
            logger.info("✓ Beneficiario eliminado correctamente ID: " + idBeneficiario);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "✗ Error al eliminar beneficiario", e);
            return false;
        }
    }
}
