package com.sistemadevoluntariado.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.DashboardService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public String vista(Model model, HttpSession session) {
        if (session.getAttribute("usuarioLogeado") == null) {
            return "redirect:/login";
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        model.addAttribute("usuario", usuario);

        // Estadísticas generales
        Map<String, Object> stats = dashboardService.obtenerEstadisticas();
        model.addAttribute("totalVoluntarios", stats.get("totalVoluntarios"));
        model.addAttribute("voluntariosActivos", stats.get("voluntariosActivos"));
        model.addAttribute("voluntariosInactivos", stats.get("voluntariosInactivos"));
        model.addAttribute("totalActividades", stats.get("totalActividades"));
        model.addAttribute("totalDonaciones", stats.get("totalDonaciones"));
        model.addAttribute("montoDonaciones", stats.get("montoDonaciones"));
        model.addAttribute("totalBeneficiarios", stats.get("totalBeneficiarios"));

        // Gráficos
        Map<String, Object> actividadesPorMes = dashboardService.obtenerActividadesPorMes();
        model.addAttribute("actividadesPorMesLabels", actividadesPorMes.get("labels"));
        model.addAttribute("actividadesPorMesData", actividadesPorMes.get("data"));

        Map<String, Object> horasVoluntarias = dashboardService.obtenerHorasVoluntariasPorActividad();
        model.addAttribute("horasLabels", horasVoluntarias.get("labels"));
        model.addAttribute("horasData", horasVoluntarias.get("data"));

        model.addAttribute("totalHorasVoluntarias", dashboardService.obtenerTotalHorasVoluntarias());

        Map<String, String> proxima = dashboardService.obtenerProximaActividad();
        if (proxima != null) {
            model.addAttribute("proximaActividadNombre", proxima.get("nombre"));
            model.addAttribute("proximaActividadFecha", proxima.get("fecha"));
        }

        return "views/dashboard/dashboard";
    }
}
