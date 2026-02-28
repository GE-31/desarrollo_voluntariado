package com.sistemadevoluntariado.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.InventarioItem;

@Repository
public interface InventarioRepository extends JpaRepository<InventarioItem, Integer>, InventarioRepositoryCustom {
}
