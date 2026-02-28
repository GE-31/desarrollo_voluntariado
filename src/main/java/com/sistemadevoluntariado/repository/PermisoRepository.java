package com.sistemadevoluntariado.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Permiso;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Integer>, PermisoRepositoryCustom {
}
