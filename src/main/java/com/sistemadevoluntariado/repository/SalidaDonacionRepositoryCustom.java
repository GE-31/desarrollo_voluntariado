package com.sistemadevoluntariado.repository;

import java.util.List;
import java.util.Map;

import com.sistemadevoluntariado.entity.SalidaDonacion;

public interface SalidaDonacionRepositoryCustom {

    List<SalidaDonacion> listar();

    SalidaDonacion obtenerPorId(int id);

    boolean guardar(SalidaDonacion s);

    boolean actualizar(SalidaDonacion s);

    boolean anular(int id, int idUsuario, String motivo);

    boolean cambiarEstado(int id, String estado);

    List<Map<String, Object>> listarDonacionesDisponibles();

    List<Map<String, Object>> buscarDonacionesDisponibles(String query);

    Map<String, Object> obtenerSaldoDisponible(int idDonacion);
}
