package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Voluntario;
import com.sistemadevoluntariado.repository.VoluntarioRepository;

@Service
public class VoluntarioService {

    @Autowired
    private VoluntarioRepository voluntarioRepository;

    @Transactional
    public List<Voluntario> obtenerTodosVoluntarios() {
        return voluntarioRepository.findAllByOrderByIdVoluntarioDesc();
    }

    @Transactional
    public Voluntario obtenerVoluntarioPorId(int id) {
        return voluntarioRepository.findById(id).orElse(null);
    }

    @Transactional
    public Voluntario obtenerVoluntarioPorUsuarioId(int idUsuario) {
        return voluntarioRepository.findFirstByIdUsuarioOrderByIdVoluntarioDesc(idUsuario).orElse(null);
    }

    @Transactional
    public List<Voluntario> obtenerVoluntariosConAcceso() {
        return voluntarioRepository.obtenerVoluntariosConAcceso();
    }

    @Transactional
    public List<Voluntario> obtenerVoluntariosConAsistencia() {
        return voluntarioRepository.obtenerVoluntariosConAsistencia();
    }

    @Transactional
    public boolean crearVoluntario(Voluntario voluntario) {
        return voluntarioRepository.crearVoluntario(voluntario);
    }

    @Transactional
    public boolean actualizarVoluntario(Voluntario voluntario) {
        return voluntarioRepository.actualizarVoluntario(voluntario);
    }

    @Transactional
    public boolean cambiarEstado(int id, String estado) {
        return voluntarioRepository.cambiarEstado(id, estado);
    }

    @Transactional
    public boolean eliminarVoluntario(int id) {
        return voluntarioRepository.eliminarVoluntario(id);
    }
}
