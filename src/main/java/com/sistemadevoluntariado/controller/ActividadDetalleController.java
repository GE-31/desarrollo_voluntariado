package com.sistemadevoluntariado.controller;

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

import com.sistemadevoluntariado.entity.ActividadBeneficiario;
import com.sistemadevoluntariado.entity.ActividadRecurso;
import com.sistemadevoluntariado.entity.Participacion;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.ActividadBeneficiarioService;
import com.sistemadevoluntariado.service.ActividadRecursoService;
import com.sistemadevoluntariado.service.ActividadService;
import com.sistemadevoluntariado.service.LugarService;
import com.sistemadevoluntariado.service.ParticipacionService;
import com.sistemadevoluntariado.service.RecursoService;

import jakarta.servlet.http.HttpSession;

/**
 * Controlador para la vista de detalle / gestión integral de una actividad.
 * Maneja: recursos requeridos, participación de voluntarios, beneficiarios vinculados, localidades.
 */
@Controller
@RequestMapping("/actividad-detalle")
public class ActividadDetalleController {

    @Autowired private ActividadService actividadService;
    @Autowired private LugarService lugarService;
    @Autowired private RecursoService recursoService;
    @Autowired private ActividadRecursoService actividadRecursoService;
    @Autowired private ParticipacionService participacionService;
    @Autowired private ActividadBeneficiarioService actividadBeneficiarioService;

    /* ═══════════════════════════════════════════
     *  VISTA DETALLE
     * ═══════════════════════════════════════════ */
    @GetMapping
    public String vista(@RequestParam int id, Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        if (usuario == null) return "redirect:/login";

        var actividad = actividadService.obtenerActividadPorId(id);
        if (actividad == null) return "redirect:/actividades";

        model.addAttribute("usuario", usuario);
        model.addAttribute("actividad", actividad);
        model.addAttribute("lugares", lugarService.obtenerTodos());
        model.addAttribute("recursos", recursoService.obtenerTodos());
        model.addAttribute("roles", participacionService.obtenerRoles());
        return "views/actividades/detalle";
    }

    /* ═══════════════════════════════════════════
     *  RECURSOS DE LA ACTIVIDAD
     * ═══════════════════════════════════════════ */
    @GetMapping(params = "action=recursos")
    @ResponseBody
    public List<ActividadRecurso> listarRecursos(@RequestParam int id) {
        return actividadRecursoService.obtenerPorActividad(id);
    }

