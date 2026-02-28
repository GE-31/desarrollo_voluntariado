package com.sistemadevoluntariado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.RolSistema;

@Repository
public interface RolSistemaRepository extends JpaRepository<RolSistema, Integer>, RolSistemaRepositoryCustom {

    List<RolSistema> findAllByOrderByNombreRolAsc();
}
