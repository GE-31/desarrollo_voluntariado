package com.sistemadevoluntariado.repository;

import java.util.List;

import com.sistemadevoluntariado.entity.Voluntario;

public interface VoluntarioRepositoryCustom {

    boolean crearVoluntario(Voluntario voluntario);

    boolean actualizarVoluntario(Voluntario voluntario);

    boolean cambiarEstado(int idVoluntario, String nuevoEstado);

    boolean eliminarVoluntario(int idVoluntario);

    List<Voluntario> obtenerVoluntariosConAsistencia();
}
