package com.sistemadevoluntariado.controller;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sistemadevoluntariado.entity.Usuario;
import com.sistemadevoluntariado.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/foto-perfil")
public class FotoPerfilController {

    private static final Logger logger = Logger.getLogger(FotoPerfilController.class.getName());
    private static final String UPLOAD_DIR = "img";

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public Map<String, Object> subir(@RequestParam("foto") MultipartFile file,
                                     HttpServletRequest request,
                                     HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
            if (usuario == null) {
                return Map.of("success", false, "message", "Sesión expirada. Inicie sesión nuevamente.");
            }

            if (file == null || file.isEmpty()) {
                return Map.of("success", false, "message", "No se seleccionó ningún archivo.");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Map.of("success", false, "message", "Solo se permiten archivos de imagen (JPG, PNG, GIF).");
            }

            String appPath = request.getServletContext().getRealPath("");
            String uploadPath = appPath + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
                logger.info("► Directorio img/ creado: " + uploadPath);
            }

            String prefijo = "perfil_" + usuario.getIdUsuario();

            // Eliminar fotos anteriores
            File[] fotosAnteriores = uploadDir.listFiles((dir, name) -> name.startsWith(prefijo));
            if (fotosAnteriores != null) {
                for (File fotoVieja : fotosAnteriores) {
                    if (fotoVieja.delete()) {
                        logger.info("► Foto anterior eliminada: " + fotoVieja.getName());
                    }
                }
            }

            if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
                File fotoAnteriorBD = new File(appPath + File.separator
                        + usuario.getFotoPerfil().replace("/", File.separator));
                if (fotoAnteriorBD.exists()) {
                    fotoAnteriorBD.delete();
                }
            }

            String originalFileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            String extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
            String nuevoNombre = prefijo + extension;

            file.transferTo(new File(uploadPath + File.separator + nuevoNombre));
            logger.info("✓ Foto guardada en img/: " + nuevoNombre);

            String rutaRelativa = UPLOAD_DIR + "/" + nuevoNombre;
            boolean actualizado = usuarioService.actualizarFotoPerfil(usuario.getIdUsuario(), rutaRelativa);

            if (actualizado) {
                usuario.setFotoPerfil(rutaRelativa);
                session.setAttribute("usuarioLogeado", usuario);
                return Map.of("success", true,
                              "message", "Foto de perfil actualizada correctamente.",
                              "fotoUrl", request.getContextPath() + "/" + rutaRelativa);
            } else {
                return Map.of("success", false, "message", "Error al guardar la foto en la base de datos.");
            }
        } catch (IllegalStateException e) {
            logger.log(Level.WARNING, "Archivo demasiado grande", e);
            return Map.of("success", false, "message", "El archivo es demasiado grande. Máximo 5 MB.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al subir foto de perfil", e);
            return Map.of("success", false, "message", "Error inesperado al subir la foto.");
        }
    }
}
