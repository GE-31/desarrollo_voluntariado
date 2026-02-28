package com.sistemadevoluntariado.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer>, NotificacionRepositoryCustom {
    // Todas las operaciones delegadas a SPs via NotificacionRepositoryImpl
}
