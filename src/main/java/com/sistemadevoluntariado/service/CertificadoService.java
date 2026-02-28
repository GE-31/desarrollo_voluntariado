package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Certificado;
import com.sistemadevoluntariado.repository.CertificadoRepository;

@Service
public class CertificadoService {

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Transactional
    public List<Certificado> obtenerTodosCertificados() {
        return certificadoRepository.obtenerTodosCertificados();
    }

    @Transactional
    public Certificado obtenerCertificadoPorId(int id) {
        return certificadoRepository.obtenerCertificadoPorId(id);
    }

    @Transactional
    public Certificado obtenerCertificadoPorCodigo(String codigo) {
        return certificadoRepository.obtenerCertificadoPorCodigo(codigo);
    }

    @Transactional
    public boolean existeCertificadoActivo(int idVoluntario, int idActividad) {
        return certificadoRepository.existeCertificadoActivo(idVoluntario, idActividad);
    }

    @Transactional
    public List<Certificado> obtenerCertificadosPorVoluntario(int idVoluntario) {
        return certificadoRepository.obtenerCertificadosPorVoluntario(idVoluntario);
    }

    @Transactional
    public boolean crearCertificado(Certificado certificado) {
        return certificadoRepository.crearCertificado(certificado);
    }

    @Transactional
    public boolean anularCertificado(int id, String motivo) {
        return certificadoRepository.anularCertificado(id, motivo);
    }
}
