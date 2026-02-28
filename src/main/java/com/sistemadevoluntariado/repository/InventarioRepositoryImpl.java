package com.sistemadevoluntariado.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sistemadevoluntariado.entity.InventarioItem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class InventarioRepositoryImpl implements InventarioRepositoryCustom {

    private static final Logger logger = Logger.getLogger(InventarioRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<InventarioItem> listar() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listar_inventario()")
                    .getResultList();
            List<InventarioItem> lista = new ArrayList<>();
            for (Object[] row : rows) lista.add(mapear(row));
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar inventario", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public InventarioItem obtenerPorId(int idItem) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_obtener_item_inventario(?1)")
                    .setParameter(1, idItem)
                    .getResultList();
            return rows.isEmpty() ? null : mapear(rows.get(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener item inventario", e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public int registrar(InventarioItem item) {
        try {
            List<?> rows = em.createNativeQuery("CALL sp_crear_item_inventario(?1, ?2, ?3, ?4, ?5)")
                    .setParameter(1, item.getNombre())
                    .setParameter(2, item.getCategoria())
                    .setParameter(3, item.getUnidadMedida())
                    .setParameter(4, item.getStockMinimo())
                    .setParameter(5, item.getObservacion())
                    .getResultList();
            if (rows == null || rows.isEmpty()) {
                return -1;
            }
            Object first = rows.get(0);
            if (first instanceof Object[]) {
                Object id = ((Object[]) first)[0];
                return id instanceof Number ? ((Number) id).intValue() : Integer.parseInt(String.valueOf(id));
            }
            return first instanceof Number ? ((Number) first).intValue() : Integer.parseInt(String.valueOf(first));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al registrar item inventario", e);
            return -1;
        }
    }

    @Override
    public boolean actualizar(InventarioItem item) {
        try {
            Object resultado = em.createNativeQuery("CALL sp_actualizar_item_inventario(?1, ?2, ?3, ?4, ?5, ?6)")
                    .setParameter(1, item.getIdItem())
                    .setParameter(2, item.getNombre())
                    .setParameter(3, item.getCategoria())
                    .setParameter(4, item.getUnidadMedida())
                    .setParameter(5, item.getStockMinimo())
                    .setParameter(6, item.getObservacion())
                    .getSingleResult();
            return ((Number) resultado).intValue() > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar item inventario", e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<InventarioItem> filtrar(String q, String categoria, String estado, boolean stockBajo) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_filtrar_inventario(?1, ?2, ?3, ?4)")
                    .setParameter(1, q != null && !q.isEmpty() ? q : null)
                    .setParameter(2, categoria != null && !categoria.isEmpty() ? categoria : null)
                    .setParameter(3, estado != null && !estado.isEmpty() ? estado : null)
                    .setParameter(4, stockBajo ? 1 : 0)
                    .getResultList();
            List<InventarioItem> lista = new ArrayList<>();
            for (Object[] row : rows) lista.add(mapear(row));
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al filtrar inventario", e);
            return List.of();
        }
    }

    @Override
    public boolean registrarMovimiento(int idItem, String tipo, String motivo, double cantidad, String observacion, int idUsuario) {
        try {
            em.createNativeQuery("CALL sp_registrar_movimiento_inventario(?1, ?2, ?3, ?4, ?5, ?6)")
                    .setParameter(1, idItem)
                    .setParameter(2, tipo)
                    .setParameter(3, motivo)
                    .setParameter(4, cantidad)
                    .setParameter(5, observacion)
                    .setParameter(6, idUsuario)
                    .getSingleResult();
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al registrar movimiento inventario", e);
            return false;
        }
    }

    @Override
    public boolean cambiarEstadoItem(int idItem, String estado) {
        try {
            Object resultado = em.createNativeQuery("CALL sp_cambiar_estado_inventario(?1, ?2)")
                    .setParameter(1, idItem)
                    .setParameter(2, estado != null ? estado : "INACTIVO")
                    .getSingleResult();
            return ((Number) resultado).intValue() > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cambiar estado inventario", e);
            return false;
        }
    }

    @Override
    public int contarStockBajo() {
        try {
            Object resultado = em.createNativeQuery("CALL sp_contar_stock_bajo()")
                    .getSingleResult();
            return ((Number) resultado).intValue();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al contar stock bajo", e);
            return 0;
        }
    }

    // ── HELPER ──────────────────────────────────────────────

    private InventarioItem mapear(Object[] row) {
        InventarioItem item = new InventarioItem();
        item.setIdItem(row[0] != null ? ((Number) row[0]).intValue() : 0);
        item.setNombre((String) row[1]);
        item.setCategoria((String) row[2]);
        item.setUnidadMedida((String) row[3]);
        item.setStockActual(row[4] != null ? ((Number) row[4]).doubleValue() : 0.0);
        item.setStockMinimo(row[5] != null ? ((Number) row[5]).doubleValue() : 0.0);
        item.setEstado((String) row[6]);
        item.setObservacion((String) row[7]);
        item.setCreadoEn(row[8] != null ? row[8].toString() : null);
        item.setActualizadoEn(row[9] != null ? row[9].toString() : null);
        return item;
    }
}
