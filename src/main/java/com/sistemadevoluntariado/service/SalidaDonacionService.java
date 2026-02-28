package com.sistemadevoluntariado.service;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.SalidaDonacion;
import com.sistemadevoluntariado.repository.SalidaDonacionRepository;

@Service
public class SalidaDonacionService {

    private static final Logger logger = Logger.getLogger(SalidaDonacionService.class.getName());

    @Autowired
    private SalidaDonacionRepository salidaDonacionRepository;

    @Transactional
    public List<SalidaDonacion> listarTodos() {
        return salidaDonacionRepository.listar();
    }

    @Transactional
    public SalidaDonacion obtenerPorId(int id) {
        return salidaDonacionRepository.obtenerPorId(id);
    }

    @Transactional
    public boolean guardar(SalidaDonacion s) {
        try {
            return salidaDonacionRepository.guardar(s);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar salida de donacion", e);
            return false;
        }
    }

    @Transactional
    public boolean actualizar(SalidaDonacion s) {
        try {
            return salidaDonacionRepository.actualizar(s);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar salida de donacion", e);
            return false;
        }
    }

    @Transactional
    public boolean anular(int id, int idUsuario, String motivo) {
        try {
            return salidaDonacionRepository.anular(id, idUsuario, motivo);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al anular salida de donacion", e);
            return false;
        }
    }

    @Transactional
    public boolean cambiarEstado(int id, String estado) {
        try {
            return salidaDonacionRepository.cambiarEstado(id, estado);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cambiar estado de salida", e);
            return false;
        }
    }

    @Transactional
    public List<Map<String, Object>> listarDonacionesDisponibles() {
        return salidaDonacionRepository.listarDonacionesDisponibles();
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public List<Map<String, Object>> buscarDonacionesDisponibles(String query) {
        return salidaDonacionRepository.buscarDonacionesDisponibles(query);
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public Map<String, Object> obtenerSaldoDisponible(int idDonacion) {
        return salidaDonacionRepository.obtenerSaldoDisponible(idDonacion);
    }
}
