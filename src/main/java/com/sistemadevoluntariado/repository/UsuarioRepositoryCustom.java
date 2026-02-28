package com.sistemadevoluntariado.repository;

public interface UsuarioRepositoryCustom {

    boolean registrarUsuarioConVoluntario(int voluntarioId, int rolSistemaId,
                                          String username, String password);

    int cambiarEstado(int idUsuario, String estado);

    int actualizarFotoPerfil(int idUsuario, String fotoPerfil);
}
