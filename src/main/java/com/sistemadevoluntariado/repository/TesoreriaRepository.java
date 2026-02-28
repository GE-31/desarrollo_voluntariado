package com.sistemadevoluntariado.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.MovimientoFinanciero;

@Repository
public interface TesoreriaRepository extends JpaRepository<MovimientoFinanciero, Integer>, TesoreriaRepositoryCustom {
}
