package com.sistemadevoluntariado.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemadevoluntariado.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>, UsuarioRepositoryCustom {

    Optional<Usuario> findByUsername(String username);

    List<Usuario> findAllByOrderByIdUsuarioDesc();
}
