package com.sistemadevoluntariado.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.SalidaDonacion;

@Repository
public interface SalidaDonacionRepository extends JpaRepository<SalidaDonacion, Integer>, SalidaDonacionRepositoryCustom {
}
