package com.sistemadevoluntariado.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.sistemadevoluntariado.entity.Actividad;
import com.sistemadevoluntariado.entity.Certificado;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.entity.Voluntario;
import com.sistemadevoluntariado.service.ActividadService;
import com.sistemadevoluntariado.service.AsistenciaService;
import com.sistemadevoluntariado.service.CertificadoService;
import com.sistemadevoluntariado.service.VoluntarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/certificados")
public class CertificadoController {

    @Autowired
    private CertificadoService certificadoService;

    @Autowired
    private VoluntarioService voluntarioService;

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private AsistenciaService asistenciaService;

    /* ───── Vista principal ───── */
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        if (usuario == null) return "redirect:/login";
        model.addAttribute("usuario", usuario);
        model.addAttribute("certificados", certificadoService.obtenerTodosCertificados());
        model.addAttribute("voluntarios", voluntarioService.obtenerTodosVoluntarios());
        model.addAttribute("actividades", actividadService.obtenerTodasActividades());
        return "views/certificados/listar";
    }

    @GetMapping(params = "action=listar")
    @ResponseBody
    public List<Certificado> listar() {
        return certificadoService.obtenerTodosCertificados();
    }

    @GetMapping(params = "action=obtener")
    @ResponseBody
    public Map<String, Object> obtener(@RequestParam int id) {
        Certificado c = certificadoService.obtenerCertificadoPorId(id);
        if (c != null) return Map.of("success", true, "certificado", c);
        return Map.of("success", false, "message", "Certificado no encontrado");
    }

    @GetMapping(params = "action=verificar")
    @ResponseBody
    public Map<String, Object> verificar(@RequestParam String codigo) {
        Certificado c = certificadoService.obtenerCertificadoPorCodigo(codigo);
        if (c != null) return Map.of("valid", true, "certificado", c);
        return Map.of("valid", false, "message", "Certificado no encontrado");
    }

    @GetMapping(params = "action=voluntarios")
    @ResponseBody
    public List<Voluntario> listarVoluntarios() {
        return voluntarioService.obtenerVoluntariosConAsistencia();
    }

    @GetMapping(params = "action=actividades")
    @ResponseBody
    public List<Actividad> listarActividades() {
        return actividadService.obtenerTodasActividades();
    }

    @GetMapping(params = "action=voluntariosPorActividad")
    @ResponseBody
    public List<Map<String, Object>> voluntariosPorActividad(@RequestParam int idActividad) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        try {
            List<Voluntario> voluntarios = voluntarioService.obtenerVoluntariosConAsistencia();
            for (Voluntario v : voluntarios) {
                BigDecimal horas = asistenciaService.obtenerHorasVoluntarioActividad(v.getIdVoluntario(), idActividad);
                if (horas.compareTo(BigDecimal.ZERO) > 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("idVoluntario", v.getIdVoluntario());
                    item.put("nombres", v.getNombres());
                    item.put("apellidos", v.getApellidos());
                    item.put("dni", v.getDni());
                    item.put("horas", horas.intValue());
                    resultado.add(item);
                }
            }
        } catch (Exception e) {
            // retorna lista vacía
        }
        return resultado;
    }

    @GetMapping(params = "action=horasVoluntario")
    @ResponseBody
    public Map<String, Object> horasVoluntario(@RequestParam int idVoluntario,
                                                @RequestParam int idActividad) {
        try {
            BigDecimal horas = asistenciaService.obtenerHorasVoluntarioActividad(idVoluntario, idActividad);
            return Map.of("success", true, "horas", horas);
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error interno");
        }
    }

    @PostMapping(params = "action=crear")
    @ResponseBody
    public Map<String, Object> crear(@RequestParam int idVoluntario,
                                     @RequestParam int idActividad,
                                     @RequestParam(required = false) String observaciones,
                                     HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
            if (usuario == null) return Map.of("success", false, "message", "No autorizado");

            if (certificadoService.existeCertificadoActivo(idVoluntario, idActividad)) {
                return Map.of("success", false, "message",
                    "Este voluntario ya tiene un certificado emitido para esta actividad");
            }

            // Calcular horas automáticamente desde las asistencias
            BigDecimal horas = asistenciaService.obtenerHorasVoluntarioActividad(idVoluntario, idActividad);
            int horasVoluntariado = horas.intValue();
            if (horasVoluntariado <= 0) {
                return Map.of("success", false, "message",
                    "Este voluntario no tiene horas de asistencia registradas en esta actividad");
            }

            Certificado c = new Certificado();
            c.setIdVoluntario(idVoluntario);
            c.setIdActividad(idActividad);
            c.setHorasVoluntariado(horasVoluntariado);
            c.setObservaciones(observaciones);
            c.setIdUsuarioEmite(usuario.getIdUsuario());

            boolean ok = certificadoService.crearCertificado(c);
            return ok ? Map.of("success", true, "message", "Certificado emitido correctamente")
                      : Map.of("success", false, "message", "Error al emitir el certificado");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error interno: " + e.getMessage());
        }
    }

    @PostMapping(params = "action=anular")
    @ResponseBody
    public Map<String, Object> anular(@RequestParam int id,
                                      @RequestParam(required = false, defaultValue = "Sin motivo especificado") String motivo) {
        boolean ok = certificadoService.anularCertificado(id, motivo);
        return ok ? Map.of("success", true, "message", "Certificado anulado correctamente")
                  : Map.of("success", false, "message", "Error al anular el certificado");
    }
}
