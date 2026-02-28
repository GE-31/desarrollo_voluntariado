package com.sistemadevoluntariado.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.sistemadevoluntariado.entity.Usuario;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/session")
public class SessionRenewController {

    @PostMapping("/renew")
    public Map<String, Object> renew(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        if (usuario != null) {
            // Tocar la sesión para renovar el timeout
            session.setAttribute("lastActivity", System.currentTimeMillis());
            return Map.of("success", true, "message", "Sesión renovada");
        }
        return Map.of("success", false, "message", "No hay sesión activa");
    }
}
