package com.sistemadevoluntariado.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Asistencia;
import com.sistemadevoluntariado.repository.AsistenciaRepository;

@Service
public class AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private ActividadService actividadService;

    @Transactional
    public List<Asistencia> listarAsistencias() {
        return asistenciaRepository.listarAsistencias();
    }

    @Transactional
    public Asistencia obtenerPorId(int id) {
        return asistenciaRepository.obtenerPorId(id);
    }

    @Transactional
    public List<Asistencia> listarPorActividad(int idActividad) {
        return asistenciaRepository.listarPorActividad(idActividad);
    }

    @Transactional
    public List<Asistencia> listarPorVoluntario(int idVoluntario) {
        return asistenciaRepository.listarPorVoluntario(idVoluntario);
    }

    @Transactional
    public BigDecimal obtenerHorasVoluntarioActividad(int idVoluntario, int idActividad) {
        return asistenciaRepository.obtenerHorasVoluntarioActividad(idVoluntario, idActividad);
    }

    /**
     * Registra asistencia validando cupo disponible y calcula horas.
     * Después actualiza conteo de inscritos.
     */
    @Transactional
    public boolean registrarAsistencia(Asistencia asistencia) {
        // Calcular horas automáticamente
        asistencia.setHorasTotales(calcularHoras(asistencia.getHoraEntrada(), asistencia.getHoraSalida()));

        // Validar cupo disponible
        if (!actividadService.tieneCupoDisponible(asistencia.getIdActividad())) {
            return false;
        }

        boolean resultado = asistenciaRepository.registrarAsistencia(asistencia);
        if (resultado) {
            actividadService.actualizarInscritosPorAsistencia(asistencia.getIdActividad());
        }
        return resultado;
    }

    @Transactional
    public boolean actualizarAsistencia(Asistencia asistencia) {
        // Obtener actividad asociada antes de actualizar
        Asistencia existente = asistenciaRepository.obtenerPorId(asistencia.getIdAsistencia());

        // Recalcular horas
        asistencia.setHorasTotales(calcularHoras(asistencia.getHoraEntrada(), asistencia.getHoraSalida()));

        boolean resultado = asistenciaRepository.actualizarAsistencia(asistencia);
        if (resultado && existente != null) {
            actividadService.actualizarInscritosPorAsistencia(existente.getIdActividad());
        }
        return resultado;
    }

    @Transactional
    public boolean eliminarAsistencia(int id) {
        Asistencia existente = asistenciaRepository.obtenerPorId(id);
        boolean resultado = asistenciaRepository.eliminarAsistencia(id);
        if (resultado && existente != null) {
            actividadService.actualizarInscritosPorAsistencia(existente.getIdActividad());
        }
        return resultado;
    }

    /**
     * Calcula la diferencia en horas entre hora_entrada y hora_salida (formato HH:mm o HH:mm:ss).
     */
    public BigDecimal calcularHoras(String horaEntrada, String horaSalida) {
        try {
            if (horaEntrada == null || horaEntrada.trim().isEmpty()
                    || horaSalida == null || horaSalida.trim().isEmpty()) {
                return BigDecimal.ZERO;
            }
            LocalTime entrada = LocalTime.parse(horaEntrada.length() == 5 ? horaEntrada + ":00" : horaEntrada);
            LocalTime salida = LocalTime.parse(horaSalida.length() == 5 ? horaSalida + ":00" : horaSalida);
            long minutos = ChronoUnit.MINUTES.between(entrada, salida);
            if (minutos <= 0) return BigDecimal.ZERO;
            return new BigDecimal(minutos).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
