package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Actividad;
import com.sistemadevoluntariado.repository.ActividadRepository;

@Service
public class ActividadService {

    @Autowired
    private ActividadRepository actividadRepository;

    @Transactional
    public List<Actividad> obtenerTodasActividades() {
        return actividadRepository.obtenerTodasActividades();
    }

    @Transactional
    public Actividad obtenerActividadPorId(int id) {
        return actividadRepository.obtenerActividadPorId(id);
    }

    @Transactional
    public List<Actividad> obtenerActividadesActivas() {
        return actividadRepository.obtenerActividadesActivas();
    }

    @Transactional
    public boolean crearActividad(Actividad actividad) {
        return actividadRepository.crearActividad(actividad);
    }

    @Transactional
    public boolean actualizarActividad(Actividad actividad) {
        return actividadRepository.actualizarActividad(actividad);
    }

    @Transactional
    public boolean cambiarEstado(int id, String estado) {
        return actividadRepository.cambiarEstado(id, estado);
    }

    @Transactional
    public boolean eliminarActividad(int id) {
        return actividadRepository.eliminarActividad(id);
    }

    @Transactional
    public boolean tieneCupoDisponible(int idActividad) {
        return actividadRepository.tieneCupoDisponible(idActividad);
    }

    @Transactional
    public boolean actualizarInscritosPorAsistencia(int idActividad) {
        return actividadRepository.actualizarInscritosPorAsistencia(idActividad);
    }

    @Transactional
    public boolean actualizarInscritosPorParticipacion(int idActividad) {
        return actividadRepository.actualizarInscritosPorParticipacion(idActividad);
    }
}

//logica de negocio para manejar las actividades, 
// incluyendo la obtención de actividades, creación, 
// actualización, cambio de estado y eliminación. 
// También incluye métodos para verificar el cupo 
// disponible y actualizar el número de inscritos 
// según la asistencia.