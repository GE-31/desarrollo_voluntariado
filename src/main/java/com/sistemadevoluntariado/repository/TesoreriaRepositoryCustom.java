package com.sistemadevoluntariado.repository;

import java.util.List;
import java.util.Map;

import com.sistemadevoluntariado.entity.MovimientoFinanciero;

public interface TesoreriaRepositoryCustom {

    List<MovimientoFinanciero> listarConJoins();

    Map<String, Double> obtenerBalance();

    List<MovimientoFinanciero> filtrar(String tipo, String categoria, String fechaInicio, String fechaFin);

    List<Map<String, Object>> resumenPorCategoria();

    List<Map<String, Object>> resumenMensual();
}
