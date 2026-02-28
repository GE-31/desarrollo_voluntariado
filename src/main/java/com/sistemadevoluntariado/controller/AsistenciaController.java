package com.sistemadevoluntariado.controller;

import java.util.List;
import java.util.Map;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sistemadevoluntariado.entity.Asistencia;
import com.sistemadevoluntariado.entity.Participacion;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.ActividadService;
import com.sistemadevoluntariado.service.AsistenciaService;
import com.sistemadevoluntariado.service.ParticipacionService;
import com.sistemadevoluntariado.service.VoluntarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/asistencias")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private VoluntarioService voluntarioService;

    @Autowired
    private ParticipacionService participacionService;

    /* ───── Vista principal ───── */
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        if (usuario == null) return "redirect:/login";
        model.addAttribute("usuario", usuario);
        model.addAttribute("actividades", actividadService.obtenerTodasActividades());
        return "views/asistencias/listar";
    }

    /* ───── REST: listar JSON ───── */
    @GetMapping(params = "action=listar")
    @ResponseBody
    public List<Asistencia> listar() {
        return asistenciaService.listarAsistencias();
    }

    /* ───── REST: obtener por ID ───── */
    @GetMapping(params = "action=obtener")
    @ResponseBody
    public Asistencia obtener(@RequestParam int id) {
        return asistenciaService.obtenerPorId(id);
    }

    /* ───── REST: por actividad ───── */
    @GetMapping(params = "action=porActividad")
    @ResponseBody
    public List<Asistencia> porActividad(@RequestParam("id_actividad") int idActividad) {
        return asistenciaService.listarPorActividad(idActividad);
    }

    /* ───── REST: por voluntario ───── */
    @GetMapping(params = "action=porVoluntario")
    @ResponseBody
    public List<Asistencia> porVoluntario(@RequestParam("id_voluntario") int idVoluntario) {
        return asistenciaService.listarPorVoluntario(idVoluntario);
    }

    /* ───── REST: participantes de una actividad ───── */
    @GetMapping(params = "action=participantes")
    @ResponseBody
    public List<Participacion> participantes(@RequestParam("id_actividad") int idActividad) {
        return participacionService.obtenerPorActividad(idActividad);
    }

    /* ───── REST: registrar ───── */
    @PostMapping(params = "action=registrar")
    @ResponseBody
    public Map<String, Object> registrar(@RequestParam("id_voluntario") int idVoluntario,
                                          @RequestParam("id_actividad") int idActividad,
                                          @RequestParam String fecha,
                                          @RequestParam(value = "hora_entrada", required = false) String horaEntrada,
                                          @RequestParam(value = "hora_salida", required = false) String horaSalida,
                                          @RequestParam String estado,
                                          @RequestParam(required = false) String observaciones,
                                          HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
            if (usuario == null) return Map.of("success", false, "message", "No autorizado");

            if (horaEntrada != null && horaEntrada.trim().isEmpty()) horaEntrada = null;
            if (horaSalida != null && horaSalida.trim().isEmpty()) horaSalida = null;
            if (observaciones != null && observaciones.trim().isEmpty()) observaciones = null;

            if (!actividadService.tieneCupoDisponible(idActividad)) {
                return Map.of("success", false, "message",
                    "No se puede registrar la asistencia. El cupo de esta actividad ya está lleno.");
            }

            Asistencia a = new Asistencia();
            a.setIdVoluntario(idVoluntario);
            a.setIdActividad(idActividad);
            a.setFecha(fecha);
            a.setHoraEntrada(horaEntrada);
            a.setHoraSalida(horaSalida);
            a.setEstado(estado);
            a.setObservaciones(observaciones);
            a.setIdUsuarioRegistro(usuario.getIdUsuario());

            boolean ok = asistenciaService.registrarAsistencia(a);
            return ok ? Map.of("success", true, "message", "Asistencia registrada correctamente")
                      : Map.of("success", false, "message",
                            "Este voluntario ya tiene asistencia registrada en esa actividad para la fecha seleccionada.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error interno: " + e.getMessage());
        }
    }

    /* ───── REST: actualizar ───── */
    @PostMapping(params = "action=actualizar")
    @ResponseBody
    public Map<String, Object> actualizar(@RequestParam int id,
                                           @RequestParam(value = "hora_entrada", required = false) String horaEntrada,
                                           @RequestParam(value = "hora_salida", required = false) String horaSalida,
                                           @RequestParam String estado,
                                           @RequestParam(required = false) String observaciones) {
        try {
            Asistencia a = new Asistencia();
            a.setIdAsistencia(id);
            a.setHoraEntrada(horaEntrada);
            a.setHoraSalida(horaSalida);
            a.setEstado(estado);
            a.setObservaciones(observaciones);

            boolean ok = asistenciaService.actualizarAsistencia(a);
            return ok ? Map.of("success", true, "message", "Asistencia actualizada correctamente")
                      : Map.of("success", false, "message", "Error al actualizar asistencia");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    /* ───── REST: eliminar ───── */
    @PostMapping(params = "action=eliminar")
    @ResponseBody
    public Map<String, Object> eliminar(@RequestParam int id) {
        boolean ok = asistenciaService.eliminarAsistencia(id);
        return ok ? Map.of("success", true, "message", "Asistencia eliminada correctamente")
                  : Map.of("success", false, "message", "Error al eliminar asistencia");
    }
}
