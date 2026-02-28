package com.sistemadevoluntariado.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sistemadevoluntariado.entity.Donacion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class DonacionRepositoryImpl implements DonacionRepositoryCustom {

    private static final Logger logger = Logger.getLogger(DonacionRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<Donacion> listar() {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_listar_donaciones_con_detalle()")
                    .getResultList();
            List<Donacion> lista = new ArrayList<>();
            for (Object[] row : rows)
                lista.add(mapear(row));
            logger.info("Se listaron " + lista.size() + " donaciones");
            return lista;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar donaciones", e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Donacion obtenerPorId(int id) {
        try {
            List<Object[]> rows = em.createNativeQuery("CALL sp_obtener_donacion_detalle(?1)")
                    .setParameter(1, id)
                    .getResultList();
            return rows.isEmpty() ? null : mapear(rows.get(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener donacion por ID", e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean guardar(Donacion d) {
        try {
            // Normalizar nulls para evitar errores de tipo en Hibernate con native queries
            Integer idItem = d.getIdItem() != null ? d.getIdItem() : 0;
            Double stockMinimo = d.getItemStockMinimo() != null ? d.getItemStockMinimo() : 0.0;
            String itemNombre = d.getItemNombre() != null ? d.getItemNombre() : "";
            String itemCategoria = d.getItemCategoria() != null ? d.getItemCategoria() : "";
            String itemUnidadMedida = d.getItemUnidadMedida() != null ? d.getItemUnidadMedida() : "";
            String tipoDonante = d.getTipoDonante() != null ? d.getTipoDonante() : "";
            String nombreDonante = d.getNombreDonante() != null ? d.getNombreDonante() : "";
            String correoDonante = d.getCorreoDonante() != null ? d.getCorreoDonante() : "";
            String telefonoDonante = d.getTelefonoDonante() != null ? d.getTelefonoDonante() : "";
            String dniDonante = d.getDniDonante() != null ? d.getDniDonante() : "";
            String rucDonante = d.getRucDonante() != null ? d.getRucDonante() : "";
            String descripcion = d.getDescripcion() != null ? d.getDescripcion() : "";
            String subtipoDonacion = d.getSubtipoDonacion() != null ? d.getSubtipoDonacion() : "";

            List<?> result;
            try {
                // Version del SP con subtipo_donacion (19 parametros)
                result = em.createNativeQuery(
                        "CALL sp_registrar_donacion_inventario(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19)")
                        .setParameter(1, d.getCantidad())
                        .setParameter(2, descripcion)
                        .setParameter(3, d.getIdTipoDonacion())
                        .setParameter(4, subtipoDonacion)
                        .setParameter(5, d.getIdActividad())
                        .setParameter(6, d.getIdUsuarioRegistro())
                        .setParameter(7, idItem)
                        .setParameter(8, d.isCrearNuevoItem() ? 1 : 0)
                        .setParameter(9, itemNombre)
                        .setParameter(10, itemCategoria)
                        .setParameter(11, itemUnidadMedida)
                        .setParameter(12, stockMinimo)
                        .setParameter(13, d.isDonacionAnonima() ? 1 : 0)
                        .setParameter(14, tipoDonante)
                        .setParameter(15, nombreDonante)
                        .setParameter(16, correoDonante)
                        .setParameter(17, telefonoDonante)
                        .setParameter(18, dniDonante)
                        .setParameter(19, rucDonante)
                        .getResultList();
            } catch (Exception exVersionNueva) {
                logger.log(Level.WARNING,
                        "SP sp_registrar_donacion_inventario (19 params) no disponible. Reintentando version 18 params.",
                        exVersionNueva);
                // Version antigua del SP sin subtipo_donacion (18 parametros)
                result = em.createNativeQuery(
                        "CALL sp_registrar_donacion_inventario(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18)")
                        .setParameter(1, d.getCantidad())
                        .setParameter(2, descripcion)
                        .setParameter(3, d.getIdTipoDonacion())
                        .setParameter(4, d.getIdActividad())
                        .setParameter(5, d.getIdUsuarioRegistro())
                        .setParameter(6, idItem)
                        .setParameter(7, d.isCrearNuevoItem() ? 1 : 0)
                        .setParameter(8, itemNombre)
                        .setParameter(9, itemCategoria)
                        .setParameter(10, itemUnidadMedida)
                        .setParameter(11, stockMinimo)
                        .setParameter(12, d.isDonacionAnonima() ? 1 : 0)
                        .setParameter(13, tipoDonante)
                        .setParameter(14, nombreDonante)
                        .setParameter(15, correoDonante)
                        .setParameter(16, telefonoDonante)
                        .setParameter(17, dniDonante)
                        .setParameter(18, rucDonante)
                        .getResultList();
            }
            if (!result.isEmpty()) {
                Object first = result.get(0);
                if (first instanceof Object[]) {
                    d.setIdDonacion(toInt(((Object[]) first)[0], 0));
                } else {
                    d.setIdDonacion(toInt(first, 0));
                }
            }
            if (d.getIdDonacion() > 0 && d.getIdTipoDonacion() == 2 && idItem > 0) {
                asegurarDetalleEspecie(d.getIdDonacion(), idItem, d.getCantidad(), descripcion);
            }
            logger.info("âœ“ Donacion registrada con ID: " + d.getIdDonacion());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar donacion", e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean actualizar(Donacion d) {
        try {
            try {
                // Version nueva del SP (14 params, incluye subtipo_donacion)
                em.createNativeQuery(
                        "CALL sp_actualizar_donacion_inventario(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14)")
                        .setParameter(1, d.getIdDonacion())
                        .setParameter(2, d.getCantidad())
                        .setParameter(3, d.getDescripcion())
                        .setParameter(4, d.getSubtipoDonacion() != null ? d.getSubtipoDonacion() : "")
                        .setParameter(5, d.getIdActividad())
                        .setParameter(6, d.isDonacionAnonima() ? 1 : 0)
                        .setParameter(7, d.getTipoDonante())
                        .setParameter(8, d.getNombreDonante())
                        .setParameter(9, d.getCorreoDonante())
                        .setParameter(10, d.getTelefonoDonante())
                        .setParameter(11, d.getDniDonante())
                        .setParameter(12, d.getRucDonante())
                        .setParameter(13, 0)
                        .setParameter(14, (String) null)
                        .getResultList();
            } catch (Exception exVersionNueva) {
                logger.log(Level.WARNING,
                        "SP sp_actualizar_donacion_inventario (14 params) no disponible. Reintentando version 13 params.",
                        exVersionNueva);
                // Version anterior del SP (13 params, sin subtipo_donacion)
                em.createNativeQuery(
                        "CALL sp_actualizar_donacion_inventario(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13)")
                        .setParameter(1, d.getIdDonacion())
                        .setParameter(2, d.getCantidad())
                        .setParameter(3, d.getDescripcion())
                        .setParameter(4, d.getIdActividad())
                        .setParameter(5, d.isDonacionAnonima() ? 1 : 0)
                        .setParameter(6, d.getTipoDonante())
                        .setParameter(7, d.getNombreDonante())
                        .setParameter(8, d.getCorreoDonante())
                        .setParameter(9, d.getTelefonoDonante())
                        .setParameter(10, d.getDniDonante())
                        .setParameter(11, d.getRucDonante())
                        .setParameter(12, 0)
                        .setParameter(13, (String) null)
                        .getResultList();
            }
            logger.info("Donacion actualizada ID: " + d.getIdDonacion());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar donacion", e);
            return false;
        }
    }

    @Override
    public boolean actualizarPendienteEspecie(Donacion d) {
        try {
            int filas = em.createNativeQuery(
                    "UPDATE donacion " +
                    "SET cantidad = ?1, descripcion = ?2, subtipo_donacion = ?3, id_actividad = ?4, actualizado_en = NOW() " +
                    "WHERE id_donacion = ?5 AND UPPER(COALESCE(estado, 'PENDIENTE')) = 'PENDIENTE'")
                    .setParameter(1, d.getCantidad())
                    .setParameter(2, d.getDescripcion())
                    .setParameter(3, d.getSubtipoDonacion())
                    .setParameter(4, d.getIdActividad() > 0 ? d.getIdActividad() : null)
                    .setParameter(5, d.getIdDonacion())
                    .executeUpdate();
            if (filas <= 0) {
                return false;
            }
            upsertDetalleEspecie(d.getIdDonacion(), d.getCantidad(), d.getDescripcion());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar donacion pendiente en especie", e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean actualizarDetalleEspecie(int idDonacion, double cantidad, String observacion) {
        try {
            List<?> rows = em.createNativeQuery("CALL sp_actualizar_detalle_especie(?1, ?2, ?3)")
                    .setParameter(1, idDonacion)
                    .setParameter(2, cantidad)
                    .setParameter(3, observacion)
                    .getResultList();
            int filas = 0;
            if (rows != null && !rows.isEmpty()) {
                Object first = rows.get(0);
                if (first instanceof Object[]) {
                    filas = toInt(((Object[]) first)[0], 0);
                } else {
                    filas = toInt(first, 0);
                }
            }
            if (filas > 0) {
                return true;
            }
            Donacion actual = obtenerPorId(idDonacion);
            if (actual != null && actual.getIdItem() != null && actual.getIdItem() > 0) {
                asegurarDetalleEspecie(idDonacion, actual.getIdItem(), cantidad, observacion);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar detalle de donacion en especie", e);
            return false;
        }
    }

    @Override
    public boolean anular(int id, int idUsuario, String motivo) {
        try {
            em.createNativeQuery("{CALL sp_anular_donacion_inventario(?,?,?)}")
                    .setParameter(1, id)
                    .setParameter(2, idUsuario)
                    .setParameter(3, motivo)
                    .executeUpdate();
            logger.info("âœ“ Donacion anulada ID: " + id);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al anular donacion", e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean cambiarEstado(int id, String estado) {
        try {
            List<?> results = em.createNativeQuery("CALL sp_cambiar_estado_donacion(?1, ?2)")
                    .setParameter(1, id)
                    .setParameter(2, estado)
                    .getResultList();
            if (results.isEmpty()) {
                logger.warning("SP sp_cambiar_estado_donacion no retornÃ³ resultado para id=" + id);
                return false;
            }
            Object row = results.get(0);
            int filas;
            if (row instanceof Object[]) {
                filas = toInt(((Object[]) row)[0], 0);
            } else {
                filas = toInt(row, 0);
            }
            logger.info("cambiarEstado donacion #" + id + " a " + estado + " -> filas=" + filas);
            return filas > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cambiar estado de donacion id=" + id + " estado=" + estado, e);
            return false;
        }
    }

    // â”€â”€ HELPERS â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private Donacion mapear(Object[] row) {
        Donacion d = new Donacion();
        d.setIdDonacion(toInt(row[0], 0));
        d.setCantidad(toDouble(row[1], 0.0));
        d.setDescripcion(row[2] != null ? row[2].toString() : null);
        d.setIdTipoDonacion(toInt(row[3], 0));
        d.setIdActividad(toInt(row[4], 0));
        d.setIdUsuarioRegistro(toInt(row[5], 0));
        d.setRegistradoEn(row[6] != null ? row[6].toString() : null);
        d.setTipoDonacion(row[7] != null ? row[7].toString() : null);
        d.setActividad(row[8] != null ? row[8].toString() : null);
        d.setUsuarioRegistro(row[9] != null ? row[9].toString() : null);
        d.setDonanteNombre(row[10] != null ? row[10].toString() : null);
        d.setEstado(row[11] != null ? row[11].toString() : null);
        d.setTipoDonante(row[12] != null ? row[12].toString() : null);
        d.setIdItem(row[13] != null ? toInt(row[13], null) : null);
        d.setCantidadItem(row[14] != null ? toDouble(row[14], null) : null);
        d.setItemNombre(row[15] != null ? row[15].toString() : null);
        d.setItemUnidadMedida(row[16] != null ? row[16].toString() : null);
        d.setDniDonante(row[17] != null ? row[17].toString() : null);
        d.setRucDonante(row[18] != null ? row[18].toString() : null);
        d.setCorreoDonante(row[19] != null ? row[19].toString() : null);
        d.setTelefonoDonante(row[20] != null ? row[20].toString() : null);
        d.setSubtipoDonacion(row.length > 21 && row[21] != null ? row[21].toString() : null);
        return d;
    }

    /** Convierte cualquier Object (Number o String) a int de forma segura. */
    private Integer toInt(Object value, Integer defaultValue) {
        if (value == null)
            return defaultValue;
        if (value instanceof Number)
            return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Convierte cualquier Object (Number o String) a double de forma segura. */
    private Double toDouble(Object value, Double defaultValue) {
        if (value == null)
            return defaultValue;
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void asegurarDetalleEspecie(int idDonacion, int idItem, Double cantidad, String observacion) {
        try {
            Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM donacion_detalle WHERE id_donacion = ?1")
                    .setParameter(1, idDonacion)
                    .getSingleResult();
            if (count != null && count.intValue() > 0) {
                return;
            }

            double cant = cantidad != null ? cantidad : 0d;
            em.createNativeQuery(
                    "INSERT INTO donacion_detalle(id_donacion, id_item, cantidad, observacion, creado_en) " +
                    "VALUES(?1, ?2, ?3, ?4, NOW())")
                    .setParameter(1, idDonacion)
                    .setParameter(2, idItem)
                    .setParameter(3, cant)
                    .setParameter(4, observacion)
                    .executeUpdate();
        } catch (Exception e) {
            logger.log(Level.WARNING, "No se pudo asegurar detalle para donacion en especie #" + idDonacion, e);
        }
    }

    private void upsertDetalleEspecie(int idDonacion, Double cantidad, String observacion) {
        try {
            int updated = em.createNativeQuery(
                    "UPDATE donacion_detalle SET cantidad = ?1, observacion = ?2 WHERE id_donacion = ?3")
                    .setParameter(1, cantidad != null ? cantidad : 0d)
                    .setParameter(2, observacion)
                    .setParameter(3, idDonacion)
                    .executeUpdate();
            if (updated > 0) {
                return;
            }
            Donacion actual = obtenerPorId(idDonacion);
            if (actual != null && actual.getIdItem() != null && actual.getIdItem() > 0) {
                asegurarDetalleEspecie(idDonacion, actual.getIdItem(), cantidad, observacion);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "No se pudo hacer upsert de donacion_detalle para " + idDonacion, e);
        }
    }
}
