package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.CategoriaInventario;
import com.sistemadevoluntariado.entity.InventarioItem;
import com.sistemadevoluntariado.repository.CategoriaInventarioRepository;
import com.sistemadevoluntariado.repository.InventarioRepository;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private CategoriaInventarioRepository categoriaRepository;

    // ── Categorías ──

    @Transactional
    public List<CategoriaInventario> listarCategorias() {
        return categoriaRepository.findAllByOrderByIdCategoriaAsc();
    }

    @Transactional
    public boolean registrarCategoria(CategoriaInventario cat) {
        try {
            categoriaRepository.save(cat);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public boolean actualizarCategoria(CategoriaInventario cat) {
        try {
            categoriaRepository.save(cat);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public boolean eliminarCategoria(int idCategoria) {
        try {
            categoriaRepository.deleteById(idCategoria);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Items de inventario ──

    @Transactional
    public List<InventarioItem> listar() {
        return inventarioRepository.listar();
    }

    @Transactional
    public InventarioItem obtenerPorId(int id) {
        return inventarioRepository.obtenerPorId(id);
    }

    @Transactional
    public List<InventarioItem> filtrar(String q, String categoria, String estado, boolean stockBajo) {
        return inventarioRepository.filtrar(q, categoria, estado, stockBajo);
    }

    @Transactional
    public int contarStockBajo() {
        return inventarioRepository.contarStockBajo();
    }

    /**
     * Registrar nuevo item. Retorna el ID generado o -1 si falla.
     */
    @Transactional
    public int registrar(InventarioItem item) {
        return inventarioRepository.registrar(item);
    }

    @Transactional
    public boolean actualizar(InventarioItem item) {
        return inventarioRepository.actualizar(item);
    }

    @Transactional
    public boolean cambiarEstado(int idItem, String estado) {
        return inventarioRepository.cambiarEstadoItem(idItem, estado);
    }

    @Transactional
    public boolean registrarMovimiento(int idItem, String tipo, String motivo,
                                        double cantidad, String observacion, int idUsuario) {
        return inventarioRepository.registrarMovimiento(idItem, tipo, motivo, cantidad, observacion, idUsuario);
    }
}
