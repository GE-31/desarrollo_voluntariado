package com.sistemadevoluntariado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.ActividadBeneficiario;

@Repository
public interface ActividadBeneficiarioRepository extends JpaRepository<ActividadBeneficiario, Integer> {

    @Query("SELECT ab FROM ActividadBeneficiario ab WHERE ab.idActividad = :idActividad")
    List<ActividadBeneficiario> obtenerPorActividad(int idActividad);

    @Query("SELECT ab FROM ActividadBeneficiario ab WHERE ab.idBeneficiario = :idBeneficiario")
    List<ActividadBeneficiario> obtenerPorBeneficiario(int idBeneficiario);

    @Query("SELECT COUNT(ab) FROM ActividadBeneficiario ab WHERE ab.idActividad = :idActividad AND ab.idBeneficiario = :idBeneficiario")
    long existeVinculo(int idActividad, int idBeneficiario);

    @Modifying
    @Query("DELETE FROM ActividadBeneficiario ab WHERE ab.idActividadBeneficiario = :id")
    void eliminarPorId(int id);
}
