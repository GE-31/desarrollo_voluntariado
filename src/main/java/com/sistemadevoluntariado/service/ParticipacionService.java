package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Participacion;
import com.sistemadevoluntariado.entity.RolActividad;
import com.sistemadevoluntariado.repository.ParticipacionRepository;
import com.sistemadevoluntariado.repository.RolActividadRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ParticipacionService {

    @Autowired
    private ParticipacionRepository participacionRepository;

    @Autowired
    private RolActividadRepository rolActividadRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public List<RolActividad> obtenerRoles() {
        return rolActividadRepository.obtenerTodos();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Participacion> obtenerPorActividad(int idActividad) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT p.id_participacion, p.id_voluntario, p.id_actividad, p.id_rol_actividad, " +
                "CONCAT(v.nombres, ' ', v.apellidos) AS nombre_voluntario, v.dni, " +
                "COALESCE(v.carrera, '') AS carrera " +
                "FROM participacion p " +
                "JOIN voluntario v ON v.id_voluntario = p.id_voluntario " +
                "WHERE p.id_actividad = :idAct ORDER BY nombre_voluntario")
            .setParameter("idAct", idActividad)
            .getResultList();

        return rows.stream().map(r -> {
            Participacion p = new Participacion();
            p.setIdParticipacion(((Number) r[0]).intValue());
            p.setIdVoluntario(((Number) r[1]).intValue());
            p.setIdActividad(((Number) r[2]).intValue());
            p.setIdRolActividad(r[3] != null ? ((Number) r[3]).intValue() : 0);
            p.setNombreVoluntario((String) r[4]);
            p.setDniVoluntario((String) r[5]);
            p.setCarreraVoluntario((String) r[6]);
            return p;
        }).toList();
    }

    @Transactional
    public Participacion guardar(int idActividad, int idVoluntario) {
        // Verificar que no exista ya
        if (participacionRepository.existeParticipacion(idActividad, idVoluntario) > 0) {
            throw new RuntimeException("El voluntario ya está asignado a esta actividad");
        }
        Participacion p = new Participacion();
        p.setIdActividad(idActividad);
        p.setIdVoluntario(idVoluntario);
        p.setIdRolActividad(null);
        return participacionRepository.save(p);
    }

    @Transactional
    public void eliminar(int id) {
        participacionRepository.eliminarPorId(id);
    }

    @Transactional
    public int eliminarYObtenerActividad(int id) {
        var opt = participacionRepository.findById(id);
        if (opt.isPresent()) {
            int idActividad = opt.get().getIdActividad();
            participacionRepository.eliminarPorId(id);
            return idActividad;
        }
        participacionRepository.eliminarPorId(id);
        return 0;
    }
}
