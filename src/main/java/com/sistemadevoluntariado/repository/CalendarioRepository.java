package com.sistemadevoluntariado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Calendario;

@Repository
public interface CalendarioRepository extends JpaRepository<Calendario, Integer> {

    List<Calendario> findAllByOrderByFechaInicioAsc();
}
