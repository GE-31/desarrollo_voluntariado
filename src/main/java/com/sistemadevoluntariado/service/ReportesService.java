package com.sistemadevoluntariado.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Actividad;
import com.sistemadevoluntariado.entity.Asistencia;
import com.sistemadevoluntariado.entity.Beneficiario;
import com.sistemadevoluntariado.entity.Donacion;
import com.sistemadevoluntariado.entity.InventarioItem;
import com.sistemadevoluntariado.entity.MovimientoFinanciero;
import com.sistemadevoluntariado.entity.Voluntario;

@Service
public class ReportesService {

    @Autowired private DonacionService donacionService;
    @Autowired private VoluntarioService voluntarioService;
    @Autowired private ActividadService actividadService;
    @Autowired private BeneficiarioService beneficiarioService;
    @Autowired private AsistenciaService asistenciaService;
    @Autowired private InventarioService inventarioService;
    @Autowired private TesoreriaService tesoreriaService;

    // ══════════════════════════════════════════════════════════
    //  RESUMEN GENERAL DEL SISTEMA
    // ══════════════════════════════════════════════════════════

    @Transactional
    public Map<String, Object> obtenerResumenGeneral() {
        Map<String, Object> resumen = new LinkedHashMap<>();

        // Voluntarios
        List<Voluntario> voluntarios = voluntarioService.obtenerTodosVoluntarios();
        long volActivos = voluntarios.stream().filter(v -> "ACTIVO".equalsIgnoreCase(v.getEstado())).count();
        resumen.put("totalVoluntarios", voluntarios.size());
        resumen.put("voluntariosActivos", volActivos);

        // Beneficiarios
        List<Beneficiario> beneficiarios = beneficiarioService.obtenerTodosBeneficiarios();
        long benActivos = beneficiarios.stream().filter(b -> "ACTIVO".equalsIgnoreCase(b.getEstado())).count();
        resumen.put("totalBeneficiarios", beneficiarios.size());
        resumen.put("beneficiariosActivos", benActivos);

        // Actividades
        List<Actividad> actividades = actividadService.obtenerTodasActividades();
        long actActivas = actividades.stream().filter(a -> "ACTIVO".equalsIgnoreCase(a.getEstado())).count();
        long actFinalizadas = actividades.stream().filter(a -> "FINALIZADO".equalsIgnoreCase(a.getEstado())).count();
        resumen.put("totalActividades", actividades.size());
        resumen.put("actividadesActivas", actActivas);
        resumen.put("actividadesFinalizadas", actFinalizadas);

        // Asistencias
        List<Asistencia> asistencias = asistenciaService.listarAsistencias();
        long totalAsistieron = asistencias.stream().filter(a -> "ASISTIO".equalsIgnoreCase(a.getEstado())).count();
        long totalTardanzas = asistencias.stream().filter(a -> "TARDANZA".equalsIgnoreCase(a.getEstado())).count();
        long totalFaltas = asistencias.stream().filter(a -> "FALTA".equalsIgnoreCase(a.getEstado())).count();
        double totalHoras = asistencias.stream()
                .filter(a -> a.getHorasTotales() != null)
                .mapToDouble(a -> a.getHorasTotales().doubleValue()).sum();
        resumen.put("totalAsistencias", asistencias.size());
        resumen.put("asistieron", totalAsistieron);
        resumen.put("tardanzas", totalTardanzas);
        resumen.put("faltas", totalFaltas);
        resumen.put("totalHorasVoluntarias", Math.round(totalHoras * 100.0) / 100.0);

        // Donaciones
        List<Donacion> donaciones = donacionService.listarTodos();
        long donConfirmadas = donaciones.stream()
                .filter(d -> "CONFIRMADO".equalsIgnoreCase(d.getEstado()) || "ACTIVO".equalsIgnoreCase(d.getEstado())).count();
        double totalDinero = donaciones.stream()
                .filter(d -> d.getIdTipoDonacion() == 1 && ("CONFIRMADO".equalsIgnoreCase(d.getEstado()) || "ACTIVO".equalsIgnoreCase(d.getEstado())))
                .mapToDouble(d -> d.getCantidad() != null ? d.getCantidad() : 0).sum();
        resumen.put("totalDonaciones", donaciones.size());
        resumen.put("donacionesConfirmadas", donConfirmadas);
        resumen.put("totalDonacionesDinero", Math.round(totalDinero * 100.0) / 100.0);

        // Inventario
        List<InventarioItem> items = inventarioService.listar();
        long itemsActivos = items.stream().filter(i -> "ACTIVO".equalsIgnoreCase(i.getEstado())).count();
        int stockBajo = inventarioService.contarStockBajo();
        resumen.put("totalItemsInventario", items.size());
        resumen.put("itemsActivos", itemsActivos);
        resumen.put("itemsStockBajo", stockBajo);

        // Tesorería - balance
        try {
            Map<String, Double> balance = tesoreriaService.obtenerBalance();
            resumen.put("totalIngresos", balance.getOrDefault("ingresos", 0.0));
            resumen.put("totalEgresos", balance.getOrDefault("egresos", 0.0));
            resumen.put("saldoActual", balance.getOrDefault("saldo", 0.0));
        } catch (Exception e) {
            resumen.put("totalIngresos", 0.0);
            resumen.put("totalEgresos", 0.0);
            resumen.put("saldoActual", 0.0);
        }

        return resumen;
    }

