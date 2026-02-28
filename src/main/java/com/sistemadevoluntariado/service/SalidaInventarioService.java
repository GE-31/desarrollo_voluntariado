package com.sistemadevoluntariado.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.InventarioItem;
import com.sistemadevoluntariado.entity.SalidaInventario;
import com.sistemadevoluntariado.entity.SalidaInventarioDetalle;
import com.sistemadevoluntariado.repository.SalidaInventarioRepository;

@Service
public class SalidaInventarioService {

    private static final Logger logger = Logger.getLogger(SalidaInventarioService.class.getName());

    @Autowired
    private SalidaInventarioRepository salidaInventarioRepository;

    @Transactional(readOnly = true)
    public List<SalidaInventario> listarTodos() {
        return salidaInventarioRepository.listarTodos();
    }

    @Transactional(readOnly = true)
    public SalidaInventario obtenerPorId(int id) {
        return salidaInventarioRepository.obtenerPorId(id);
    }

    @Transactional(readOnly = true)
    public List<SalidaInventarioDetalle> obtenerDetalle(int idSalidaInv) {
        return salidaInventarioRepository.obtenerDetalle(idSalidaInv);
    }

    @Transactional(readOnly = true)
    public List<InventarioItem> listarItemsDisponibles() {
        return salidaInventarioRepository.listarItemsDisponibles();
    }

    /**
     * Registra una salida completa: cabecera + detalles (carrito).
     * Retorna el ID de la salida creada, o -1 si falla.
     */
    @Transactional
    public int registrarSalidaCompleta(SalidaInventario cabecera, List<SalidaInventarioDetalle> detalles) {
        try {
            // 1. Registrar cabecera
            int idSalida = salidaInventarioRepository.registrarCabecera(cabecera);
            if (idSalida <= 0) {
                throw new RuntimeException("No se pudo crear la cabecera de salida de inventario");
            }

            // 2. Registrar cada detalle (descontando stock)
            for (SalidaInventarioDetalle det : detalles) {
                salidaInventarioRepository.registrarDetalle(idSalida, det.getIdItem(), det.getCantidad());
            }

            logger.info("✓ Salida de inventario #" + idSalida + " registrada con " + detalles.size() + " items");
            return idSalida;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al registrar salida completa de inventario", e);
            throw new RuntimeException("Error al registrar salida de inventario: " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean anular(int idSalidaInv, int idUsuario, String motivo) {
        try {
            return salidaInventarioRepository.anular(idSalidaInv, idUsuario, motivo);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al anular salida de inventario", e);
            throw new RuntimeException("Error al anular salida de inventario: " + e.getMessage(), e);
        }
    }
}
