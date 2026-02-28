package com.sistemadevoluntariado.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.sistemadevoluntariado.entity.MovimientoFinanciero;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.TesoreriaService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/tesoreria")
public class TesoreriaController {

    @Autowired
    private TesoreriaService tesoreriaService;

    // ── Vista principal ──
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        model.addAttribute("usuario", usuario);
        return "views/tesoreria/tesoreria";
    }

    // ── Listar movimientos (con JOINs) ──
    @GetMapping(params = "accion=listar")
    @ResponseBody
    public List<MovimientoFinanciero> listar() {
        return tesoreriaService.listar();
    }

    // ── Obtener por ID ──
    @GetMapping(params = "accion=obtener")
    @ResponseBody
    public Object obtener(@RequestParam int id) {
        MovimientoFinanciero m = tesoreriaService.obtenerPorId(id);
        if (m != null) return m;
        return Map.of("error", "Movimiento no encontrado");
    }

    // ── Balance general ──
    @GetMapping(params = "accion=balance")
    @ResponseBody
    public Map<String, Double> balance() {
        return tesoreriaService.obtenerBalance();
    }

    // ── Filtrar movimientos ──
    @GetMapping(params = "accion=filtrar")
    @ResponseBody
    public List<MovimientoFinanciero> filtrar(
            @RequestParam(required = false, defaultValue = "") String tipo,
            @RequestParam(required = false, defaultValue = "") String categoria,
            @RequestParam(required = false, defaultValue = "") String fechaInicio,
            @RequestParam(required = false, defaultValue = "") String fechaFin) {
        return tesoreriaService.filtrar(tipo, categoria, fechaInicio, fechaFin);
    }

    // ── Resumen por categoría ──
    @GetMapping(params = "accion=resumenCategoria")
    @ResponseBody
    public List<Map<String, Object>> resumenCategoria() {
        return tesoreriaService.resumenPorCategoria();
    }

    // ── Resumen mensual ──
    @GetMapping(params = "accion=resumenMensual")
    @ResponseBody
    public List<Map<String, Object>> resumenMensual() {
        return tesoreriaService.resumenMensual();
    }

    // ── Eliminar movimiento ──
    @GetMapping(params = "accion=eliminar")
    @ResponseBody
    public Map<String, Object> eliminar(@RequestParam int id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            boolean ok = tesoreriaService.eliminar(id);
            resp.put("success", ok);
            resp.put("message", ok ? "Movimiento eliminado correctamente" : "Error al eliminar movimiento");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    // ── Registrar movimiento ──
    @PostMapping(params = "accion=registrar")
    @ResponseBody
    public Map<String, Object> registrar(@RequestParam String tipo,
                                          @RequestParam double monto,
                                          @RequestParam(required = false) String descripcion,
                                          @RequestParam(required = false) String categoria,
                                          @RequestParam(required = false) String comprobante,
                                          @RequestParam(required = false) String fechaMovimiento,
                                          @RequestParam(required = false, defaultValue = "0") int idActividad,
                                          HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");

            MovimientoFinanciero m = new MovimientoFinanciero();
            m.setTipo(tipo);
            m.setMonto(monto);
            m.setDescripcion(descripcion != null ? descripcion : "");
            m.setCategoria(categoria != null ? categoria : "");
            m.setComprobante(comprobante != null ? comprobante : "");
            m.setFechaMovimiento(fechaMovimiento != null ? fechaMovimiento : "");
            m.setIdActividad(idActividad);
            m.setIdUsuario(usuario != null ? usuario.getIdUsuario() : 0);

            boolean ok = tesoreriaService.registrar(m);
            resp.put("success", ok);
            resp.put("message", ok ? "Movimiento registrado correctamente" : "Error al registrar movimiento");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    // ── Actualizar movimiento ──
    @PostMapping(params = "accion=actualizar")
    @ResponseBody
    public Map<String, Object> actualizar(@RequestParam int idMovimiento,
                                           @RequestParam String tipo,
                                           @RequestParam double monto,
                                           @RequestParam(required = false) String descripcion,
                                           @RequestParam(required = false) String categoria,
                                           @RequestParam(required = false) String comprobante,
                                           @RequestParam(required = false) String fechaMovimiento,
                                           @RequestParam(required = false, defaultValue = "0") int idActividad,
                                           HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");

            MovimientoFinanciero m = new MovimientoFinanciero();
            m.setIdMovimiento(idMovimiento);
            m.setTipo(tipo);
            m.setMonto(monto);
            m.setDescripcion(descripcion != null ? descripcion : "");
            m.setCategoria(categoria != null ? categoria : "");
            m.setComprobante(comprobante != null ? comprobante : "");
            m.setFechaMovimiento(fechaMovimiento != null ? fechaMovimiento : "");
            m.setIdActividad(idActividad);
            m.setIdUsuario(usuario != null ? usuario.getIdUsuario() : 0);

            boolean ok = tesoreriaService.actualizar(m);
            resp.put("success", ok);
            resp.put("message", ok ? "Movimiento actualizado correctamente" : "Error al actualizar movimiento");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }
}