    @PostMapping(params = "action=agregarRecurso")
    @ResponseBody
    public Map<String, Object> agregarRecurso(@RequestParam int idActividad,
                                               @RequestParam int idRecurso,
                                               @RequestParam double cantidadRequerida,
                                               @RequestParam(required = false, defaultValue = "MEDIA") String prioridad,
                                               @RequestParam(required = false) String observacion) {
        Map<String, Object> resp = new HashMap<>();
        try {
            ActividadRecurso ar = new ActividadRecurso();
            ar.setIdActividad(idActividad);
            ar.setIdRecurso(idRecurso);
            ar.setCantidadRequerida(cantidadRequerida);
            ar.setCantidadConseguida(0);
            ar.setPrioridad(prioridad);
            ar.setObservacion(observacion);
            actividadRecursoService.guardar(ar);
            resp.put("success", true);
            resp.put("message", "Recurso agregado correctamente");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping(params = "action=actualizarConseguida")
    @ResponseBody
    public Map<String, Object> actualizarConseguida(@RequestParam int idActividadRecurso,
                                                     @RequestParam double cantidadConseguida) {
        Map<String, Object> resp = new HashMap<>();
        try {
            actividadRecursoService.actualizarConseguida(idActividadRecurso, cantidadConseguida);
            resp.put("success", true);
            resp.put("message", "Cantidad actualizada");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping(params = "action=eliminarRecurso")
    @ResponseBody
    public Map<String, Object> eliminarRecurso(@RequestParam int idActividadRecurso) {
        Map<String, Object> resp = new HashMap<>();
        try {
            actividadRecursoService.eliminar(idActividadRecurso);
            resp.put("success", true);
            resp.put("message", "Recurso eliminado");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    /* ═══════════════════════════════════════════
     *  PARTICIPACIÓN DE VOLUNTARIOS
     * ═══════════════════════════════════════════ */
    @GetMapping(params = "action=participantes")
    @ResponseBody
    public List<Participacion> listarParticipantes(@RequestParam int id) {
        return participacionService.obtenerPorActividad(id);
    }

    @PostMapping(params = "action=agregarParticipante")
    @ResponseBody
    public Map<String, Object> agregarParticipante(@RequestParam int idActividad,
                                                    @RequestParam int idVoluntario) {
        Map<String, Object> resp = new HashMap<>();
        try {
            // Validar cupos disponibles
            var actividad = actividadService.obtenerActividadPorId(idActividad);
            if (actividad != null && actividad.getCupoMaximo() > 0
                    && actividad.getInscritos() >= actividad.getCupoMaximo()) {
                resp.put("success", false);
                resp.put("message", "No hay cupos disponibles (" + actividad.getInscritos() + "/" + actividad.getCupoMaximo() + ")");
                return resp;
            }
            participacionService.guardar(idActividad, idVoluntario);
            actividadService.actualizarInscritosPorParticipacion(idActividad);
            resp.put("success", true);
            resp.put("message", "Voluntario asignado correctamente");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping(params = "action=eliminarParticipante")
    @ResponseBody
    public Map<String, Object> eliminarParticipante(@RequestParam int idParticipacion) {
        Map<String, Object> resp = new HashMap<>();
        try {
            // Obtener idActividad antes de eliminar para actualizar inscritos
            int idActividad = participacionService.eliminarYObtenerActividad(idParticipacion);
            if (idActividad > 0) {
                actividadService.actualizarInscritosPorParticipacion(idActividad);
            }
            resp.put("success", true);
            resp.put("message", "Participante eliminado");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    /* ═══════════════════════════════════════════
     *  BENEFICIARIOS VINCULADOS
     * ═══════════════════════════════════════════ */
    @GetMapping(params = "action=beneficiarios")
    @ResponseBody
    public List<ActividadBeneficiario> listarBeneficiarios(@RequestParam int id) {
        return actividadBeneficiarioService.obtenerPorActividad(id);
    }

    @PostMapping(params = "action=agregarBeneficiario")
    @ResponseBody
    public Map<String, Object> agregarBeneficiario(@RequestParam int idActividad,
                                                    @RequestParam int idBeneficiario,
                                                    @RequestParam(required = false) String observacion) {
        Map<String, Object> resp = new HashMap<>();
        try {
            actividadBeneficiarioService.guardar(idActividad, idBeneficiario, observacion);
            resp.put("success", true);
            resp.put("message", "Beneficiario vinculado correctamente");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping(params = "action=eliminarBeneficiario")
    @ResponseBody
    public Map<String, Object> eliminarBeneficiario(@RequestParam int idActividadBeneficiario) {
        Map<String, Object> resp = new HashMap<>();
        try {
            actividadBeneficiarioService.eliminar(idActividadBeneficiario);
            resp.put("success", true);
            resp.put("message", "Beneficiario desvinculado");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    /* ═══════════════════════════════════════════
     *  CATÁLOGOS (para selects dinámicos)
     * ═══════════════════════════════════════════ */
    @GetMapping(params = "action=catalogoLugares")
    @ResponseBody
    public List<?> catalogoLugares() {
        return lugarService.obtenerTodos();
    }

    @GetMapping(params = "action=catalogoRecursos")
    @ResponseBody
    public List<?> catalogoRecursos() {
        return recursoService.obtenerTodos();
    }

    @GetMapping(params = "action=catalogoRoles")
    @ResponseBody
    public List<?> catalogoRoles() {
        return participacionService.obtenerRoles();
    }

    /* ── Lugar: CRUD rápido ── */
    @PostMapping(params = "action=crearLugar")
    @ResponseBody
    public Map<String, Object> crearLugar(@RequestParam String departamento,
                                           @RequestParam String provincia,
                                           @RequestParam String distrito,
                                           @RequestParam(required = false) String direccionReferencia) {
        Map<String, Object> resp = new HashMap<>();
        try {
            var lugar = new com.sistemadevoluntariado.entity.Lugar(departamento, provincia, distrito, direccionReferencia);
            lugar = lugarService.guardar(lugar);
            resp.put("success", true);
            resp.put("message", "Localidad registrada");
            resp.put("idLugar", lugar.getIdLugar());
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    /* ── Recurso: CRUD rápido ── */
    @PostMapping(params = "action=crearRecurso")
    @ResponseBody
    public Map<String, Object> crearRecurso(@RequestParam String nombre,
                                             @RequestParam(required = false) String unidadMedida,
                                             @RequestParam(required = false) String tipoRecurso,
                                             @RequestParam(required = false) String descripcion) {
        Map<String, Object> resp = new HashMap<>();
        try {
            var recurso = new com.sistemadevoluntariado.entity.Recurso(nombre, unidadMedida, tipoRecurso, descripcion);
            recurso = recursoService.guardar(recurso);
            resp.put("success", true);
            resp.put("message", "Recurso registrado");
            resp.put("idRecurso", recurso.getIdRecurso());
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }
}
