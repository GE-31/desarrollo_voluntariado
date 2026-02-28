package com.sistemadevoluntariado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Lugar;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, Integer> {

    @Query("SELECT l FROM Lugar l ORDER BY l.departamento, l.provincia, l.distrito")
    List<Lugar> obtenerTodos();

    @Query("SELECT DISTINCT l.departamento FROM Lugar l ORDER BY l.departamento")
    List<String> obtenerDepartamentos();

    @Query("SELECT DISTINCT l.provincia FROM Lugar l WHERE l.departamento = :dep ORDER BY l.provincia")
    List<String> obtenerProvinciasPorDepartamento(String dep);

    @Query("SELECT l FROM Lugar l WHERE l.departamento = :dep AND l.provincia = :prov ORDER BY l.distrito")
    List<Lugar> obtenerPorDepartamentoYProvincia(String dep, String prov);
}
