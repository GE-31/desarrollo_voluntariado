package com.sistemadevoluntariado.repository;

import java.math.BigDecimal;
import java.util.List;

import com.sistemadevoluntariado.entity.Asistencia;

public interface AsistenciaRepositoryCustom {

    boolean registrarAsistencia(Asistencia asistencia);

    List<Asistencia> listarAsistencias();

    Asistencia obtenerPorId(int idAsistencia);

    List<Asistencia> listarPorActividad(int idActividad);

    List<Asistencia> listarPorVoluntario(int idVoluntario);

    boolean actualizarAsistencia(Asistencia asistencia);

    boolean eliminarAsistencia(int idAsistencia);

    BigDecimal obtenerHorasVoluntarioActividad(int idVoluntario, int idActividad);
}

//Métodos personalizados para la gestión de asistencias, como 
// registrar, listar, obtener por ID, listar por actividad o 
// voluntario, actualizar, eliminar y obtener horas de voluntariado
//  por actividad.