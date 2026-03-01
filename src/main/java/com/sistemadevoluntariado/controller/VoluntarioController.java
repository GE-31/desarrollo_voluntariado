package com.sistemadevoluntariado.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.entity.Voluntario;
import com.sistemadevoluntariado.service.VoluntarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/voluntarios")
public class VoluntarioController {

    /** Cargos que otorgan acceso al sistema */
    private static final Set<String> CARGOS_CON_ACCESO = Set.of(
            "Coordinador", "Responsable", "Líder", "Administrador");

    @Autowired
    private VoluntarioService voluntarioService;

    // ── Vista principal ──
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        model.addAttribute("usuario", usuario);
        return "views/voluntarios/listar";
    }

    // ── Listar todos ──
    @GetMapping(params = "action=listar")
    @ResponseBody
    public List<Voluntario> listar() {
        return voluntarioService.obtenerTodosVoluntarios();
    }

    // ── Obtener por ID ──
    @GetMapping(params = "action=obtener")
    @ResponseBody
    public Object obtener(@RequestParam int id) {
        Voluntario v = voluntarioService.obtenerVoluntarioPorId(id);
        if (v != null) {
            return v;
        }
        return Map.of("error", "Voluntario no encontrado");
    }

    // ── Crear voluntario ──
    @PostMapping(params = "action=crear")
    @ResponseBody
    public Map<String, Object> crear(@RequestParam String nombres,
                                      @RequestParam String apellidos,
                                      @RequestParam String dni,
                                      @RequestParam(required = false) String correo,
                                      @RequestParam(required = false) String telefono,
                                      @RequestParam(required = false) String carrera,
                                      @RequestParam(required = false) String cargo,
                                      HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
            Voluntario v = new Voluntario();
            v.setNombres(nombres);
            v.setApellidos(apellidos);
            v.setDni(dni);
            v.setCorreo(correo != null ? correo : "");
            v.setTelefono(telefono != null ? telefono : "");
            v.setCarrera(carrera != null ? carrera : "");
            String cargoFinal = (cargo != null && !cargo.isBlank()) ? cargo.trim() : "Voluntario";
            v.setCargo(cargoFinal);
            v.setAccesoSistema(CARGOS_CON_ACCESO.contains(cargoFinal));
            v.setIdUsuario(null);

            boolean ok = voluntarioService.crearVoluntario(v);
            resp.put("success", ok);
            resp.put("message", ok ? "Voluntario registrado correctamente" : "Error al registrar voluntario");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    // ── Editar voluntario ──
    @PostMapping(params = "action=editar")
    @ResponseBody
    public Map<String, Object> editar(@RequestParam int idVoluntario,
                                       @RequestParam String nombres,
                                       @RequestParam String apellidos,
                                       @RequestParam String dni,
                                       @RequestParam(required = false) String correo,
                                       @RequestParam(required = false) String telefono,
                                       @RequestParam(required = false) String carrera,
                                       @RequestParam(required = false) String cargo,
                                       HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
            Voluntario v = new Voluntario();
            v.setIdVoluntario(idVoluntario);
            v.setNombres(nombres);
            v.setApellidos(apellidos);
            v.setDni(dni);
            v.setCorreo(correo != null ? correo : "");
            v.setTelefono(telefono != null ? telefono : "");
            v.setCarrera(carrera != null ? carrera : "");
            String cargoFinal = (cargo != null && !cargo.isBlank()) ? cargo.trim() : "Voluntario";
            v.setCargo(cargoFinal);
            v.setAccesoSistema(CARGOS_CON_ACCESO.contains(cargoFinal));
            v.setIdUsuario(usuario != null ? usuario.getIdUsuario() : null);

            boolean ok = voluntarioService.actualizarVoluntario(v);
            resp.put("success", ok);
            resp.put("message", ok ? "Voluntario actualizado correctamente" : "Error al actualizar voluntario");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    // ── Cambiar estado ──
    @PostMapping(params = "action=cambiarEstado")
    @ResponseBody
    public Map<String, Object> cambiarEstado(@RequestParam int id,
                                              @RequestParam String estado) {
        Map<String, Object> resp = new HashMap<>();
        try {
            boolean ok = voluntarioService.cambiarEstado(id, estado);
            resp.put("success", ok);
            resp.put("message", ok ? "Estado actualizado correctamente" : "Error al cambiar estado");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    // ── Eliminar voluntario ──
    @PostMapping(params = "action=eliminar")
    @ResponseBody
    public Map<String, Object> eliminar(@RequestParam int id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            boolean ok = voluntarioService.eliminarVoluntario(id);
            resp.put("success", ok);
            resp.put("message", ok ? "Voluntario eliminado correctamente"
                                    : "No se pudo eliminar: puede tener registros asociados");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    // ── Validar y guardar voluntario ──
    @PostMapping(params = {"action=crear", "action=editar"})
    @ResponseBody
    public Map<String, Object> validarYGuardarVoluntario(@RequestParam(required = false) Integer idVoluntario,
                                                     @RequestParam String nombres,
                                                     @RequestParam String apellidos,
                                                     @RequestParam String dni,
                                                     @RequestParam(required = false) String correo,
                                                     @RequestParam(required = false) String telefono,
                                                     @RequestParam(required = false) String carrera,
                                                     @RequestParam(required = false) String cargo,
                                                     HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            // Validar duplicados
            if (voluntarioService.existeDni(dni, idVoluntario)) {
                resp.put("success", false);
                resp.put("message", "El DNI ya está registrado.");
                return resp;
            }
            if (correo != null && voluntarioService.existeCorreo(correo, idVoluntario)) {
                resp.put("success", false);
                resp.put("message", "El correo ya está registrado.");
                return resp;
            }
            if (telefono != null && voluntarioService.existeTelefono(telefono, idVoluntario)) {
                resp.put("success", false);
                resp.put("message", "El teléfono ya está registrado.");
                return resp;
            }

            // Guardar voluntario
            Voluntario v = new Voluntario();
            if (idVoluntario != null) {
                v.setIdVoluntario(idVoluntario);
            }
            v.setNombres(nombres);
            v.setApellidos(apellidos);
            v.setDni(dni);
            v.setCorreo(correo != null ? correo : "");
            v.setTelefono(telefono != null ? telefono : "");
            v.setCarrera(carrera != null ? carrera : "");
            String cargoFinal = (cargo != null && !cargo.isBlank()) ? cargo.trim() : "Voluntario";
            v.setCargo(cargoFinal);
            v.setAccesoSistema(CARGOS_CON_ACCESO.contains(cargoFinal));
            v.setIdUsuario(null);

            boolean ok = idVoluntario == null ? voluntarioService.crearVoluntario(v) : voluntarioService.actualizarVoluntario(v);
            resp.put("success", ok);
            resp.put("message", ok ? "Voluntario guardado correctamente" : "Error al guardar voluntario");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    // ── Buscar voluntarios ──
    @GetMapping(params = "action=buscar")
    @ResponseBody
    public List<Voluntario> buscar(
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidos,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) String cargo) {
        return voluntarioService.buscarVoluntarios(nombres, apellidos, dni, correo, telefono, carrera, cargo);
    }
}
