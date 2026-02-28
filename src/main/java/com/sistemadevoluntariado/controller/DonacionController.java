package com.sistemadevoluntariado.controller;

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
import com.sistemadevoluntariado.entity.Donacion;
import com.sistemadevoluntariado.entity.InventarioItem;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.ActividadService;
import com.sistemadevoluntariado.service.DonacionService;
import com.sistemadevoluntariado.service.InventarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/donaciones")
public class DonacionController {

    @Autowired
    private DonacionService donacionService;

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private InventarioService inventarioService;

    /* â”€â”€â”€â”€â”€ Vista principal â”€â”€â”€â”€â”€ */
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
        if (user == null)
            return "redirect:/login";

        String donacionError = (String) session.getAttribute("donacionError");
        if (donacionError != null) {
            model.addAttribute("donacionError", donacionError);
            session.removeAttribute("donacionError");
        }

        model.addAttribute("page", "donaciones");
        model.addAttribute("donaciones", donacionService.listarTodos());
        model.addAttribute("usuario", user);
        return "views/donaciones/donaciones";
    }

    /* â”€â”€â”€â”€â”€ REST GET: actividades activas â”€â”€â”€â”€â”€ */
    @GetMapping(params = "accion=actividades")
    @ResponseBody
    public List<Actividad> listarActividades() {
        return actividadService.obtenerActividadesActivas();
    }

    /* â”€â”€â”€â”€â”€ REST GET: obtener donaciÃ³n por ID â”€â”€â”€â”€â”€ */
    @GetMapping(params = "accion=obtener")
    @ResponseBody
    public Object obtener(@RequestParam int id) {
        Donacion d = donacionService.obtenerPorId(id);
        if (d == null)
            return Map.of("ok", false, "message", "Donacion no encontrada");
        return d;
    }

    /* â”€â”€â”€â”€â”€ REST GET: buscar donantes registrados â”€â”€â”€â”€â”€ */
    @GetMapping(params = "accion=buscarDonante")
    @ResponseBody
    public List<Map<String, Object>> buscarDonante(@RequestParam String q) {
        return donacionService.buscarDonantes(q);
    }

    /* â”€â”€â”€â”€â”€ POST: crear donaciÃ³n (sin parÃ¡metro accion) â”€â”€â”€â”€â”€ */
    @PostMapping
    public String crear(HttpSession session,
            @RequestParam double cantidad,
            @RequestParam(required = false) String descripcion,
            @RequestParam int tipoDonacion,
            @RequestParam(required = false) String subtipoDonacion,
            @RequestParam int actividad,
            @RequestParam(required = false, defaultValue = "0") String donacionAnonima,
            @RequestParam(required = false) String tipoDonante,
            @RequestParam(required = false) String nombreDonante,
            @RequestParam(required = false) String correoDonante,
            @RequestParam(required = false) String telefonoDonante,
            @RequestParam(required = false) String rucDonante,
            @RequestParam(required = false) String dniDonante,
            @RequestParam(required = false) Integer idItem,
            @RequestParam(required = false) String accion) {
        // Si viene con un accion inesperado (ni vacÃ­o ni "registrar"), redirigir
        if (accion != null && !accion.isEmpty() && !"registrar".equalsIgnoreCase(accion)) {
            return "redirect:/donaciones";
        }

        Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
        if (user == null)
            return "redirect:/login";

        Donacion d = new Donacion();
        try {
            if (cantidad <= 0)
                return "redirect:/donaciones";

            d.setCantidad(cantidad);
            d.setDescripcion(descripcion);
            d.setIdTipoDonacion(tipoDonacion);
            d.setSubtipoDonacion(trim(subtipoDonacion));
            d.setIdActividad(actividad);
            d.setIdUsuarioRegistro(user.getIdUsuario());
            boolean anonima = "1".equals(donacionAnonima);
            d.setDonacionAnonima(anonima);

            if (!anonima) {
                d.setTipoDonante(trim(tipoDonante));
                d.setNombreDonante(trim(nombreDonante));
                d.setCorreoDonante(trim(correoDonante));
                d.setTelefonoDonante(trim(telefonoDonante));
                d.setDniDonante(trim(dniDonante));
                d.setRucDonante(trim(rucDonante));

                String tipoVal = d.getTipoDonante() != null ? d.getTipoDonante().toUpperCase() : "PERSONA";
                if ("PERSONA".equals(tipoVal)) {
                    if (isBlank(d.getNombreDonante())) {
                        session.setAttribute("donacionError", "Debe ingresar el nombre del donante (persona).");
                        return "redirect:/donaciones";
                    }
                    if (isBlank(d.getCorreoDonante()) && isBlank(d.getTelefonoDonante())) {
                        session.setAttribute("donacionError",
                                "Para persona: ingresa al menos correo o telÃ©fono del donante.");
                        return "redirect:/donaciones";
                    }
                } else if ("EMPRESA".equals(tipoVal) || "GRUPO".equals(tipoVal)) {
                    if (isBlank(d.getRucDonante())) {
                        session.setAttribute("donacionError", "Para empresa/grupo es obligatorio el RUC.");
                        return "redirect:/donaciones";
                    }
                    if (isBlank(d.getNombreDonante())) {
                        session.setAttribute("donacionError",
                                "Debe ingresar la razÃ³n social del donante (empresa/grupo).");
                        return "redirect:/donaciones";
                    }
                }
            }

            if (tipoDonacion == 2) {
                if (idItem == null || idItem <= 0) {
                    session.setAttribute("donacionError",
                            "Para donaciones en especie debes seleccionar un Ã­tem existente.");
                    return "redirect:/donaciones";
                }
                InventarioItem itm = inventarioService.obtenerPorId(idItem);
                if (itm == null || !"ACTIVO".equalsIgnoreCase(itm.getEstado())) {
                    session.setAttribute("donacionError", "El Ã­tem seleccionado no existe o no estÃ¡ activo.");
                    return "redirect:/donaciones";
                }
                d.setIdItem(idItem);
            }

            boolean guardado = donacionService.guardar(d);
            if (!guardado) {
                session.setAttribute("donacionError", "Error al registrar la donaciÃ³n. Intente nuevamente.");
            }
        } catch (Exception e) {
            // El SP hace COMMIT interno, asÃ­ que la donaciÃ³n puede estar guardada
            // aunque la transacciÃ³n de Hibernate falle (ej: integraciÃ³n con TesorerÃ­a)
            if (d.getIdDonacion() > 0) {
                java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.WARNING,
                        "DonaciÃ³n #" + d.getIdDonacion() + " guardada OK, pero hubo error en integraciÃ³n post-registro",
                        e);
            } else {
                java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,
                        "Error inesperado al registrar donaciÃ³n", e);
                session.setAttribute("donacionError", "Error inesperado al registrar la donaciÃ³n. Intente nuevamente.");
            }
        }
        return "redirect:/donaciones";
    }

    /* â”€â”€â”€â”€â”€ POST: anular donaciÃ³n â”€â”€â”€â”€â”€ */
    @PostMapping(params = "accion=anular")
    @ResponseBody
    public Map<String, Object> anular(@RequestParam int idDonacion,
            @RequestParam(required = false, defaultValue = "Anulacion manual") String motivo,
            HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
        if (user == null)
            return Map.of("ok", false, "message", "No autorizado");
        boolean ok = donacionService.anular(idDonacion, user.getIdUsuario(), motivo);
        return Map.of("ok", ok, "message", ok ? "Donacion anulada" : "No se pudo anular la donacion");
    }

    /* â”€â”€â”€â”€â”€ POST: editar donaciÃ³n â”€â”€â”€â”€â”€ */
    @PostMapping(params = "accion=editar")
    public String editar(HttpSession session,
            @RequestParam int idDonacion,
            @RequestParam(required = false) Double cantidad,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String subtipoDonacion,
            @RequestParam(required = false) Integer actividad,
            @RequestParam(required = false, defaultValue = "0") String donacionAnonima,
            @RequestParam(required = false) String tipoDonante,
            @RequestParam(required = false) String dniDonante,
            @RequestParam(required = false) String nombreDonante,
            @RequestParam(required = false) String correoDonante,
            @RequestParam(required = false) String telefonoDonante,
            @RequestParam(required = false) String rucDonante,
            @RequestParam(required = false) String motivoEdicion) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
        if (user == null)
            return "redirect:/login";

        Donacion actual = donacionService.obtenerPorId(idDonacion);
        if (actual == null || "ANULADO".equalsIgnoreCase(actual.getEstado())) {
            return "redirect:/donaciones";
        }

        Donacion d = new Donacion();
        d.setIdDonacion(idDonacion);
        d.setIdUsuarioRegistro(user.getIdUsuario());

        boolean especiePendiente = actual.getIdTipoDonacion() == 2 && "PENDIENTE".equalsIgnoreCase(actual.getEstado());
        if ((actual.getIdTipoDonacion() == 1 || especiePendiente) && cantidad != null && cantidad > 0) {
            d.setCantidad(cantidad);
        } else {
            d.setCantidad(actual.getCantidad());
        }

        d.setDescripcion(trim(descripcion));
        d.setSubtipoDonacion(trim(subtipoDonacion));
        d.setIdActividad(actividad != null && actividad > 0 ? actividad : actual.getIdActividad());

        boolean anonima = "1".equals(donacionAnonima);
        d.setDonacionAnonima(anonima);
        if (!anonima) {
            d.setTipoDonante(trim(tipoDonante));
            d.setDniDonante(trim(dniDonante));
            d.setNombreDonante(trim(nombreDonante));
            d.setCorreoDonante(trim(correoDonante));
            d.setTelefonoDonante(trim(telefonoDonante));
            d.setRucDonante(trim(rucDonante));

            String tipoVal = d.getTipoDonante() != null ? d.getTipoDonante().toUpperCase() : "PERSONA";
            if ("PERSONA".equals(tipoVal)) {
                if (isBlank(d.getDniDonante())) {
                    session.setAttribute("donacionError", "Para persona: ingresa el DNI del donante.");
                    return "redirect:/donaciones";
                }
                if (isBlank(d.getNombreDonante())) {
                    session.setAttribute("donacionError", "Debe ingresar el nombre del donante (persona).");
                    return "redirect:/donaciones";
                }
            } else if ("EMPRESA".equals(tipoVal) || "GRUPO".equals(tipoVal)) {
                if (isBlank(d.getRucDonante())) {
                    session.setAttribute("donacionError", "Para empresa/grupo es obligatorio el RUC.");
                    return "redirect:/donaciones";
                }
            }
        }
        d.setMotivoAnulacion(trim(motivoEdicion));

        try {
            boolean ok = donacionService.actualizarConTesoreria(d, actual);
            if (!ok) {
                session.setAttribute("donacionError", "No se pudo editar la donacion. Verifica el estado y los datos.");
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,
                    "Error inesperado al editar donacion", e);
            session.setAttribute("donacionError", "Error inesperado al editar la donacion. Intente nuevamente.");
        }
        return "redirect:/donaciones";
    }

    /* â”€â”€â”€â”€â”€ POST: cambiar estado â”€â”€â”€â”€â”€ */
    @PostMapping(params = "accion=cambiar_estado")
    @ResponseBody
    public Map<String, Object> cambiarEstado(@RequestParam int idDonacion,
            @RequestParam String estado,
            HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogeado");
        if (user == null)
            return Map.of("ok", false, "message", "No autorizado");
        try {
            // 1. Obtener datos ANTES del cambio (transacciÃ³n propia)
            Donacion antes = donacionService.obtenerPorId(idDonacion);

            // 2. Cambiar estado en la BD (transacciÃ³n propia, se commitea independientemente)
            boolean ok = donacionService.cambiarEstado(idDonacion, estado);

            // 3. IntegraciÃ³n TesorerÃ­a/Inventario (transacciÃ³n separada)
            //    Si falla, el cambio de estado ya estÃ¡ comiteado.
            if (ok) {
                try {
                    donacionService.ejecutarIntegracionPostCambio(antes, idDonacion, estado, user.getIdUsuario());
                } catch (Exception intEx) {
                    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.WARNING,
                            "IntegraciÃ³n fallÃ³ pero el estado de la donaciÃ³n #" + idDonacion + " ya fue actualizado",
                            intEx);
                }
            }
            return Map.of("ok", ok, "message", ok ? "Estado actualizado" : "No se pudo actualizar el estado");
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.WARNING,
                    "ExcepciÃ³n al cambiar estado de donaciÃ³n #" + idDonacion, e);
            // Verificar si el SP actualizÃ³ el estado a pesar de la excepciÃ³n
            try {
                Donacion actual = donacionService.obtenerPorId(idDonacion);
                if (actual != null && estado.equalsIgnoreCase(actual.getEstado())) {
                    return Map.of("ok", true, "message", "Estado actualizado");
                }
            } catch (Exception ignored) {
            }
            return Map.of("ok", false, "message", "No se pudo actualizar el estado");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

