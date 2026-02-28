package com.sistemadevoluntariado.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sistemadevoluntariado.entity.SalidaDonacion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class SalidaDonacionRepositoryImpl implements SalidaDonacionRepositoryCustom {

    private static final Logger logger = Logger.getLogger(SalidaDonacionRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<SalidaDonacion> listar() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listar_salidas_donaciones()")
                    .getResultList();
            List<SalidaDonacion> lista = new ArrayList<>();
            for (Object[] row : rows) lista.add(mapear(row));
            logger.info("Se listaron " + lista.size() + " salidas de donaciones");
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar salidas de donaciones", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public SalidaDonacion obtenerPorId(int id) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_obtener_salida_donacion(?1)")
                    .setParameter(1, id)
                    .getResultList();
            return rows.isEmpty() ? null : mapear(rows.get(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener salida de donacion por ID", e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean guardar(SalidaDonacion s) {
        try {
            Integer idItem = s.getIdItem() != null ? s.getIdItem() : 0;
            Double cantidadItem = s.getCantidadItem() != null ? s.getCantidadItem() : 0.0;
            String descripcion = s.getDescripcion() != null ? s.getDescripcion() : "";

            List<?> result = em.createNativeQuery(
                    "CALL sp_registrar_salida_donacion(?1,?2,?3,?4,?5,?6,?7,?8)")
                    .setParameter(1, s.getIdDonacion())
                    .setParameter(2, s.getIdActividad())
                    .setParameter(3, s.getTipoSalida())
                    .setParameter(4, s.getCantidad())
                    .setParameter(5, descripcion)
                    .setParameter(6, idItem)
                    .setParameter(7, cantidadItem)
                    .setParameter(8, s.getIdUsuarioRegistro())
                    .getResultList();
            if (!result.isEmpty()) {
                Number idGenerado = (Number) result.get(0);
                s.setIdSalida(idGenerado.intValue());
            }
            logger.info("✓ Salida de donacion registrada con ID: " + s.getIdSalida());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar salida de donacion", e);
            return false;
        }
    }

    @Override
    public boolean actualizar(SalidaDonacion s) {
        try {
            Integer idItem = s.getIdItem() != null ? s.getIdItem() : 0;
            Double cantidadItem = s.getCantidadItem() != null ? s.getCantidadItem() : 0.0;
            String descripcion = s.getDescripcion() != null ? s.getDescripcion() : "";

            em.createNativeQuery("CALL sp_actualizar_salida_donacion(?1,?2,?3,?4,?5,?6)")
                    .setParameter(1, s.getIdSalida())
                    .setParameter(2, s.getIdActividad())
                    .setParameter(3, s.getCantidad())
                    .setParameter(4, descripcion)
                    .setParameter(5, idItem)
                    .setParameter(6, cantidadItem)
                    .executeUpdate();
            logger.info("✓ Salida de donacion actualizada ID: " + s.getIdSalida());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar salida de donacion", e);
            return false;
        }
    }

    @Override
    public boolean anular(int id, int idUsuario, String motivo) {
        try {
            em.createNativeQuery("{CALL sp_anular_salida_donacion(?,?,?)}")
                    .setParameter(1, id)
                    .setParameter(2, idUsuario)
                    .setParameter(3, motivo)
                    .executeUpdate();
            logger.info("✓ Salida de donacion anulada ID: " + id);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al anular salida de donacion", e);
            return false;
        }
    }

    @Override
    public boolean cambiarEstado(int id, String estado) {
        try {
            Object result = em.createNativeQuery("CALL sp_cambiar_estado_salida(?1, ?2)")
                    .setParameter(1, id)
                    .setParameter(2, estado)
                    .getSingleResult();
            return ((Number) result).intValue() > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cambiar estado de salida", e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarDonacionesDisponibles() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listar_donaciones_disponibles()")
                    .getResultList();
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("idDonacion", row[0]);
                map.put("cantidad", row[1]);
                map.put("descripcion", row[2] != null ? row[2].toString() : "");
                map.put("tipoDonacion", row[3] != null ? row[3].toString() : "");
                map.put("actividadOrigen", row[4] != null ? row[4].toString() : "");
                map.put("donante", row[5] != null ? row[5].toString() : "ANÓNIMO");
                map.put("estado", row[6] != null ? row[6].toString() : "");
                map.put("idTipoDonacion", row[7]);
                lista.add(map);
            }
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar donaciones disponibles", e);
            return List.of();
        }
    }

    // ── HELPERS ─────────────────────────────────────────────

    private SalidaDonacion mapear(Object[] row) {
        SalidaDonacion s = new SalidaDonacion();
        s.setIdSalida(toInt(row[0], 0));
        s.setIdDonacion(toInt(row[1], 0));
        s.setIdActividad(toInt(row[2], 0));
        s.setTipoSalida(row[3] != null ? row[3].toString() : null);
        s.setCantidad(toDouble(row[4], 0.0));
        s.setDescripcion(row[5] != null ? row[5].toString() : null);
        s.setIdItem(row[6] != null ? toInt(row[6], null) : null);
        s.setCantidadItem(row[7] != null ? toDouble(row[7], null) : null);
        s.setIdUsuarioRegistro(toInt(row[8], 0));
        s.setRegistradoEn(row[9] != null ? row[9].toString() : null);
        s.setEstado(row[10] != null ? row[10].toString() : null);
        s.setDonacionCantidad(toDouble(row[11], 0.0));
        s.setTipoDonacionNombre(row[12] != null ? row[12].toString() : null);
        s.setActividadNombre(row[13] != null ? row[13].toString() : null);
        s.setUsuarioRegistro(row[14] != null ? row[14].toString() : null);
        s.setItemNombre(row[15] != null ? row[15].toString() : null);
        s.setItemUnidadMedida(row[16] != null ? row[16].toString() : null);
        s.setDonanteNombre(row[17] != null ? row[17].toString() : null);
        s.setMotivoAnulacion(row[18] != null ? row[18].toString() : null);
        s.setAnuladoEn(row[19] != null ? row[19].toString() : null);
        s.setDonacionDescripcion(row[20] != null ? row[20].toString() : null);
        return s;
    }

    private Integer toInt(Object value, Integer defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString().trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private Double toDouble(Object value, Double defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString().trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> buscarDonacionesDisponibles(String query) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_buscar_donaciones_disponibles(?1)")
                    .setParameter(1, query != null ? query : "")
                    .getResultList();
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", toInt(row[0], 0));
                map.put("cantidadOriginal", toDouble(row[1], 0.0));
                map.put("saldoDisponible", toDouble(row[2], 0.0));
                map.put("descripcion", row[3] != null ? row[3].toString() : "");
                map.put("tipoDonacion", row[4] != null ? row[4].toString() : "DINERO");
                map.put("idTipoDonacion", toInt(row[5], 0));
                map.put("actividadOrigen", row[6] != null ? row[6].toString() : "");
                map.put("donante", row[7] != null ? row[7].toString() : "ANÓNIMO");
                lista.add(map);
            }
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al buscar donaciones disponibles", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerSaldoDisponible(int idDonacion) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_obtener_saldo_donacion(?1)")
                    .setParameter(1, idDonacion)
                    .getResultList();
            if (rows.isEmpty()) return Map.of("saldoDisponible", 0.0);
            Object[] row = rows.get(0);
            Map<String, Object> map = new HashMap<>();
            map.put("cantidadOriginal", toDouble(row[0], 0.0));
            map.put("saldoDisponible", toDouble(row[1], 0.0));
            map.put("tipoDonacion", row[2] != null ? row[2].toString() : "DINERO");
            return map;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener saldo de donación", e);
            return Map.of("saldoDisponible", 0.0);
        }
    }
}
