package com.sistemadevoluntariado.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.CustomUserDetailsService;
import com.sistemadevoluntariado.service.NotificacionService;
import com.sistemadevoluntariado.service.PermisoService;
import com.sistemadevoluntariado.service.RolSistemaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Se ejecuta tras login exitoso:
 *  - guarda el objeto Usuario completo en la sesión HTTP (usuarioLogeado)
 *  - genera notificaciones del día
 *  - redirige al dashboard
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PermisoService permisoService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private RolSistemaService rolSistemaService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String username = authentication.getName();
        Usuario usuario = userDetailsService.loadUsuarioCompleto(username);

        HttpSession session = request.getSession();
        session.setAttribute("usuarioLogeado", usuario);

        // Cargar permisos del usuario en la sesión para el sidebar
        if (usuario != null) {
            try {
                List<Integer> permisos = permisoService.obtenerPermisosDeUsuario(usuario.getIdUsuario());
                session.setAttribute("permisosUsuario", permisos);
            } catch (Exception e) {
                // Log error pero no interrumpir login
            }

            // Cargar nombre del rol en la sesión para el topbar
            try {
                String nombreRol = rolSistemaService.obtenerNombreRolDeUsuario(usuario.getIdUsuario());
                session.setAttribute("nombreRolUsuario", nombreRol != null ? nombreRol : "Usuario");
            } catch (Exception e) {
                session.setAttribute("nombreRolUsuario", "Usuario");
            }
        }

        // Generar notificaciones del día
        if (usuario != null) {
            try {
                notificacionService.generarNotificacionesActividadesHoy(usuario.getIdUsuario());
                notificacionService.generarNotificacionesEventosHoy(usuario.getIdUsuario());
            } catch (Exception e) {
                // No cortar el login si las notificaciones fallan
            }
        }

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
