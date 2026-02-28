package com.sistemadevoluntariado.repository;

import java.util.List;

import com.sistemadevoluntariado.entity.Donacion;

public interface DonacionRepositoryCustom {

    List<Donacion> listar();

    Donacion obtenerPorId(int id);

    boolean guardar(Donacion d);

    boolean actualizar(Donacion d);

    boolean actualizarPendienteEspecie(Donacion d);

    boolean actualizarDetalleEspecie(int idDonacion, double cantidad, String observacion);

    boolean anular(int id, int idUsuario, String motivo);

    boolean cambiarEstado(int id, String estado);
}
