package com.sistemadevoluntariado.repository;

import java.util.List;

import com.sistemadevoluntariado.entity.Actividad;

public interface ActividadRepositoryCustom {

    boolean crearActividad(Actividad actividad);

    List<Actividad> obtenerTodasActividades();

    Actividad obtenerActividadPorId(int idActividad);

    boolean actualizarActividad(Actividad actividad);

    boolean cambiarEstado(int idActividad, String nuevoEstado);

    boolean eliminarActividad(int idActividad);

    boolean tieneCupoDisponible(int idActividad);

    boolean actualizarInscritosPorAsistencia(int idActividad);

    boolean actualizarInscritosPorParticipacion(int idActividad);
}


//Aquí solo declaras
// y Define 8 métodos que no se pueden hacer con JPA automático
