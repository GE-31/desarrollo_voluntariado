package com.sistemadevoluntariado.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.PermisoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Interceptor Spring MVC que verifica si el usuario autenticado
 * tiene el permiso necesario para acceder a cada ruta del sistema.
 *
 * Rutas protegidas y su permiso requerido:
 *   /voluntarios     → voluntarios.ver
 *   /beneficiarios   → beneficiarios.ver
 *   /actividades     → actividades.ver
 *   /asistencias     → asistencias.ver
 *   /inventario      → inventario.ver
 *   /donaciones      → donaciones.ver
 *   /calendario      → calendario.ver
 *   /reportes        → reportes.ver
 *   /tesoreria       → tesoreria.ver
 *   /certificados    → certificados.ver
 */
@Component
public class PermisoInterceptor implements HandlerInterceptor {

    private static final Logger logger = Logger.getLogger(PermisoInterceptor.class.getName());

    private static final Map<String, String> RUTA_PERMISO = new HashMap<>();

    static {
        RUTA_PERMISO.put("/voluntarios",   "voluntarios.ver");
        RUTA_PERMISO.put("/beneficiarios", "beneficiarios.ver");
        RUTA_PERMISO.put("/actividades",   "actividades.ver");
        RUTA_PERMISO.put("/asistencias",   "asistencias.ver");
        RUTA_PERMISO.put("/inventario",    "inventario.ver");
        RUTA_PERMISO.put("/donaciones",    "donaciones.ver");
        RUTA_PERMISO.put("/salidas-donaciones", "donaciones.ver");
        RUTA_PERMISO.put("/salidas-inventario", "inventario.ver");
        RUTA_PERMISO.put("/calendario",    "calendario.ver");
        RUTA_PERMISO.put("/reportes",      "reportes.ver");
        RUTA_PERMISO.put("/tesoreria",     "tesoreria.ver");
        RUTA_PERMISO.put("/certificados",  "certificados.ver");
    }

    @Autowired
    private PermisoService permisoService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // Si no hay sesión, Spring Security ya redirige al login
        if (session == null || session.getAttribute("usuarioLogeado") == null) {
            return true;
        }

        Usuario usuarioLogeado = (Usuario) session.getAttribute("usuarioLogeado");

        // Obtener la ruta relativa al contexto
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        String ruta = requestURI.substring(contextPath.length());

        // Quitar query string si existe
        int q = ruta.indexOf('?');
        if (q > 0) ruta = ruta.substring(0, q);

        // Verificar si esta ruta tiene un permiso requerido
        String permisoRequerido = RUTA_PERMISO.get(ruta);

        // Si no hay permiso mapeado, dejar pasar
        if (permisoRequerido == null) {
            return true;
        }

        // Cachear permisos en sesión
        @SuppressWarnings("unchecked")
        List<Integer> permisosEnSession = (List<Integer>) session.getAttribute("permisosUsuario");

        if (permisosEnSession == null || permisosEnSession.isEmpty()) {
            permisosEnSession = permisoService.obtenerPermisosDeUsuario(usuarioLogeado.getIdUsuario());
            session.setAttribute("permisosUsuario", permisosEnSession);
            session.removeAttribute("nombreRolUsuario");
            logger.info("✓ Permisos recargados para " + usuarioLogeado.getUsername() + ": " + permisosEnSession);
        }

        // Verificar el permiso
        boolean tieneAcceso = permisoService.tienePermiso(usuarioLogeado.getIdUsuario(), permisoRequerido);

        if (tieneAcceso) {
            return true;
        } else {
            logger.warning("⛔ Usuario " + usuarioLogeado.getUsername() +
                " intentó acceder a " + ruta + " sin permiso: " + permisoRequerido);

            session.setAttribute("mensajeError", "No tienes permiso para acceder a esa sección.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return false;
        }
    }
}
