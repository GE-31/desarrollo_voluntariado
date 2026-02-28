package com.sistemadevoluntariado.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Certificado;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Integer>, CertificadoRepositoryCustom {

    @Query("SELECT COUNT(c) > 0 FROM Certificado c WHERE c.idVoluntario = :v AND c.idActividad = :a AND c.estado = 'EMITIDO'")
    boolean existeCertificadoActivo(@Param("v") int idVoluntario, @Param("a") int idActividad);
}
