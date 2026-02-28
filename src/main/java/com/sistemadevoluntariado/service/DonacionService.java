package com.sistemadevoluntariado.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Donacion;
import com.sistemadevoluntariado.entity.MovimientoFinanciero;
import com.sistemadevoluntariado.repository.DonacionRepository;
import com.sistemadevoluntariado.repository.InventarioRepository;
import com.sistemadevoluntariado.repository.TesoreriaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class DonacionService {

    private static final Logger logger = Logger.getLogger(DonacionService.class.getName());

    @Autowired
    private DonacionRepository donacionRepository;

    @Autowired
    private TesoreriaRepository tesoreriaRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public List<Donacion> listarTodos() {
        return donacionRepository.listar();
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public Donacion obtenerPorId(int id) {
        return donacionRepository.obtenerPorId(id);
    }

    /**
     * Buscar donantes registrados en la tabla donante por DNI, RUC o nombre (parcial).
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public List<Map<String, Object>> buscarDonantes(String termino) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        if (termino == null || termino.trim().isEmpty()) return resultado;
        String t = termino.trim();
        try {
            List<Object[]> rows = em.createNativeQuery(
                "SELECT id_donante, tipo, nombre, correo, telefono, dni, ruc " +
                "FROM donante " +
                "WHERE LOWER(nombre) LIKE LOWER(CONCAT('%', ?1, '%')) " +
                "   OR dni = ?2 " +
                "   OR ruc = ?2 " +
                "   OR telefono = ?2 " +
                "ORDER BY nombre ASC LIMIT 10")
                .setParameter(1, t)
                .setParameter(2, t)
                .getResultList();
            for (Object[] row : rows) {
                Map<String, Object> m = new HashMap<>();
                m.put("idDonante", row[0]);
                m.put("tipo", row[1] != null ? row[1].toString() : "Persona");
                m.put("nombre", row[2] != null ? row[2].toString() : "");
                m.put("correo", row[3] != null ? row[3].toString() : "");
                m.put("telefono", row[4] != null ? row[4].toString() : "");
                m.put("dni", row[5] != null ? row[5].toString() : "");
                m.put("ruc", row[6] != null ? row[6].toString() : "");
                resultado.add(m);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al buscar donantes", e);
        }
        return resultado;
    }

    @Transactional(noRollbackFor = Exception.class)
    public boolean guardar(Donacion d) {
        try {
            boolean ok = donacionRepository.guardar(d);
            // Si es donación monetaria y se guardó bien, registrar ingreso en Tesorería
            if (ok && d.getIdTipoDonacion() == 1 && d.getIdDonacion() > 0) {
                try {
                    registrarIngresoTesoreria(d.getIdDonacion(), d, d.getIdUsuarioRegistro());
                } catch (Exception te) {
                    logger.log(Level.WARNING,
                            "Error al registrar ingreso en tesorería para donación #" + d.getIdDonacion(), te);
                }
            }
            return ok;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar donación en servicio", e);
            return false;
        }
    }

    @Transactional(noRollbackFor = Exception.class)
    public boolean actualizar(Donacion d) {
        return donacionRepository.actualizar(d);
    }

    @Transactional(noRollbackFor = Exception.class)
    public boolean actualizarDetalleEspecie(int idDonacion, double cantidad, String descripcion) {
        return donacionRepository.actualizarDetalleEspecie(idDonacion, cantidad, descripcion);
    }

    @Transactional(noRollbackFor = Exception.class)
    public boolean anular(int idDonacion, int idUsuario, String motivo) {
        boolean ok = donacionRepository.anular(idDonacion, idUsuario, motivo);
        if (ok) {
            eliminarMovimientoTesoreria(idDonacion);
        }
        return ok;
    }

    @Transactional(noRollbackFor = Exception.class)
    public boolean cambiarEstado(int idDonacion, String estado) {
        return donacionRepository.cambiarEstado(idDonacion, estado);
    }

    /**
     * Ejecutar la integración Tesorería/Inventario DESPUÉS de cambiar el estado.
     * Se ejecuta en transacción SEPARADA para que un fallo aquí NO revierta
     * el cambio de estado ya comiteado.
     */
    @Transactional(noRollbackFor = Exception.class)
    public void ejecutarIntegracionPostCambio(Donacion antes, int idDonacion, String estado, int idUsuario) {
        if (antes == null) return;
        try {
            // Si pasa a CONFIRMADO y es monetaria → registrar ingreso en Tesorería
            if ("CONFIRMADO".equalsIgnoreCase(estado) && antes.getIdTipoDonacion() == 1) {
                registrarIngresoTesoreria(idDonacion, antes, idUsuario);
            }

            // Si pasa a CONFIRMADO y es en especie → registrar entrada en inventario
            if ("CONFIRMADO".equalsIgnoreCase(estado) && antes.getIdTipoDonacion() == 2
                    && antes.getIdItem() != null && antes.getIdItem() > 0) {
                double cantidad = antes.getCantidadItem() != null ? antes.getCantidadItem() : antes.getCantidad();
                inventarioRepository.registrarMovimiento(
                        antes.getIdItem(), "ENTRADA", "DONACION", cantidad,
                        "Donación en especie #" + idDonacion
                                + (antes.getDescripcion() != null ? " - " + antes.getDescripcion() : ""),
                        idUsuario);
            }

            // Si pasa a ANULADO/RECHAZADO → eliminar movimiento Tesorería
            if (("ANULADO".equalsIgnoreCase(estado) || "RECHAZADO".equalsIgnoreCase(estado))
                    && antes.getIdTipoDonacion() == 1) {
                eliminarMovimientoTesoreria(idDonacion);
            }

            // Si pasa a ANULADO/RECHAZADO en especie y estaba CONFIRMADO → registrar salida
            if (("ANULADO".equalsIgnoreCase(estado) || "RECHAZADO".equalsIgnoreCase(estado))
                    && antes.getIdTipoDonacion() == 2
                    && antes.getIdItem() != null && antes.getIdItem() > 0
                    && "CONFIRMADO".equalsIgnoreCase(antes.getEstado())) {
                double cantidad = antes.getCantidadItem() != null ? antes.getCantidadItem() : antes.getCantidad();
                inventarioRepository.registrarMovimiento(
                        antes.getIdItem(), "SALIDA", "ANULACION", cantidad,
                        "Reversión donación #" + idDonacion,
                        idUsuario);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error en integración Tesorería/Inventario (estado ya fue actualizado)", e);
        }
    }

    /**
     * Actualizar donación y sincronizar movimiento de Tesorería si aplica.
     */
    @Transactional(noRollbackFor = Exception.class)
    public boolean actualizarConTesoreria(Donacion d, Donacion actual) {
        boolean ok = donacionRepository.actualizar(d);
        boolean especiePendiente = actual.getIdTipoDonacion() == 2 && "PENDIENTE".equalsIgnoreCase(actual.getEstado());

        if (ok && especiePendiente && d.getCantidad() != null && d.getCantidad() > 0) {
            ok = donacionRepository.actualizarDetalleEspecie(d.getIdDonacion(), d.getCantidad(), d.getDescripcion());
        }

        // Si fue donación monetaria, actualizar movimiento en Tesorería
        if (ok && actual.getIdTipoDonacion() == 1) {
            actualizarMovimientoTesoreria(d, actual);
        }

        return ok;
    }

    // ── Métodos privados de integración ──

    private void registrarIngresoTesoreria(int idDonacion, Donacion antes, int idUsuario) {
        // Evitar duplicados
        List<MovimientoFinanciero> listado = tesoreriaRepository.filtrar("INGRESO", "Donaciones", null, null);
        for (MovimientoFinanciero mv : listado) {
            if (mv.getDescripcion() != null && mv.getDescripcion().contains("Donacion #" + idDonacion)) {
                return; // Ya existe
            }
        }

        // Usar INSERT nativo para no contaminar la sesión de Hibernate.
        // JPA save() agrega al persistence context y si el flush falla, se marca
        // rollback-only y se pierde la actualización del estado de la donación.
        String desc = "Donación" + (antes.getDescripcion() != null && !antes.getDescripcion().isEmpty()
                ? ": " + antes.getDescripcion()
                : "") + " (Donacion #" + idDonacion + ")";
        String comprobante = resolverComprobante(antes, idDonacion);
        double monto = antes.getCantidad() != null ? antes.getCantidad() : 0d;
        em.createNativeQuery(
                "INSERT INTO movimiento_financiero (tipo, monto, descripcion, categoria, comprobante, fecha_movimiento, id_actividad, id_usuario) "
                        +
                        "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)")
                .setParameter(1, "INGRESO")
                .setParameter(2, monto)
                .setParameter(3, desc)
                .setParameter(4, "Donaciones")
                .setParameter(5, comprobante)
                .setParameter(6, LocalDate.now().toString())
                .setParameter(7, antes.getIdActividad())
                .setParameter(8, idUsuario)
                .executeUpdate();
    }

    private void eliminarMovimientoTesoreria(int idDonacion) {
        try {
            List<MovimientoFinanciero> listado = tesoreriaRepository.filtrar("INGRESO", "Donaciones", null, null);
            for (MovimientoFinanciero mv : listado) {
                if (mv.getDescripcion() != null && mv.getDescripcion().contains("Donacion #" + idDonacion)) {
                    tesoreriaRepository.deleteById(mv.getIdMovimiento());
                    break;
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al eliminar movimiento tesorería", e);
        }
    }

    private void actualizarMovimientoTesoreria(Donacion d, Donacion actual) {
        try {
            List<MovimientoFinanciero> listado = tesoreriaRepository.filtrar("INGRESO", "Donaciones", null, null);
            for (MovimientoFinanciero mv : listado) {
                if (mv.getDescripcion() != null && mv.getDescripcion().contains("Donacion #" + d.getIdDonacion())) {
                    mv.setMonto(d.getCantidad() != null ? d.getCantidad() : mv.getMonto());
                    mv.setDescripcion("Donación" + (d.getDescripcion() != null && !d.getDescripcion().isEmpty()
                            ? ": " + d.getDescripcion()
                            : "") + " (Donacion #" + d.getIdDonacion() + ")");
                    mv.setCategoria("Donaciones");
                    mv.setIdActividad(d.getIdActividad());
                    mv.setComprobante(resolverComprobante(d, d.getIdDonacion()));
                    tesoreriaRepository.save(mv);
                    break;
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al actualizar movimiento tesorería", e);
        }
    }

    private String resolverComprobante(Donacion d, int idDonacion) {
        String tipoDon = d.getTipoDonante() != null ? d.getTipoDonante().toUpperCase() : null;
        if (d.isDonacionAnonima() || tipoDon == null || "PERSONA".equals(tipoDon)) {
            return "BOLETA-" + idDonacion;
        } else if ("EMPRESA".equals(tipoDon) || "GRUPO".equals(tipoDon)) {
            return (d.getRucDonante() != null && !d.getRucDonante().isEmpty()) ? d.getRucDonante() : null;
        }
        return null;
    }
}
