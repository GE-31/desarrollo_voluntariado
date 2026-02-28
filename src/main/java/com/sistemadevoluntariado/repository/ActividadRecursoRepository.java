package com.sistemadevoluntariado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.ActividadRecurso;

@Repository
public interface ActividadRecursoRepository extends JpaRepository<ActividadRecurso, Integer> {

    @Query("SELECT ar FROM ActividadRecurso ar WHERE ar.idActividad = :idActividad")
    List<ActividadRecurso> obtenerPorActividad(int idActividad);

    @Modifying
    @Query("DELETE FROM ActividadRecurso ar WHERE ar.idActividadRecurso = :id")
    void eliminarPorId(int id);
}
