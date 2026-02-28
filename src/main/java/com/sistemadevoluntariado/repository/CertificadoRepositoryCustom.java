package com.sistemadevoluntariado.repository;

import java.util.List;

import com.sistemadevoluntariado.entity.Certificado;

public interface CertificadoRepositoryCustom {

    boolean crearCertificado(Certificado c);

    List<Certificado> obtenerTodosCertificados();

    Certificado obtenerCertificadoPorId(int id);

    Certificado obtenerCertificadoPorCodigo(String codigo);

    boolean anularCertificado(int id, String motivo);

    List<Certificado> obtenerCertificadosPorVoluntario(int idVoluntario);
}
