package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.ActividadBeneficiario;
import com.sistemadevoluntariado.repository.ActividadBeneficiarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ActividadBeneficiarioService {

    @Autowired
    private ActividadBeneficiarioRepository actividadBeneficiarioRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ActividadBeneficiario> obtenerPorActividad(int idActividad) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT ab.id_actividad_beneficiario, ab.id_actividad, ab.id_beneficiario, ab.observacion, " +
                "b.organizacion, b.direccion, b.distrito, b.necesidad_principal, b.observaciones, " +
                "b.nombre_responsable, b.apellidos_responsable, b.dni, b.telefono " +
                "FROM actividad_beneficiario ab " +
                "JOIN beneficiario b ON b.id_beneficiario = ab.id_beneficiario " +
                "WHERE ab.id_actividad = :idAct ORDER BY b.organizacion")
            .setParameter("idAct", idActividad)
            .getResultList();

        return rows.stream().map(r -> {
            ActividadBeneficiario ab = new ActividadBeneficiario();
            ab.setIdActividadBeneficiario(((Number) r[0]).intValue());
            ab.setIdActividad(((Number) r[1]).intValue());
            ab.setIdBeneficiario(((Number) r[2]).intValue());
            ab.setObservacion(r[3] != null ? (String) r[3] : "");
            ab.setOrganizacion((String) r[4]);
            ab.setDireccion((String) r[5]);
            ab.setDistrito((String) r[6]);
            ab.setNecesidadPrincipal((String) r[7]);
            ab.setObservaciones((String) r[8]);
            ab.setNombreResponsable((String) r[9]);
            ab.setApellidosResponsable((String) r[10]);
            ab.setDni((String) r[11]);
            ab.setTelefono((String) r[12]);
            return ab;
        }).toList();
    }

    @Transactional
    public ActividadBeneficiario guardar(int idActividad, int idBeneficiario, String observacion) {
        if (actividadBeneficiarioRepository.existeVinculo(idActividad, idBeneficiario) > 0) {
            throw new RuntimeException("El beneficiario ya está vinculado a esta actividad");
        }
        ActividadBeneficiario ab = new ActividadBeneficiario();
        ab.setIdActividad(idActividad);
        ab.setIdBeneficiario(idBeneficiario);
        ab.setObservacion(observacion);
        return actividadBeneficiarioRepository.save(ab);
    }

    @Transactional
    public void eliminar(int id) {
        actividadBeneficiarioRepository.eliminarPorId(id);
    }
}
