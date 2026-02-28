package com.sistemadevoluntariado.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sistemadevoluntariado.entity.InventarioItem;
import com.sistemadevoluntariado.entity.SalidaInventario;
import com.sistemadevoluntariado.entity.SalidaInventarioDetalle;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class SalidaInventarioRepositoryImpl implements SalidaInventarioRepositoryCustom {

    private static final Logger logger = Logger.getLogger(SalidaInventarioRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<SalidaInventario> listarTodos() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listar_salidas_inventario()")
                    .getResultList();
            List<SalidaInventario> lista = new ArrayList<>();
            for (Object[] row : rows) lista.add(mapearCabecera(row));
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar salidas de inventario", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public SalidaInventario obtenerPorId(int idSalidaInv) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_obtener_salida_inventario(?1)")
                    .setParameter(1, idSalidaInv)
                    .getResultList();
            return rows.isEmpty() ? null : mapearCabeceraSimple(rows.get(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener salida de inventario", e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SalidaInventarioDetalle> obtenerDetalle(int idSalidaInv) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_obtener_salida_inventario_detalle(?1)")
                    .setParameter(1, idSalidaInv)
                    .getResultList();
            List<SalidaInventarioDetalle> lista = new ArrayList<>();
            for (Object[] row : rows) lista.add(mapearDetalle(row));
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener detalle de salida inventario", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public int registrarCabecera(SalidaInventario salida) {
        List<Object> results = em.createNativeQuery("CALL sp_registrar_salida_inventario(?1, ?2, ?3, ?4)")
                .setParameter(1, salida.getIdActividad() != null ? salida.getIdActividad() : 0)
                .setParameter(2, salida.getMotivo())
                .setParameter(3, salida.getObservacion())
                .setParameter(4, salida.getIdUsuarioRegistro())
                .getResultList();
        if (results.isEmpty()) {
            throw new RuntimeException("El procedimiento no devolvió un ID de salida");
        }
        return ((Number) results.get(0)).intValue();
    }

    @Override
    public boolean registrarDetalle(int idSalidaInv, int idItem, double cantidad) {
        em.createNativeQuery("CALL sp_registrar_salida_inventario_detalle(?1, ?2, ?3)")
                .setParameter(1, idSalidaInv)
                .setParameter(2, idItem)
                .setParameter(3, cantidad)
                .getResultList();
        return true;
    }

    @Override
    public boolean anular(int idSalidaInv, int idUsuario, String motivo) {
        em.createNativeQuery("CALL sp_anular_salida_inventario(?1, ?2, ?3)")
                .setParameter(1, idSalidaInv)
                .setParameter(2, idUsuario)
                .setParameter(3, motivo)
                .getResultList();
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<InventarioItem> listarItemsDisponibles() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listar_items_disponibles_salida()")
                    .getResultList();
            List<InventarioItem> lista = new ArrayList<>();
            for (Object[] row : rows) {
                InventarioItem item = new InventarioItem();
                item.setIdItem(row[0] != null ? ((Number) row[0]).intValue() : 0);
                item.setNombre((String) row[1]);
                item.setCategoria((String) row[2]);
                item.setUnidadMedida((String) row[3]);
                item.setStockActual(row[4] != null ? ((Number) row[4]).doubleValue() : 0.0);
                item.setEstado((String) row[5]);
                lista.add(item);
            }
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar items disponibles", e);
            return List.of();
        }
    }

    // ── HELPERS ──────────────────────────────────────────────

    private SalidaInventario mapearCabecera(Object[] row) {
        SalidaInventario s = new SalidaInventario();
        s.setIdSalidaInv(row[0] != null ? ((Number) row[0]).intValue() : 0);
        s.setIdActividad(row[1] != null ? ((Number) row[1]).intValue() : null);
        s.setActividadNombre(row[2] != null ? row[2].toString() : "Sin actividad");
        s.setMotivo(row[3] != null ? row[3].toString() : "");
        s.setObservacion(row[4] != null ? row[4].toString() : "");
        s.setIdUsuarioRegistro(row[5] != null ? ((Number) row[5]).intValue() : 0);
        s.setUsuarioRegistro(row[6] != null ? row[6].toString() : "");
        s.setRegistradoEn(row[7] != null ? row[7].toString() : "");
        s.setEstado(row[8] != null ? row[8].toString() : "CONFIRMADO");
        s.setAnuladoEn(row[9] != null ? row[9].toString() : null);
        s.setMotivoAnulacion(row[10] != null ? row[10].toString() : null);
        s.setTotalItems(row[11] != null ? ((Number) row[11]).intValue() : 0);
        s.setTotalCantidad(row[12] != null ? ((Number) row[12]).doubleValue() : 0.0);
        return s;
    }

    private SalidaInventario mapearCabeceraSimple(Object[] row) {
        SalidaInventario s = new SalidaInventario();
        s.setIdSalidaInv(row[0] != null ? ((Number) row[0]).intValue() : 0);
        s.setIdActividad(row[1] != null ? ((Number) row[1]).intValue() : null);
        s.setActividadNombre(row[2] != null ? row[2].toString() : "Sin actividad");
        s.setMotivo(row[3] != null ? row[3].toString() : "");
        s.setObservacion(row[4] != null ? row[4].toString() : "");
        s.setIdUsuarioRegistro(row[5] != null ? ((Number) row[5]).intValue() : 0);
        s.setUsuarioRegistro(row[6] != null ? row[6].toString() : "");
        s.setRegistradoEn(row[7] != null ? row[7].toString() : "");
        s.setEstado(row[8] != null ? row[8].toString() : "CONFIRMADO");
        s.setAnuladoEn(row[9] != null ? row[9].toString() : null);
        s.setMotivoAnulacion(row[10] != null ? row[10].toString() : null);
        return s;
    }

    private SalidaInventarioDetalle mapearDetalle(Object[] row) {
        SalidaInventarioDetalle d = new SalidaInventarioDetalle();
        d.setIdDetalle(row[0] != null ? ((Number) row[0]).intValue() : 0);
        d.setIdSalidaInv(row[1] != null ? ((Number) row[1]).intValue() : 0);
        d.setIdItem(row[2] != null ? ((Number) row[2]).intValue() : 0);
        d.setItemNombre(row[3] != null ? row[3].toString() : "");
        d.setItemCategoria(row[4] != null ? row[4].toString() : "");
        d.setItemUnidad(row[5] != null ? row[5].toString() : "");
        d.setCantidad(row[6] != null ? ((Number) row[6]).doubleValue() : 0.0);
        d.setStockAntes(row[7] != null ? ((Number) row[7]).doubleValue() : 0.0);
        d.setStockDespues(row[8] != null ? ((Number) row[8]).doubleValue() : 0.0);
        return d;
    }
}
