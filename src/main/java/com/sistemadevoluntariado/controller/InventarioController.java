package com.sistemadevoluntariado.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.sistemadevoluntariado.entity.CategoriaInventario;
import com.sistemadevoluntariado.entity.InventarioItem;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.InventarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    // ── Vista principal ──
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        model.addAttribute("usuario", usuario);
        return "views/inventario/inventario";
    }

    // ══════════════════════ CATEGORÍAS ══════════════════════

    @GetMapping(params = "accion=listar_categorias")
    @ResponseBody
    public List<CategoriaInventario> listarCategorias() {
        return inventarioService.listarCategorias();
    }

    @PostMapping(params = "accion=registrar_categoria")
    @ResponseBody
    public Map<String, Object> registrarCategoria(@RequestParam String nombre,
                                                    @RequestParam(required = false) String descripcion,
                                                    @RequestParam(required = false) String color,
                                                    @RequestParam(required = false) String icono) {
        Map<String, Object> resp = new HashMap<>();
        try {
            CategoriaInventario cat = new CategoriaInventario();
            cat.setNombre(nombre);
            cat.setDescripcion(descripcion != null ? descripcion : "");
            cat.setColor(color != null ? color : "#6c757d");
            cat.setIcono(icono != null ? icono : "fa-box");
            boolean ok = inventarioService.registrarCategoria(cat);
            resp.put("success", ok);
            resp.put("message", ok ? "Categoría registrada" : "Error al registrar categoría");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping(params = "accion=actualizar_categoria")
    @ResponseBody
    public Map<String, Object> actualizarCategoria(@RequestParam int idCategoria,
                                                     @RequestParam String nombre,
                                                     @RequestParam(required = false) String descripcion,
                                                     @RequestParam(required = false) String color,
                                                     @RequestParam(required = false) String icono) {
        Map<String, Object> resp = new HashMap<>();
        try {
            CategoriaInventario cat = new CategoriaInventario();
            cat.setIdCategoria(idCategoria);
            cat.setNombre(nombre);
            cat.setDescripcion(descripcion != null ? descripcion : "");
            cat.setColor(color != null ? color : "#6c757d");
            cat.setIcono(icono != null ? icono : "fa-box");
            boolean ok = inventarioService.actualizarCategoria(cat);
            resp.put("success", ok);
            resp.put("message", ok ? "Categoría actualizada" : "Error al actualizar categoría");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping(params = "accion=eliminar_categoria")
    @ResponseBody
    public Map<String, Object> eliminarCategoria(@RequestParam int idCategoria) {
        Map<String, Object> resp = new HashMap<>();
        try {
            boolean ok = inventarioService.eliminarCategoria(idCategoria);
            resp.put("success", ok);
            resp.put("message", ok ? "Categoría eliminada" : "No se pudo eliminar (puede tener items asociados)");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    // ══════════════════════ ITEMS ══════════════════════

    @GetMapping(params = "accion=listar")
    @ResponseBody
    public List<InventarioItem> listar() {
        return inventarioService.listar();
    }

    @GetMapping(params = "accion=obtener")
    @ResponseBody
    public Object obtener(@RequestParam int id) {
        InventarioItem item = inventarioService.obtenerPorId(id);
        if (item != null) return item;
        return Map.of("error", "Ítem no encontrado");
    }

    @GetMapping(params = "accion=filtrar")
    @ResponseBody
    public List<InventarioItem> filtrar(@RequestParam(required = false, defaultValue = "") String q,
                                         @RequestParam(required = false, defaultValue = "") String categoria,
                                         @RequestParam(required = false, defaultValue = "") String estado,
                                         @RequestParam(required = false, defaultValue = "false") boolean stockBajo) {
        return inventarioService.filtrar(q, categoria, estado, stockBajo);
    }

    @GetMapping(params = "accion=stock_bajo")
    @ResponseBody
    public Map<String, Object> stockBajo() {
        return Map.of("cantidad", inventarioService.contarStockBajo());
    }

    @PostMapping(params = "accion=registrar")
    @ResponseBody
    public Map<String, Object> registrar(@RequestParam String nombre,
                                          @RequestParam(required = false) String categoria,
                                          @RequestParam(required = false) String unidadMedida,
                                          @RequestParam(required = false, defaultValue = "0") double stockMinimo,
                                          @RequestParam(required = false) String observacion) {
        Map<String, Object> resp = new HashMap<>();
        try {
            InventarioItem item = new InventarioItem();
            item.setNombre(nombre);
            item.setCategoria(categoria != null ? categoria : "");
            item.setUnidadMedida(unidadMedida != null ? unidadMedida : "");
            item.setStockMinimo(stockMinimo);
            item.setObservacion(observacion != null ? observacion : "");

            int id = inventarioService.registrar(item);
            resp.put("success", id > 0);
            resp.put("message", id > 0 ? "Ítem registrado correctamente" : "Error al registrar ítem");
            if (id > 0) resp.put("idItem", id);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping(params = "accion=actualizar")
    @ResponseBody
    public Map<String, Object> actualizar(@RequestParam int idItem,
                                           @RequestParam String nombre,
                                           @RequestParam(required = false) String categoria,
                                           @RequestParam(required = false) String unidadMedida,
                                           @RequestParam(required = false, defaultValue = "0") double stockMinimo,
                                           @RequestParam(required = false) String observacion) {
        Map<String, Object> resp = new HashMap<>();
        try {
            InventarioItem item = inventarioService.obtenerPorId(idItem);
            if (item == null) {
                resp.put("success", false);
                resp.put("message", "Ítem no encontrado");
                return resp;
            }
            item.setNombre(nombre);
            item.setCategoria(categoria != null ? categoria : "");
            item.setUnidadMedida(unidadMedida != null ? unidadMedida : "");
            item.setStockMinimo(stockMinimo);
            item.setObservacion(observacion != null ? observacion : "");

            boolean ok = inventarioService.actualizar(item);
            resp.put("success", ok);
            resp.put("message", ok ? "Ítem actualizado correctamente" : "Error al actualizar ítem");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping(params = "accion=cambiar_estado")
    @ResponseBody
    public Map<String, Object> cambiarEstado(@RequestParam int idItem,
                                              @RequestParam String estado) {
        Map<String, Object> resp = new HashMap<>();
        try {
            boolean ok = inventarioService.cambiarEstado(idItem, estado);
            resp.put("success", ok);
            resp.put("message", ok ? "Estado actualizado" : "Error al cambiar estado");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping(params = "accion=registrar_movimiento")
    @ResponseBody
    public Map<String, Object> registrarMovimiento(@RequestParam int idItem,
                                                     @RequestParam String tipo,
                                                     @RequestParam String motivo,
                                                     @RequestParam double cantidad,
                                                     @RequestParam(required = false, defaultValue = "") String observacion,
                                                     HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
            int idUsuario = usuario != null ? usuario.getIdUsuario() : 0;

            boolean ok = inventarioService.registrarMovimiento(idItem, tipo, motivo, cantidad, observacion, idUsuario);
            resp.put("success", ok);
            resp.put("message", ok ? "Movimiento registrado correctamente" : "Error al registrar movimiento");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
        }
        return resp;
    }
}
