package com.sistemadevoluntariado.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.RolSistema;
import com.sistemadevoluntariado.repository.RolSistemaRepository;

@Service
public class RolSistemaService {

    @Autowired
    private RolSistemaRepository rolSistemaRepository;

    @Transactional
    public List<RolSistema> obtenerTodosRoles() {
        return rolSistemaRepository.findAllByOrderByNombreRolAsc();
    }

    @Transactional
    public String obtenerNombreRolDeUsuario(int idUsuario) {
        return rolSistemaRepository.obtenerNombreRolDeUsuario(idUsuario);
    }

    @Transactional
    public Map<Integer, String> obtenerRolesPorUsuario() {
        return rolSistemaRepository.obtenerRolesPorUsuario();
    }
}
