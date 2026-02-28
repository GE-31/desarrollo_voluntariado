package com.sistemadevoluntariado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Participacion;

@Repository
public interface ParticipacionRepository extends JpaRepository<Participacion, Integer> {

    @Query("SELECT p FROM Participacion p WHERE p.idActividad = :idActividad")
    List<Participacion> obtenerPorActividad(int idActividad);

    @Query("SELECT p FROM Participacion p WHERE p.idVoluntario = :idVoluntario")
    List<Participacion> obtenerPorVoluntario(int idVoluntario);

    @Query("SELECT COUNT(p) FROM Participacion p WHERE p.idActividad = :idActividad AND p.idVoluntario = :idVoluntario")
    long existeParticipacion(int idActividad, int idVoluntario);

    @Modifying
    @Query("DELETE FROM Participacion p WHERE p.idParticipacion = :id")
    void eliminarPorId(int id);
}
