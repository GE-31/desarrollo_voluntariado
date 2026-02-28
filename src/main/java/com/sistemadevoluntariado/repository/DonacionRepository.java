package com.sistemadevoluntariado.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Donacion;

@Repository
public interface DonacionRepository extends JpaRepository<Donacion, Integer>, DonacionRepositoryCustom {
}
