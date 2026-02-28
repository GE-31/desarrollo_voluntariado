package com.sistemadevoluntariado.service;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.MovimientoFinanciero;
import com.sistemadevoluntariado.repository.TesoreriaRepository;

@Service
public class TesoreriaService {

    private static final Logger logger = Logger.getLogger(TesoreriaService.class.getName());

    @Autowired
    private TesoreriaRepository tesoreriaRepository;

    @Transactional
    public List<MovimientoFinanciero> listar() {
        return tesoreriaRepository.listarConJoins();
    }

    @Transactional
    public MovimientoFinanciero obtenerPorId(int id) {
        return tesoreriaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Map<String, Double> obtenerBalance() {
        return tesoreriaRepository.obtenerBalance();
    }

    @Transactional
    public List<MovimientoFinanciero> filtrar(String tipo, String categoria, String fechaInicio, String fechaFin) {
        return tesoreriaRepository.filtrar(tipo, categoria, fechaInicio, fechaFin);
    }

    @Transactional
    public List<Map<String, Object>> resumenPorCategoria() {
        return tesoreriaRepository.resumenPorCategoria();
    }

    @Transactional
    public List<Map<String, Object>> resumenMensual() {
        return tesoreriaRepository.resumenMensual();
    }

    @Transactional
    public boolean registrar(MovimientoFinanciero m) {
        try {
            tesoreriaRepository.save(m);
            tesoreriaRepository.flush();
            logger.info("✓ Movimiento registrado ID: " + m.getIdMovimiento());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al registrar movimiento en tesorería", e);
            return false;
        }
    }

    @Transactional
    public boolean actualizar(MovimientoFinanciero m) {
        try {
            tesoreriaRepository.save(m);
            tesoreriaRepository.flush();
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar movimiento en tesorería", e);
            return false;
        }
    }

    @Transactional
    public boolean eliminar(int id) {
        try {
            tesoreriaRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al eliminar movimiento en tesorería", e);
            return false;
        }
    }
}
