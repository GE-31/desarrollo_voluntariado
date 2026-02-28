package com.sistemadevoluntariado.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Asistencia;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer>, AsistenciaRepositoryCustom {
}
