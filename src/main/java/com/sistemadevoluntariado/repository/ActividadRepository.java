package com.sistemadevoluntariado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Actividad;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer>, ActividadRepositoryCustom {

    @Query("SELECT a FROM Actividad a WHERE a.estado = 'ACTIVO' ORDER BY a.idActividad DESC")
    List<Actividad> obtenerActividadesActivas();
}

// jpa repository le metodo crud automatico , hereda metodos perosnalizados que llama 
// a procedimiento almacenado , busca actividades con estado activo y ordena por id 
// de actividad descendente.