package com.sistemadevoluntariado.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Voluntario;

@Repository
public interface VoluntarioRepository extends JpaRepository<Voluntario, Integer>, VoluntarioRepositoryCustom {

    List<Voluntario> findAllByOrderByIdVoluntarioDesc();

    Optional<Voluntario> findFirstByIdUsuarioOrderByIdVoluntarioDesc(int idUsuario);

    @Query("SELECT v FROM Voluntario v WHERE v.accesoSistema = true AND v.idUsuario IS NULL ORDER BY v.nombres ASC")
    List<Voluntario> obtenerVoluntariosConAcceso();
}
