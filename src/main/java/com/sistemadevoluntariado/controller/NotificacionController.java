package com.sistemadevoluntariado.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sistemadevoluntariado.entity.Notificacion;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.NotificacionService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    // ── Listar notificaciones del usuario ──
    @GetMapping(params = "action=listar")
    public List<Notificacion> listar(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        if (usuario == null) return List.of();
        return notificacionService.listarPorUsuario(usuario.getIdUsuario());
    }

    // ── Contar no leídas ──
    @GetMapping(params = "action=contar")
    public Map<String, Object> contar(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        if (usuario == null) return Map.of("cantidad", 0);
        int cantidad = notificacionService.contarNoLeidas(usuario.getIdUsuario());
        return Map.of("cantidad", cantidad);
    }

    // ── Marcar una como leída ──
    @GetMapping(params = "action=marcarLeida")
    public Map<String, Object> marcarLeida(@RequestParam int id) {
        try {
            notificacionService.marcarLeida(id);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    // ── Marcar todas como leídas ──
    @GetMapping(params = "action=marcarTodas")
    public Map<String, Object> marcarTodas(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
            if (usuario == null) return Map.of("success", false, "message", "Sesión expirada");
            notificacionService.marcarTodasLeidas(usuario.getIdUsuario());
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }
}
