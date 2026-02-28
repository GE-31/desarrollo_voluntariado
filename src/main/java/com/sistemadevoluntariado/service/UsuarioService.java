package com.sistemadevoluntariado.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.repository.PermisoRepository;
import com.sistemadevoluntariado.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    @Transactional
    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAllByOrderByIdUsuarioDesc();
    }

    @Transactional
    public Usuario obtenerUsuarioPorId(int id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Transactional
    public Usuario obtenerUsuarioPorUsername(String username) {
        Optional<Usuario> opt = usuarioRepository.findByUsername(username);
        return opt.orElse(null);
    }

    @Transactional
    public boolean registrarUsuarioConVoluntario(int voluntarioId, int rolSistemaId,
                                                  String username, String password) {
        return usuarioRepository.registrarUsuarioConVoluntario(voluntarioId, rolSistemaId, username, password);
    }

    @Transactional
    public boolean cambiarEstado(int id, String estado) {
        return usuarioRepository.cambiarEstado(id, estado) > 0;
    }

    @Transactional
    public boolean eliminarUsuario(int id) {
        // Eliminar permisos primero (por FK), luego el usuario
        permisoRepository.eliminarPermisosUsuario(id);
        try {
            usuarioRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public boolean actualizarFotoPerfil(int idUsuario, String fotoPerfil) {
        return usuarioRepository.actualizarFotoPerfil(idUsuario, fotoPerfil) > 0;
    }
}
