package com.sistemadevoluntariado.controller;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    private boolean esTextoValido(String texto) {
        return texto != null && Pattern.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", texto);
    }

    private boolean esDniValido(String dni) {
        return dni != null && dni.matches("^\\d{8}$");
    }

    private boolean esTelefonoValido(String telefono) {
        return telefono != null && telefono.matches("^\\d{9}$");
    }

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
    public Map<String, Object> crear(
            @RequestParam(required = false) String organizacion,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) String necesidadPrincipal,
            @RequestParam(required = false) String observaciones,
            @RequestParam String nombreResponsable,
            @RequestParam String apellidosResponsable,
            @RequestParam String dni,
            @RequestParam(required = false) String telefono,
            HttpSession session) {
        try {
            if (!esTextoValido(distrito) || !esTextoValido(necesidadPrincipal) || !esTextoValido(nombreResponsable) || !esTextoValido(apellidosResponsable)) {
                return Map.of("success", false, "message", "Los campos de texto no deben contener números ni caracteres especiales.");
            }

            if (!esDniValido(dni)) {
                return Map.of("success", false, "message", "El DNI debe contener exactamente 8 dígitos.");
            }

            if (telefono != null && !esTelefonoValido(telefono)) {
                return Map.of("success", false, "message", "El teléfono debe contener exactamente 9 dígitos.");
            }

            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
            if (usuario == null) return Map.of("success", false, "message", "No autorizado");

            Beneficiario b = new Beneficiario();
            b.setOrganizacion(organizacion);
            b.setDireccion(direccion);
            b.setDistrito(distrito);
            b.setNecesidadPrincipal(necesidadPrincipal);
            b.setObservaciones(observaciones);
            b.setNombreResponsable(nombreResponsable);
            b.setApellidosResponsable(apellidosResponsable);
            b.setDni(dni);
            b.setTelefono(telefono);
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
    public Map<String, Object> editar(
            @RequestParam int id,
            @RequestParam(required = false) String organizacion,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) String necesidadPrincipal,
            @RequestParam(required = false) String observaciones,
            @RequestParam String nombreResponsable,
            @RequestParam String apellidosResponsable,
            @RequestParam String dni,
            @RequestParam(required = false) String telefono) {
        try {
            if (!esTextoValido(distrito) || !esTextoValido(necesidadPrincipal) || !esTextoValido(nombreResponsable) || !esTextoValido(apellidosResponsable)) {
                return Map.of("success", false, "message", "Los campos de texto no deben contener números ni caracteres especiales.");
            }

            if (!esDniValido(dni)) {
                return Map.of("success", false, "message", "El DNI debe contener exactamente 8 dígitos.");
            }

            if (telefono != null && !esTelefonoValido(telefono)) {
                return Map.of("success", false, "message", "El teléfono debe contener exactamente 9 dígitos.");
            }

            Beneficiario b = new Beneficiario();
            b.setIdBeneficiario(id);
            b.setOrganizacion(organizacion);
            b.setDireccion(direccion);
            b.setDistrito(distrito);
            b.setNecesidadPrincipal(necesidadPrincipal);
            b.setObservaciones(observaciones);
            b.setNombreResponsable(nombreResponsable);
            b.setApellidosResponsable(apellidosResponsable);
            b.setDni(dni);
            b.setTelefono(telefono);

            boolean ok = beneficiarioService.actualizarBeneficiario(b);
            return ok ? Map.of("success", true, "message", "Beneficiario actualizado correctamente")
                      : Map.of("success", false, "message", "Error al actualizar el beneficiario");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @PostMapping(params = "action=actualizar")
    @ResponseBody
    public Map<String, Object> actualizar(
            @RequestParam int id,
            @RequestParam(required = false) String organizacion,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) String necesidadPrincipal,
            @RequestParam(required = false) String observaciones,
            @RequestParam String nombreResponsable,
            @RequestParam String apellidosResponsable,
            @RequestParam String dni,
            @RequestParam(required = false) String telefono) {
        return editar(id, organizacion, direccion, distrito, necesidadPrincipal, observaciones, nombreResponsable, apellidosResponsable, dni, telefono);
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
