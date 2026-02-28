package com.sistemadevoluntariado.repository;

import java.util.Map;

public interface RolSistemaRepositoryCustom {

    String obtenerNombreRolDeUsuario(int idUsuario);

    Map<Integer, String> obtenerRolesPorUsuario();
}
