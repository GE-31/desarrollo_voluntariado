package com.sistemadevoluntariado.controller;

import java.time.LocalDate;
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
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.ActividadService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/actividades")
public class ActividadController {

    @Autowired
    private ActividadService actividadService;

    /* ───── Vista principal ───── */
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        if (usuario == null) return "redirect:/login";
        model.addAttribute("usuario", usuario);
        model.addAttribute("actividades", actividadService.obtenerTodasActividades());
        return "views/actividades/listar";
    }

    /* ───── REST: listar JSON ───── */
    @GetMapping(params = "action=listar")
    @ResponseBody
    public List<Actividad> listar() {
        return actividadService.obtenerTodasActividades();
    }

    /* ───── REST: obtener por ID ───── */
    @GetMapping(params = "action=obtener")
    @ResponseBody
    public Actividad obtener(@RequestParam int id) {
        return actividadService.obtenerActividadPorId(id);
    }

    /* ───── REST: crear ───── */
    @PostMapping(params = "action=crear")
    @ResponseBody
    public Map<String, Object> crear(@RequestParam String nombre,
                                     @RequestParam(required = false) String descripcion,
                                     @RequestParam String fechaInicio,
                                     @RequestParam(required = false) String fechaFin,
                                     @RequestParam String ubicacion,
                                     @RequestParam int cupoMaximo,
                                     HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
            if (usuario == null) return Map.of("success", false, "message", "No autorizado");

            LocalDate fi = LocalDate.parse(fechaInicio);
            LocalDate ff = (fechaFin != null && !fechaFin.isEmpty()) ? LocalDate.parse(fechaFin) : null;

            Actividad a = new Actividad(nombre, descripcion, fi, ff, ubicacion, cupoMaximo);
            a.setIdUsuario(usuario.getIdUsuario());

            boolean ok = actividadService.crearActividad(a);
            return ok ? Map.of("success", true, "message", "Actividad creada correctamente")
                      : Map.of("success", false, "message", "Error al crear la actividad");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    /* ───── REST: editar / actualizar ───── */
    @PostMapping(params = "action=editar")
    @ResponseBody
    public Map<String, Object> editar(@RequestParam int id,
                                      @RequestParam String nombre,
                                      @RequestParam(required = false) String descripcion,
                                      @RequestParam String fechaInicio,
                                      @RequestParam(required = false) String fechaFin,
                                      @RequestParam String ubicacion,
                                      @RequestParam int cupoMaximo) {
        try {
            Actividad a = new Actividad();
            a.setIdActividad(id);
            a.setNombre(nombre);
            a.setDescripcion(descripcion);
            a.setFechaInicio(fechaInicio != null && !fechaInicio.isEmpty() ? LocalDate.parse(fechaInicio) : null);
            a.setFechaFin(fechaFin != null && !fechaFin.isEmpty() ? LocalDate.parse(fechaFin) : null);
            a.setUbicacion(ubicacion);
            a.setCupoMaximo(cupoMaximo);

            boolean ok = actividadService.actualizarActividad(a);
            return ok ? Map.of("success", true, "message", "Actividad actualizada correctamente")
                      : Map.of("success", false, "message", "Error al actualizar la actividad");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @PostMapping(params = "action=actualizar")
    @ResponseBody
    public Map<String, Object> actualizar(@RequestParam int id,
                                          @RequestParam String nombre,
                                          @RequestParam(required = false) String descripcion,
                                          @RequestParam String fechaInicio,
                                          @RequestParam(required = false) String fechaFin,
                                          @RequestParam String ubicacion,
                                          @RequestParam int cupoMaximo) {
        return editar(id, nombre, descripcion, fechaInicio, fechaFin, ubicacion, cupoMaximo);
    }

    /* ───── REST: cambiar estado ───── */
    @PostMapping(params = "action=cambiarEstado")
    @ResponseBody
    public Map<String, Object> cambiarEstado(@RequestParam int id, @RequestParam String estado) {
        boolean ok = actividadService.cambiarEstado(id, estado);
        return ok ? Map.of("success", true, "message", "Estado actualizado correctamente")
                  : Map.of("success", false, "message", "Error al cambiar estado");
    }

    /* ───── REST: eliminar ───── */
    @PostMapping(params = "action=eliminar")
    @ResponseBody
    public Map<String, Object> eliminar(@RequestParam int id) {
        boolean ok = actividadService.eliminarActividad(id);
        return ok ? Map.of("success", true, "message", "Actividad eliminada correctamente")
                  : Map.of("success", false, "message", "Error al eliminar la actividad");
    }
}

// controladores el rest y las vistas para manejar 
// las actividades, incluyendo la vista principal,