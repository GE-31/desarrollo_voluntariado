package com.sistemadevoluntariado.repository;

import java.util.ArrayList;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
            // Se lista incluyendo estados CONFIRMADO y ANULADO para permitir filtrado en UI.
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT " +
                    "s.id_salida, s.id_donacion, s.id_actividad, s.tipo_salida, s.cantidad, s.descripcion, " +
                    "s.id_item, s.cantidad_item, s.id_usuario_registro, s.registrado_en, s.estado, " +
                    "d.cantidad AS donacion_cantidad, td.nombre AS tipo_donacion_nombre, " +
                    "a.nombre AS actividad_nombre, " +
                    "CONCAT(u.nombres, ' ', u.apellidos) AS usuario_registro, " +
                    "ii.nombre AS item_nombre, ii.unidad_medida AS item_unidad_medida, " +
                    "COALESCE(dn.nombre, 'ANONIMO') AS donante_nombre, " +
                    "s.motivo_anulacion, s.anulado_en, d.descripcion AS donacion_descripcion " +
                    "FROM salida_donacion s " +
                    "INNER JOIN donacion d ON d.id_donacion = s.id_donacion " +
                    "INNER JOIN tipo_donacion td ON td.id_tipo_donacion = d.id_tipo_donacion " +
                    "INNER JOIN actividades a ON a.id_actividad = s.id_actividad " +
                    "INNER JOIN usuario u ON u.id_usuario = s.id_usuario_registro " +
                    "LEFT JOIN inventario_item ii ON ii.id_item = s.id_item " +
                    "LEFT JOIN donacion_donante dd ON dd.id_donacion = d.id_donacion " +
                    "LEFT JOIN donante dn ON dn.id_donante = dd.id_donante " +
                    "WHERE s.estado IN ('CONFIRMADO', 'ANULADO') " +
                    "ORDER BY s.registrado_en DESC")
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
            logger.info("âœ“ Salida de donacion registrada con ID: " + s.getIdSalida());
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
            logger.info("âœ“ Salida de donacion actualizada ID: " + s.getIdSalida());
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
            logger.info("âœ“ Salida de donacion anulada ID: " + id);
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
                map.put("descripcion", limpiarTexto(row[2], ""));
                map.put("tipoDonacion", limpiarTexto(row[3], ""));
                map.put("actividadOrigen", limpiarTexto(row[4], ""));
                map.put("donante", limpiarTexto(row[5], "ANONIMO"));
                map.put("estado", limpiarTexto(row[6], ""));
                map.put("idTipoDonacion", row[7]);
                lista.add(map);
            }
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar donaciones disponibles", e);
            return List.of();
        }
    }

    // â”€â”€ HELPERS â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private SalidaDonacion mapear(Object[] row) {
        SalidaDonacion s = new SalidaDonacion();
        s.setIdSalida(toInt(row[0], 0));
        s.setIdDonacion(toInt(row[1], 0));
        s.setIdActividad(toInt(row[2], 0));
        s.setTipoSalida(limpiarTexto(row[3], null));
        s.setCantidad(toDouble(row[4], 0.0));
        s.setDescripcion(limpiarTexto(row[5], null));
        s.setIdItem(row[6] != null ? toInt(row[6], null) : null);
        s.setCantidadItem(row[7] != null ? toDouble(row[7], null) : null);
        s.setIdUsuarioRegistro(toInt(row[8], 0));
        s.setRegistradoEn(limpiarTexto(row[9], null));
        s.setEstado(limpiarTexto(row[10], null));
        s.setDonacionCantidad(toDouble(row[11], 0.0));
        s.setTipoDonacionNombre(limpiarTexto(row[12], null));
        s.setActividadNombre(limpiarTexto(row[13], null));
        s.setUsuarioRegistro(limpiarTexto(row[14], null));
        s.setItemNombre(limpiarTexto(row[15], null));
        s.setItemUnidadMedida(limpiarTexto(row[16], null));
        s.setDonanteNombre(limpiarTexto(row[17], "ANONIMO"));
        s.setMotivoAnulacion(limpiarTexto(row[18], null));
        s.setAnuladoEn(limpiarTexto(row[19], null));
        s.setDonacionDescripcion(limpiarTexto(row[20], null));
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
                map.put("descripcion", limpiarTexto(row[3], ""));
                map.put("tipoDonacion", limpiarTexto(row[4], "DINERO"));
                map.put("idTipoDonacion", toInt(row[5], 0));
                map.put("actividadOrigen", limpiarTexto(row[6], ""));
                map.put("donante", limpiarTexto(row[7], "ANONIMO"));
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
            map.put("tipoDonacion", limpiarTexto(row[2], "DINERO"));
            return map;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener saldo de donaciÃ³n", e);
            return Map.of("saldoDisponible", 0.0);
        }
    }

    private String limpiarTexto(Object value, String fallback) {
        String text = value != null ? value.toString() : fallback;
        if (text == null) return null;

        String normalized = normalizarMojibake(text.trim());
        if (normalized.isEmpty() && fallback != null) return fallback;
        if (esAnonimo(normalized)) return "ANONIMO";
        return normalized;
    }

    private String normalizarMojibake(String text) {
        if (text == null || text.isEmpty()) return "";
        if (!contienePatronRoto(text)) return text;

        String isoDecoded = new String(text.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        if (conteoPatronesRotos(isoDecoded) < conteoPatronesRotos(text)) {
            return isoDecoded;
        }

        String cp1252Decoded = new String(text.getBytes(Charset.forName("Windows-1252")), StandardCharsets.UTF_8);
        if (conteoPatronesRotos(cp1252Decoded) < conteoPatronesRotos(text)) {
            return cp1252Decoded;
        }

        return text;
    }

    private boolean contienePatronRoto(String text) {
        return text.indexOf('\u00C3') >= 0
                || text.indexOf('\u00C2') >= 0
                || text.indexOf('\u251C') >= 0
                || text.indexOf('\uFFFD') >= 0;
    }

    private int conteoPatronesRotos(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00C3' || c == '\u00C2' || c == '\u251C' || c == '\uFFFD') {
                count++;
            }
        }
        return count;
    }

    private boolean esAnonimo(String text) {
        String upper = text.toUpperCase();
        return upper.contains("ANONIMO")
                || (upper.startsWith("AN") && upper.contains("NIMO"));
    }
}

