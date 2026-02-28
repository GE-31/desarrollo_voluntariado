package com.sistemadevoluntariado.controller;

import java.time.LocalDate;
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

import com.sistemadevoluntariado.entity.Beneficiario;
import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.BeneficiarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/beneficiarios")
public class BeneficiarioController {

    @Autowired
    private BeneficiarioService beneficiarioService;

    /* ───── Vista principal ───── */
    @GetMapping
    public String vista(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        if (usuario == null) return "redirect:/login";
        model.addAttribute("usuario", usuario);
        model.addAttribute("beneficiarios", beneficiarioService.obtenerTodosBeneficiarios());
        return "views/beneficiarios/listar";
    }

    @GetMapping(params = "action=listar")
    @ResponseBody
    public List<Beneficiario> listar() {
        return beneficiarioService.obtenerTodosBeneficiarios();
    }

    @GetMapping(params = "action=obtener")
    @ResponseBody
    public Beneficiario obtener(@RequestParam int id) {
        return beneficiarioService.obtenerBeneficiarioPorId(id);
    }

    @PostMapping(params = "action=crear")
    @ResponseBody
    public Map<String, Object> crear(@RequestParam String nombres,
                                     @RequestParam String apellidos,
                                     @RequestParam String dni,
                                     @RequestParam(required = false) String fechaNacimiento,
                                     @RequestParam(required = false) String telefono,
                                     @RequestParam(required = false) String direccion,
                                     @RequestParam(required = false) String distrito,
                                     @RequestParam(required = false) String tipoBeneficiario,
                                     @RequestParam(required = false) String necesidadPrincipal,
                                     @RequestParam(required = false) String observaciones,
                                     HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
            if (usuario == null) return Map.of("success", false, "message", "No autorizado");

            Beneficiario b = new Beneficiario(nombres, apellidos, dni, telefono, direccion,
                    distrito, tipoBeneficiario, necesidadPrincipal);
            if (fechaNacimiento != null && !fechaNacimiento.trim().isEmpty()) {
                b.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
            }
            b.setObservaciones(observaciones);
            b.setIdUsuario(usuario.getIdUsuario());

            int newId = beneficiarioService.crearBeneficiario(b);
            return newId > 0
                    ? Map.of("success", true, "message", "Beneficiario registrado correctamente", "idBeneficiario", newId)
                    : Map.of("success", false, "message", "Error al registrar el beneficiario");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error interno: " + e.getMessage());
        }
    }

    @PostMapping(params = "action=editar")
    @ResponseBody
    public Map<String, Object> editar(@RequestParam int id,
                                      @RequestParam String nombres,
                                      @RequestParam String apellidos,
                                      @RequestParam String dni,
                                      @RequestParam(required = false) String fechaNacimiento,
                                      @RequestParam(required = false) String telefono,
                                      @RequestParam(required = false) String direccion,
                                      @RequestParam(required = false) String distrito,
                                      @RequestParam(required = false) String tipoBeneficiario,
                                      @RequestParam(required = false) String necesidadPrincipal,
                                      @RequestParam(required = false) String observaciones) {
        try {
            Beneficiario b = new Beneficiario();
            b.setIdBeneficiario(id);
            b.setNombres(nombres);
            b.setApellidos(apellidos);
            b.setDni(dni);
            b.setTelefono(telefono);
            b.setDireccion(direccion);
            b.setDistrito(distrito);
            b.setTipoBeneficiario(tipoBeneficiario);
            b.setNecesidadPrincipal(necesidadPrincipal);
            b.setObservaciones(observaciones);
            if (fechaNacimiento != null && !fechaNacimiento.trim().isEmpty()) {
                b.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
            }

            boolean ok = beneficiarioService.actualizarBeneficiario(b);
            return ok ? Map.of("success", true, "message", "Beneficiario actualizado correctamente")
                      : Map.of("success", false, "message", "Error al actualizar el beneficiario");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @PostMapping(params = "action=actualizar")
    @ResponseBody
    public Map<String, Object> actualizar(@RequestParam int id,
                                          @RequestParam String nombres,
                                          @RequestParam String apellidos,
                                          @RequestParam String dni,
                                          @RequestParam(required = false) String fechaNacimiento,
                                          @RequestParam(required = false) String telefono,
                                          @RequestParam(required = false) String direccion,
                                          @RequestParam(required = false) String distrito,
                                          @RequestParam(required = false) String tipoBeneficiario,
                                          @RequestParam(required = false) String necesidadPrincipal,
                                          @RequestParam(required = false) String observaciones) {
        return editar(id, nombres, apellidos, dni, fechaNacimiento, telefono, direccion,
                distrito, tipoBeneficiario, necesidadPrincipal, observaciones);
    }

    @PostMapping(params = "action=cambiarEstado")
    @ResponseBody
    public Map<String, Object> cambiarEstado(@RequestParam int id, @RequestParam String estado) {
        boolean ok = beneficiarioService.cambiarEstado(id, estado);
        return ok ? Map.of("success", true, "message", "Estado actualizado correctamente")
                  : Map.of("success", false, "message", "Error al cambiar el estado");
    }

    @PostMapping(params = "action=eliminar")
    @ResponseBody
    public Map<String, Object> eliminar(@RequestParam int id) {
        boolean ok = beneficiarioService.eliminarBeneficiario(id);
        return ok ? Map.of("success", true, "message", "Beneficiario eliminado correctamente")
                  : Map.of("success", false, "message", "Error al eliminar el beneficiario");
    }
}
