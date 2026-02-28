package com.sistemadevoluntariado.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sistemadevoluntariado.entity.Actividad;
import com.sistemadevoluntariado.entity.SalidaDonacion;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.ActividadService;
import com.sistemadevoluntariado.service.SalidaDonacionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/salidas-donaciones")
public class SalidaDonacionController {

    private static final Logger logger = Logger.getLogger(SalidaDonacionController.class.getName());

    @Autowired
    private SalidaDonacionService salidaService;

    @Autowired
    private ActividadService actividadService;

    /* ───── Vista principal ───── */
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
        if (user == null) return "redirect:/login";

        String salidaError = (String) session.getAttribute("salidaError");
        if (salidaError != null) {
            model.addAttribute("salidaError", salidaError);
            session.removeAttribute("salidaError");
        }

        List<SalidaDonacion> salidas;
        try {
            salidas = salidaService.listarTodos();
            logger.info("✓ Salidas de donaciones cargadas: " + (salidas != null ? salidas.size() : "null"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cargar salidas de donaciones", e);
            salidas = new ArrayList<>();
        }

        model.addAttribute("page", "salidas-donaciones");
        model.addAttribute("salidas", salidas);
        model.addAttribute("usuario", user);
        return "views/salidas-donaciones/salidas-donaciones";
    }

    /* ───── REST GET: donaciones confirmadas (disponibles para asignar) ───── */
    @GetMapping(params = "accion=donaciones_disponibles")
    @ResponseBody
    public List<Map<String, Object>> donacionesDisponibles() {
        return salidaService.listarDonacionesDisponibles();
    }

    /* ───── REST GET: buscar donaciones con saldo disponible (AJAX autocompletado) ───── */
    @GetMapping(params = "accion=buscar_donaciones")
    @ResponseBody
    public Map<String, Object> buscarDonaciones(@RequestParam(defaultValue = "") String query) {
        List<Map<String, Object>> resultados = salidaService.buscarDonacionesDisponibles(query);
        return Map.of("results", resultados);
    }

    /* ───── REST GET: obtener saldo disponible de una donación ───── */
    @GetMapping(params = "accion=saldo_donacion")
    @ResponseBody
    public Map<String, Object> saldoDonacion(@RequestParam int id) {
        return salidaService.obtenerSaldoDisponible(id);
    }

    /* ───── REST GET: actividades activas (campañas destino) ───── */
    @GetMapping(params = "accion=actividades")
    @ResponseBody
    public List<Actividad> listarActividades() {
        return actividadService.obtenerActividadesActivas();
    }

    /* ───── REST GET: obtener salida por ID ───── */
    @GetMapping(params = "accion=obtener")
    @ResponseBody
    public Object obtener(@RequestParam int id) {
        SalidaDonacion s = salidaService.obtenerPorId(id);
        if (s == null) return Map.of("ok", false, "message", "Salida no encontrada");
        return s;
    }

    /* ───── POST: crear salida (sin parámetro accion) ───── */
    @PostMapping
    public String crear(HttpSession session,
                        @RequestParam int idDonacion,
                        @RequestParam int actividad,
                        @RequestParam double cantidad,
                        @RequestParam(required = false) String descripcion,
                        @RequestParam(required = false) String tipoSalida,
                        @RequestParam(required = false) Integer idItem,
                        @RequestParam(required = false) Double cantidadItem,
                        @RequestParam(required = false) String accion) {
        if (accion != null && !accion.isEmpty() && !"registrar".equalsIgnoreCase(accion)) {
            return "redirect:/salidas-donaciones";
        }

        Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
        if (user == null) return "redirect:/login";

        try {
            if (cantidad <= 0) {
                session.setAttribute("salidaError", "La cantidad debe ser mayor a 0.");
                return "redirect:/salidas-donaciones";
            }

            // Validar que no exceda el saldo disponible
            Map<String, Object> saldo = salidaService.obtenerSaldoDisponible(idDonacion);
            double saldoDisponible = saldo.get("saldoDisponible") instanceof Number
                    ? ((Number) saldo.get("saldoDisponible")).doubleValue() : 0.0;
            if (cantidad > saldoDisponible) {
                String tipoDon = saldo.getOrDefault("tipoDonacion", "DINERO").toString();
                String msg = "DINERO".equalsIgnoreCase(tipoDon)
                        ? String.format("El monto S/ %.2f excede el saldo disponible de la donación (S/ %.2f).", cantidad, saldoDisponible)
                        : String.format("La cantidad %.0f excede el saldo disponible de la donación (%.0f unidades).", cantidad, saldoDisponible);
                session.setAttribute("salidaError", msg);
                return "redirect:/salidas-donaciones";
            }

            SalidaDonacion s = new SalidaDonacion();
            s.setIdDonacion(idDonacion);
            s.setIdActividad(actividad);
            s.setCantidad(cantidad);
            s.setDescripcion(trim(descripcion));
            s.setTipoSalida(tipoSalida != null ? tipoSalida : "DINERO");
            s.setIdUsuarioRegistro(user.getIdUsuario());

            if ("ESPECIE".equalsIgnoreCase(tipoSalida)) {
                s.setIdItem(idItem);
                s.setCantidadItem(cantidadItem);
            }

            boolean guardado = salidaService.guardar(s);
            if (!guardado) {
                session.setAttribute("salidaError", "Error al registrar la salida. Intente nuevamente.");
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.SEVERE, "Error inesperado al registrar salida", e);
            session.setAttribute("salidaError", "Error inesperado al registrar la salida.");
        }
        return "redirect:/salidas-donaciones";
    }

    /* ───── POST: editar salida ───── */
    @PostMapping(params = "accion=editar")
    public String editar(HttpSession session,
                         @RequestParam int idSalida,
                         @RequestParam int actividad,
                         @RequestParam double cantidad,
                         @RequestParam(required = false) String descripcion,
                         @RequestParam(required = false) Integer idItem,
                         @RequestParam(required = false) Double cantidadItem) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
        if (user == null) return "redirect:/login";

        SalidaDonacion actual = salidaService.obtenerPorId(idSalida);
        if (actual == null || "ANULADO".equalsIgnoreCase(actual.getEstado())) {
            return "redirect:/salidas-donaciones";
        }

        SalidaDonacion s = new SalidaDonacion();
        s.setIdSalida(idSalida);
        s.setIdActividad(actividad);
        s.setCantidad(cantidad);
        s.setDescripcion(trim(descripcion));
        s.setIdItem(idItem);
        s.setCantidadItem(cantidadItem);

        try {
            boolean ok = salidaService.actualizar(s);
            if (!ok) {
                session.setAttribute("salidaError", "Error al editar la salida. Solo se pueden editar salidas PENDIENTES.");
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.SEVERE, "Error inesperado al editar salida", e);
            session.setAttribute("salidaError", "Error inesperado al editar la salida.");
        }
        return "redirect:/salidas-donaciones";
    }

    /* ───── POST: anular salida ───── */
    @PostMapping(params = "accion=anular")
    @ResponseBody
    public Map<String, Object> anular(@RequestParam int idSalida,
                                      @RequestParam(required = false, defaultValue = "Anulacion manual") String motivo,
                                      HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
        if (user == null) return Map.of("ok", false, "message", "No autorizado");
        boolean ok = salidaService.anular(idSalida, user.getIdUsuario(), motivo);
        return Map.of("ok", ok, "message", ok ? "Salida anulada" : "No se pudo anular la salida");
    }

    /* ───── POST: cambiar estado ───── */
    @PostMapping(params = "accion=cambiar_estado")
    @ResponseBody
    public Map<String, Object> cambiarEstado(@RequestParam int idSalida,
                                             @RequestParam String estado,
                                             HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
        if (user == null) return Map.of("ok", false, "message", "No autorizado");
        boolean ok = salidaService.cambiarEstado(idSalida, estado);
        return Map.of("ok", ok, "message", ok ? "Estado actualizado" : "No se pudo actualizar el estado");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
