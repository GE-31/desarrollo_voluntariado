package com.sistemadevoluntariado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.ActividadLugar;

@Repository
public interface ActividadLugarRepository extends JpaRepository<ActividadLugar, Integer> {

    @Query("SELECT al FROM ActividadLugar al WHERE al.idActividad = :idActividad")
    List<ActividadLugar> obtenerPorActividad(int idActividad);

    @Modifying
    @Query("DELETE FROM ActividadLugar al WHERE al.idActividad = :idActividad")
    void eliminarPorActividad(int idActividad);
}
