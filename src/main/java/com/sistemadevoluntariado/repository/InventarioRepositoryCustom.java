package com.sistemadevoluntariado.repository;

import java.util.List;

import com.sistemadevoluntariado.entity.InventarioItem;

public interface InventarioRepositoryCustom {

    List<InventarioItem> listar();

    InventarioItem obtenerPorId(int idItem);

    int registrar(InventarioItem item);

    boolean actualizar(InventarioItem item);

    List<InventarioItem> filtrar(String q, String categoria, String estado, boolean stockBajo);

    boolean registrarMovimiento(int idItem, String tipo, String motivo, double cantidad, String observacion, int idUsuario);

    boolean cambiarEstadoItem(int idItem, String estado);

    int contarStockBajo();
}
