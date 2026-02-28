package com.sistemadevoluntariado.repository;

import java.util.List;

import com.sistemadevoluntariado.entity.Beneficiario;

public interface BeneficiarioRepositoryCustom {

    int crearBeneficiario(Beneficiario b);

    List<Beneficiario> obtenerTodosBeneficiarios();

    Beneficiario obtenerBeneficiarioPorId(int idBeneficiario);

    boolean actualizarBeneficiario(Beneficiario b);

    boolean cambiarEstado(int idBeneficiario, String nuevoEstado);

    boolean eliminarBeneficiario(int idBeneficiario);
}
