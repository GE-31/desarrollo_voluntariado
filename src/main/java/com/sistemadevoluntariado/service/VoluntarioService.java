package com.sistemadevoluntariado.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Voluntario;
import com.sistemadevoluntariado.repository.VoluntarioRepository;

@Service
public class VoluntarioService {

    private static final Logger logger = Logger.getLogger(VoluntarioService.class.getName());

    @Autowired
    private VoluntarioRepository voluntarioRepository;

    @Transactional(readOnly = true)
    public List<Voluntario> obtenerTodosVoluntarios() {
        return voluntarioRepository.findAllByOrderByIdVoluntarioDesc();
    }

    @Transactional(readOnly = true)
    public Voluntario obtenerVoluntarioPorId(int id) {
        return voluntarioRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public Voluntario obtenerVoluntarioPorUsuarioId(int idUsuario) {
        return voluntarioRepository.findFirstByIdUsuarioOrderByIdVoluntarioDesc(idUsuario).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Voluntario> obtenerVoluntariosConAcceso() {
        return voluntarioRepository.obtenerVoluntariosConAcceso();
    }

    @Transactional(readOnly = true)
    public List<Voluntario> obtenerVoluntariosConAsistencia() {
        return voluntarioRepository.obtenerVoluntariosConAsistencia();
    }

    @Transactional
    public boolean crearVoluntario(Voluntario voluntario) {
        try {
            return voluntarioRepository.crearVoluntario(voluntario);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al crear voluntario: " + e.getMessage(), e);
            String msg = extraerMensajeError(e);
            throw new RuntimeException(msg, e);
        }
    }

    @Transactional
    public boolean actualizarVoluntario(Voluntario voluntario) {
        try {
            return voluntarioRepository.actualizarVoluntario(voluntario);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar voluntario: " + e.getMessage(), e);
            String msg = extraerMensajeError(e);
            throw new RuntimeException(msg, e);
        }
    }

    @Transactional
    public boolean cambiarEstado(int id, String estado) {
        try {
            return voluntarioRepository.cambiarEstado(id, estado);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cambiar estado: " + e.getMessage(), e);
            throw new RuntimeException("Error al cambiar estado del voluntario", e);
        }
    }

    @Transactional
    public boolean eliminarVoluntario(int id) {
        try {
            return voluntarioRepository.eliminarVoluntario(id);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al eliminar voluntario: " + e.getMessage(), e);
            throw new RuntimeException("No se pudo eliminar: puede tener registros asociados", e);
        }
    }

    /**
     * Extrae un mensaje de error legible a partir de la excepción de base de datos.
     */
    private String extraerMensajeError(Exception e) {
        String full = obtenerMensajeRaiz(e).toLowerCase();
        if (full.contains("duplicate") || full.contains("unique")) {
            if (full.contains("dni"))      return "Ya existe un voluntario con ese DNI";
            if (full.contains("correo"))   return "Ya existe un voluntario con ese correo electrónico";
            if (full.contains("telefono")) return "Ya existe un voluntario con ese teléfono";
            return "Ya existe un voluntario con esos datos (dato duplicado)";
        }
        return "Error al procesar el voluntario. Verifica los datos e intenta de nuevo";
    }

    private String obtenerMensajeRaiz(Throwable t) {
        Throwable causa = t;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        return causa.getMessage() != null ? causa.getMessage() : "";
    }
}
