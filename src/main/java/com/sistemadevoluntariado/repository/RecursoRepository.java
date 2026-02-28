package com.sistemadevoluntariado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Recurso;

@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Integer> {

    @Query("SELECT r FROM Recurso r ORDER BY r.nombre")
    List<Recurso> obtenerTodos();

    @Query("SELECT DISTINCT r.tipoRecurso FROM Recurso r WHERE r.tipoRecurso IS NOT NULL ORDER BY r.tipoRecurso")
    List<String> obtenerTipos();
}
