package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Recurso;
import com.sistemadevoluntariado.repository.RecursoRepository;

@Service
public class RecursoService {

    @Autowired
    private RecursoRepository recursoRepository;

    @Transactional(readOnly = true)
    public List<Recurso> obtenerTodos() {
        return recursoRepository.obtenerTodos();
    }

    @Transactional(readOnly = true)
    public Recurso obtenerPorId(int id) {
        return recursoRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<String> obtenerTipos() {
        return recursoRepository.obtenerTipos();
    }

    @Transactional
    public Recurso guardar(Recurso recurso) {
        return recursoRepository.save(recurso);
    }

    @Transactional
    public void eliminar(int id) {
        recursoRepository.deleteById(id);
    }
}
