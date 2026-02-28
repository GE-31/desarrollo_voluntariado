package com.sistemadevoluntariado.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sistemadevoluntariado.entity.Actividad;
import com.sistemadevoluntariado.entity.InventarioItem;
import com.sistemadevoluntariado.entity.SalidaInventario;
import com.sistemadevoluntariado.entity.SalidaInventarioDetalle;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.ActividadService;
import com.sistemadevoluntariado.service.SalidaInventarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/salidas-inventario")
public class SalidaInventarioController {

    private static final Logger logger = Logger.getLogger(SalidaInventarioController.class.getName());

    @Autowired
    private SalidaInventarioService salidaService;

    @Autowired
    private ActividadService actividadService;

    /* ───── Vista principal ───── */
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
        if (user == null) return "redirect:/login";

        model.addAttribute("page", "salidas-inventario");
        model.addAttribute("usuario", user);
        return "views/salidas-inventario/salidas-inventario";
    }

    /* ───── REST GET: listar todas las salidas ───── */
    @GetMapping(params = "accion=listar")
    @ResponseBody
    public List<SalidaInventario> listar() {
        return salidaService.listarTodos();
    }

    /* ───── REST GET: obtener salida por ID ───── */
    @GetMapping(params = "accion=obtener")
    @ResponseBody
    public Object obtener(@RequestParam int id) {
        SalidaInventario s = salidaService.obtenerPorId(id);
        if (s == null) return Map.of("ok", false, "message", "Salida no encontrada");
        return s;
    }

    /* ───── REST GET: obtener detalle de una salida ───── */
    @GetMapping(params = "accion=detalle")
    @ResponseBody
    public List<SalidaInventarioDetalle> detalle(@RequestParam int id) {
        return salidaService.obtenerDetalle(id);
    }

    /* ───── REST GET: items disponibles para el carrito ───── */
    @GetMapping(params = "accion=items_disponibles")
    @ResponseBody
    public List<InventarioItem> itemsDisponibles() {
        return salidaService.listarItemsDisponibles();
    }

    /* ───── REST GET: actividades activas ───── */
    @GetMapping(params = "accion=actividades")
    @ResponseBody
    public List<Actividad> listarActividades() {
        return actividadService.obtenerActividadesActivas();
    }

    /* ───── POST: registrar salida completa (cabecera + carrito) ───── */
    @PostMapping(params = "accion=registrar")
    @ResponseBody
    public Map<String, Object> registrar(@RequestBody Map<String, Object> payload, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
            if (user == null) {
                resp.put("ok", false);
                resp.put("message", "No autorizado");
                return resp;
            }

            // Extraer cabecera
            String motivo = (String) payload.get("motivo");
            String observacion = (String) payload.getOrDefault("observacion", "");
            Object idActObj = payload.get("idActividad");
            Integer idActividad = null;
            if (idActObj != null) {
                idActividad = Integer.parseInt(idActObj.toString());
                if (idActividad == 0) idActividad = null;
            }

            if (motivo == null || motivo.trim().isEmpty()) {
                resp.put("ok", false);
                resp.put("message", "El motivo es obligatorio");
                return resp;
            }

            // Extraer items del carrito
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsCarrito = (List<Map<String, Object>>) payload.get("items");
            if (itemsCarrito == null || itemsCarrito.isEmpty()) {
                resp.put("ok", false);
                resp.put("message", "Debe agregar al menos un item al carrito");
                return resp;
            }

            // Construir entidades
            SalidaInventario cabecera = new SalidaInventario();
            cabecera.setIdActividad(idActividad);
            cabecera.setMotivo(motivo.trim());
            cabecera.setObservacion(observacion != null ? observacion.trim() : "");
            cabecera.setIdUsuarioRegistro(user.getIdUsuario());

            List<SalidaInventarioDetalle> detalles = new ArrayList<>();
            for (Map<String, Object> itemMap : itemsCarrito) {
                SalidaInventarioDetalle det = new SalidaInventarioDetalle();
                det.setIdItem(Integer.parseInt(itemMap.get("idItem").toString()));
                det.setCantidad(Double.parseDouble(itemMap.get("cantidad").toString()));

                if (det.getCantidad() <= 0) {
                    resp.put("ok", false);
                    resp.put("message", "La cantidad debe ser mayor a 0 para cada item");
                    return resp;
                }

                detalles.add(det);
            }

            int idSalida = salidaService.registrarSalidaCompleta(cabecera, detalles);
            if (idSalida > 0) {
                resp.put("ok", true);
                resp.put("message", "Salida registrada correctamente con " + detalles.size() + " item(s)");
                resp.put("idSalidaInv", idSalida);
            } else {
                resp.put("ok", false);
                resp.put("message", "Error al registrar la salida");
            }
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error al registrar salida de inventario", e);
            resp.put("ok", false);
            resp.put("message", e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al registrar salida de inventario", e);
            resp.put("ok", false);
            resp.put("message", "Error inesperado: " + e.getMessage());
        }
        return resp;
    }

    /* ───── POST: anular salida ───── */
    @PostMapping(params = "accion=anular")
    @ResponseBody
    public Map<String, Object> anular(@RequestParam int idSalidaInv,
                                      @RequestParam(required = false, defaultValue = "Anulación manual") String motivo,
                                      HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
            if (user == null) {
                resp.put("ok", false);
                resp.put("message", "No autorizado");
                return resp;
            }

            boolean ok = salidaService.anular(idSalidaInv, user.getIdUsuario(), motivo);
            resp.put("ok", ok);
            resp.put("message", ok ? "Salida anulada y stock devuelto" : "No se pudo anular la salida");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al anular salida de inventario", e);
            resp.put("ok", false);
            resp.put("message", "Error al anular la salida: " + e.getMessage());
        }
        return resp;
    }
}
