package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.ActividadRecurso;
import com.sistemadevoluntariado.repository.ActividadRecursoRepository;
import com.sistemadevoluntariado.repository.RecursoRepository;

@Service
public class ActividadRecursoService {

    @Autowired
    private ActividadRecursoRepository actividadRecursoRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Transactional(readOnly = true)
    public List<ActividadRecurso> obtenerPorActividad(int idActividad) {
        List<ActividadRecurso> lista = actividadRecursoRepository.obtenerPorActividad(idActividad);
        // Enriquecer con datos del recurso
        for (ActividadRecurso ar : lista) {
            recursoRepository.findById(ar.getIdRecurso()).ifPresent(r -> {
                ar.setNombreRecurso(r.getNombre());
                ar.setUnidadMedida(r.getUnidadMedida());
                ar.setTipoRecurso(r.getTipoRecurso());
            });
        }
        return lista;
    }

    @Transactional
    public ActividadRecurso guardar(ActividadRecurso ar) {
        return actividadRecursoRepository.save(ar);
    }

    @Transactional
    public void eliminar(int id) {
        actividadRecursoRepository.eliminarPorId(id);
    }

    @Transactional
    public ActividadRecurso actualizarConseguida(int id, double cantidad) {
        ActividadRecurso ar = actividadRecursoRepository.findById(id).orElse(null);
        if (ar != null) {
            ar.setCantidadConseguida(cantidad);
            return actividadRecursoRepository.save(ar);
        }
        return null;
    }
}