    // ══════════════════════════════════════════════════════════
    //  DATOS POR MÓDULO (para tablas)
    // ══════════════════════════════════════════════════════════

    @Transactional
    public List<Voluntario> listarVoluntarios() {
        return voluntarioService.obtenerTodosVoluntarios();
    }

    @Transactional
    public List<Actividad> listarActividades() {
        return actividadService.obtenerTodasActividades();
    }

    @Transactional
    public List<Beneficiario> listarBeneficiarios() {
        return beneficiarioService.obtenerTodosBeneficiarios();
    }

    @Transactional
    public List<Asistencia> listarAsistencias() {
        return asistenciaService.listarAsistencias();
    }

    @Transactional
    public List<Donacion> listarDonaciones() {
        return donacionService.listarTodos();
    }

    @Transactional
    public List<InventarioItem> listarInventario() {
        return inventarioService.listar();
    }

    @Transactional
    public List<MovimientoFinanciero> listarTesoreria() {
        return tesoreriaService.listar();
    }

    // ══════════════════════════════════════════════════════════
    //  FILTRAR DONACIONES (legado)
    // ══════════════════════════════════════════════════════════

    @Transactional
    public List<Donacion> filtrarDonaciones(String fechaInicio, String fechaFin,
                                              String tipoDonacion, String tipoDonante,
                                              boolean incluirAnuladas) {
        List<Donacion> lista = donacionService.listarTodos();

        if (fechaInicio == null || fechaInicio.isEmpty() || fechaFin == null || fechaFin.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(fechaInicio);
            endDate = LocalDate.parse(fechaFin);
        } catch (DateTimeParseException ex) {
            return Collections.emptyList();
        }

        Stream<Donacion> stream = lista.stream()
            .filter(d -> {
                String reg = d.getRegistradoEn();
                if (reg == null || reg.isEmpty()) return false;
                String datePart = reg.length() >= 10 ? reg.substring(0, 10) : reg;
                try {
                    LocalDate regDate = LocalDate.parse(datePart);
                    return (!regDate.isBefore(startDate)) && (!regDate.isAfter(endDate));
                } catch (DateTimeParseException e) {
                    return false;
                }
            });

        if (tipoDonacion != null && !tipoDonacion.isEmpty()) {
            stream = stream.filter(d -> String.valueOf(d.getIdTipoDonacion()).equals(tipoDonacion)
                    || (d.getTipoDonacion() != null && d.getTipoDonacion().equalsIgnoreCase(tipoDonacion)));
        }
        if (tipoDonante != null && !tipoDonante.isEmpty()) {
            final String tdon = tipoDonante.toUpperCase();
            stream = stream.filter(d -> d.getTipoDonante() != null && d.getTipoDonante().toUpperCase().equals(tdon));
        }

        stream = stream.filter(d -> {
            String s = d.getEstado();
            if (s == null || s.trim().isEmpty()) return false;
            s = s.toUpperCase();
            if ("ACTIVO".equals(s)) s = "CONFIRMADO";
            if (incluirAnuladas) {
                return "CONFIRMADO".equals(s) || "ANULADO".equals(s);
            } else {
                return "CONFIRMADO".equals(s);
            }
        });

        return stream.toList();
    }
}
