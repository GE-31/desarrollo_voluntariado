package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Lugar;
import com.sistemadevoluntariado.repository.LugarRepository;

@Service
public class LugarService {

    @Autowired
    private LugarRepository lugarRepository;

    @Transactional(readOnly = true)
    public List<Lugar> obtenerTodos() {
        return lugarRepository.obtenerTodos();
    }

    @Transactional(readOnly = true)
    public Lugar obtenerPorId(int id) {
        return lugarRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<String> obtenerDepartamentos() {
        return lugarRepository.obtenerDepartamentos();
    }

    @Transactional(readOnly = true)
    public List<String> obtenerProvinciasPorDepartamento(String dep) {
        return lugarRepository.obtenerProvinciasPorDepartamento(dep);
    }

    @Transactional(readOnly = true)
    public List<Lugar> obtenerPorDepartamentoYProvincia(String dep, String prov) {
        return lugarRepository.obtenerPorDepartamentoYProvincia(dep, prov);
    }

    @Transactional
    public Lugar guardar(Lugar lugar) {
        return lugarRepository.save(lugar);
    }

    @Transactional
    public void eliminar(int id) {
        lugarRepository.deleteById(id);
    }
}
