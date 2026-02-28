package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Beneficiario;
import com.sistemadevoluntariado.repository.BeneficiarioRepository;

@Service
public class BeneficiarioService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Transactional
    public List<Beneficiario> obtenerTodosBeneficiarios() {
        return beneficiarioRepository.obtenerTodosBeneficiarios();
    }

    @Transactional
    public Beneficiario obtenerBeneficiarioPorId(int id) {
        return beneficiarioRepository.obtenerBeneficiarioPorId(id);
    }

    @Transactional
    public int crearBeneficiario(Beneficiario b) {
        return beneficiarioRepository.crearBeneficiario(b);
    }

    @Transactional
    public boolean actualizarBeneficiario(Beneficiario b) {
        return beneficiarioRepository.actualizarBeneficiario(b);
    }

    @Transactional
    public boolean cambiarEstado(int id, String estado) {
        return beneficiarioRepository.cambiarEstado(id, estado);
    }

    @Transactional
    public boolean eliminarBeneficiario(int id) {
        return beneficiarioRepository.eliminarBeneficiario(id);
    }
}
