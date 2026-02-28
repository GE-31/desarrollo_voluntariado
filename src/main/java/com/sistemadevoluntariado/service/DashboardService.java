package com.sistemadevoluntariado.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.repository.DashboardRepository;

@Service
public class DashboardService {

    @Autowired
    private DashboardRepository dashboardRepository;

    /**
     * Recopila todas las estadísticas del dashboard vía SP.
     */
    @Transactional
    public Map<String, Object> obtenerEstadisticas() {
        return dashboardRepository.obtenerEstadisticas();
    }

    @Transactional
    public Map<String, Object> obtenerActividadesPorMes() {
        return dashboardRepository.obtenerActividadesPorMes();
    }

    @Transactional
    public Map<String, Object> obtenerHorasVoluntariasPorActividad() {
        return dashboardRepository.obtenerHorasVoluntariasPorActividad();
    }

    @Transactional
    public double obtenerTotalHorasVoluntarias() {
        return dashboardRepository.obtenerTotalHorasVoluntarias();
    }

    @Transactional
    public Map<String, String> obtenerProximaActividad() {
        return dashboardRepository.obtenerProximaActividad();
    }
}
