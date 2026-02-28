package com.sistemadevoluntariado.controller;

import java.util.ArrayList;
import java.util.Collections;
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

import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.entity.Voluntario;
import com.sistemadevoluntariado.service.PermisoService;
import com.sistemadevoluntariado.service.RolSistemaService;
import com.sistemadevoluntariado.service.UsuarioService;
import com.sistemadevoluntariado.service.VoluntarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private VoluntarioService voluntarioService;
    @Autowired private RolSistemaService rolSistemaService;
    @Autowired private PermisoService permisoService;

    // ── Vista principal ──
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        model.addAttribute("usuario", usuario);
        return "views/usuarios/usuario";
    }

    // ── Listar todos los usuarios ──
    @GetMapping(params = "action=listar")
    @ResponseBody
    public List<Map<String, Object>> listar() {
        List<Usuario> usuarios = usuarioService.obtenerTodosUsuarios();
        Map<Integer, String> rolesPorUsuario = rolSistemaService.obtenerRolesPorUsuario();

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Usuario u : usuarios) {
            Map<String, Object> item = new HashMap<>();
            item.put("idUsuario", u.getIdUsuario());
            item.put("username", u.getUsername());
            item.put("estado", u.getEstado());
            item.put("creadoEn", u.getCreadoEn());
            item.put("fotoPerfil", u.getFotoPerfil());
            item.put("correo", u.getCorreo());
            // Buscar voluntario asociado por id_usuario
            Voluntario vol = voluntarioService.obtenerVoluntarioPorUsuarioId(u.getIdUsuario());
            if (vol != null) {
                item.put("nombres", vol.getNombres());
                item.put("apellidos", vol.getApellidos());
                item.put("idVoluntario", vol.getIdVoluntario());
            } else {
                item.put("nombres", u.getNombres());
                item.put("apellidos", u.getApellidos());
            }
            item.put("nombreRol", rolesPorUsuario.getOrDefault(u.getIdUsuario(), "Sin rol"));
            resultado.add(item);
        }
        return resultado;
    }

    // ── Voluntarios con acceso al sistema ──
    @GetMapping(params = "action=voluntarios")
    @ResponseBody
    public List<Voluntario> voluntariosConAcceso() {
        return voluntarioService.obtenerVoluntariosConAcceso();
    }

    // ── Roles del sistema ──
    @GetMapping(params = "action=roles")
    @ResponseBody
    public List<com.sistemadevoluntariado.entity.RolSistema> roles() {
        return rolSistemaService.obtenerTodosRoles();
    }

    // ── Obtener un usuario por ID (con permisos) ──
    @GetMapping(params = "action=obtener")
    @ResponseBody
    public Map<String, Object> obtener(@RequestParam int id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario u = usuarioService.obtenerUsuarioPorId(id);
            if (u == null) {
                resp.put("error", "Usuario no encontrado");
                return resp;
            }

            resp.put("idUsuario", u.getIdUsuario());
            resp.put("username", u.getUsername());
            resp.put("estado", u.getEstado());

            // Buscar voluntario asociado
            Voluntario vol = voluntarioService.obtenerVoluntarioPorUsuarioId(u.getIdUsuario());
            if (vol != null) {
                resp.put("idVoluntario", vol.getIdVoluntario());
            }

            // Permisos actuales
            List<Integer> permisos = permisoService.obtenerPermisosDeUsuario(u.getIdUsuario());
            resp.put("permisos", permisos);

            // Rol
            String rol = rolSistemaService.obtenerNombreRolDeUsuario(u.getIdUsuario());
            resp.put("nombreRol", rol != null ? rol : "Sin rol");

            // Todos los roles disponibles
            resp.put("roles", rolSistemaService.obtenerTodosRoles());

            // Todos los permisos disponibles
            resp.put("todosPermisos", permisoService.obtenerTodosPermisos());
        } catch (Exception e) {
            resp.put("error", "Error al obtener usuario: " + e.getMessage());
        }
        return resp;
    }

    // ── Crear usuario ──
    @PostMapping(params = "action=crear")
    @ResponseBody
    public Map<String, Object> crear(@RequestParam int voluntarioId,
                                      @RequestParam(required = false, defaultValue = "0") int rolSistemaId,
                                      @RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam(value = "permisos[]", required = false) List<Integer> permisos) {
        Map<String, Object> resp = new HashMap<>();
        try {
            boolean ok = usuarioService.registrarUsuarioConVoluntario(voluntarioId, rolSistemaId, username, password);
            if (ok) {
                // Obtener el usuario recién creado para guardar permisos
                Usuario nuevoUsuario = usuarioService.obtenerUsuarioPorUsername(username);
                if (nuevoUsuario != null && permisos != null && !permisos.isEmpty()) {
                    permisoService.guardarPermisosUsuario(nuevoUsuario.getIdUsuario(), permisos);
                }
                resp.put("success", true);
                resp.put("message", "Usuario creado correctamente");
            } else {
                resp.put("success", false);
                resp.put("message", "Error al crear el usuario");
            }
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    // ── Editar usuario (solo permisos) ──
    @PostMapping(params = "action=editar")
    @ResponseBody
    public Map<String, Object> editar(@RequestParam int idUsuario,
                                       @RequestParam(value = "permisos[]", required = false) List<Integer> permisos) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (permisos == null) permisos = Collections.emptyList();
            boolean ok = permisoService.guardarPermisosUsuario(idUsuario, permisos);
            resp.put("success", ok);
            resp.put("message", ok ? "Permisos actualizados correctamente" : "Error al actualizar permisos");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    // ── Eliminar usuario ──
    @PostMapping(params = "action=eliminar")
    @ResponseBody
    public Map<String, Object> eliminar(@RequestParam int id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            boolean ok = usuarioService.eliminarUsuario(id);
            resp.put("success", ok);
            resp.put("message", ok ? "Usuario eliminado correctamente" : "Error al eliminar usuario");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    // ── Cambiar estado ──
    @PostMapping(params = "action=cambiar_estado")
    @ResponseBody
    public Map<String, Object> cambiarEstado(@RequestParam int id,
                                              @RequestParam String estado) {
        Map<String, Object> resp = new HashMap<>();
        try {
            boolean ok = usuarioService.cambiarEstado(id, estado);
            resp.put("success", ok);
            resp.put("message", ok ? "Estado actualizado correctamente" : "Error al cambiar estado");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }
}
