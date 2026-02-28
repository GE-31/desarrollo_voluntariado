package com.sistemadevoluntariado.repository;

import java.util.List;

public interface PermisoRepositoryCustom {

    List<Integer> obtenerPermisosDeUsuario(int idUsuario);

    boolean tienePermiso(int idUsuario, String nombrePermiso);

    boolean guardarPermisosUsuario(int idUsuario, List<Integer> idsPermisos);

    boolean eliminarPermisosUsuario(int idUsuario);
}
