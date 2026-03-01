package com.sistemadevoluntariado.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Beneficiario;

@Repository
public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Integer>, BeneficiarioRepositoryCustom {

    Optional<Beneficiario> findByDni(String dni);

    // Nueva consulta: solo usa los nuevos campos, sin referencia a nombres ni apellidos
    @Query("SELECT b FROM Beneficiario b WHERE b.estado = 'ACTIVO' AND b.organizacion IS NOT NULL ORDER BY b.idBeneficiario DESC")
    List<Beneficiario> obtenerActivos();
}
