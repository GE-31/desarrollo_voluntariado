package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Permiso;
import com.sistemadevoluntariado.repository.PermisoRepository;

@Service
public class PermisoService {

    @Autowired
    private PermisoRepository permisoRepository;

    @Transactional
    public List<Permiso> obtenerTodosPermisos() {
        return permisoRepository.findAll();
    }

    @Transactional
    public List<Integer> obtenerPermisosDeUsuario(int idUsuario) {
        return permisoRepository.obtenerPermisosDeUsuario(idUsuario);
    }

    @Transactional
    public boolean tienePermiso(int idUsuario, String nombrePermiso) {
        return permisoRepository.tienePermiso(idUsuario, nombrePermiso);
    }

    @Transactional
    public boolean guardarPermisosUsuario(int idUsuario, List<Integer> idsPermisos) {
        return permisoRepository.guardarPermisosUsuario(idUsuario, idsPermisos);
    }

    @Transactional
    public boolean eliminarPermisosUsuario(int idUsuario) {
        return permisoRepository.eliminarPermisosUsuario(idUsuario);
    }
}
