package com.sistemadevoluntariado.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Maneja GET /login → muestra el formulario HTML.
 * El POST /doLogin lo intercepta Spring Security automáticamente.
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String mostrarLogin(Authentication authentication) {
        // Si ya está autenticado (y NO es anónimo), redirigir al dashboard
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "views/auth/login"; // Thymeleaf template
    }
}
