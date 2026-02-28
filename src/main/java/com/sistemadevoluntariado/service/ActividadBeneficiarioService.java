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
                "SELECT ab.id_actividad_beneficiario, ab.id_actividad, ab.id_beneficiario, " +
                "ab.observacion, CONCAT(b.nombres, ' ', b.apellidos) AS nombre, b.dni, b.tipo_beneficiario " +
                "FROM actividad_beneficiario ab " +
                "JOIN beneficiario b ON b.id_beneficiario = ab.id_beneficiario " +
                "WHERE ab.id_actividad = :idAct ORDER BY nombre")
            .setParameter("idAct", idActividad)
            .getResultList();

        return rows.stream().map(r -> {
            ActividadBeneficiario ab = new ActividadBeneficiario();
            ab.setIdActividadBeneficiario(((Number) r[0]).intValue());
            ab.setIdActividad(((Number) r[1]).intValue());
            ab.setIdBeneficiario(((Number) r[2]).intValue());
            ab.setObservacion(r[3] != null ? (String) r[3] : "");
            ab.setNombreBeneficiario((String) r[4]);
            ab.setDniBeneficiario((String) r[5]);
            ab.setTipoBeneficiario(r[6] != null ? (String) r[6] : "");
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
