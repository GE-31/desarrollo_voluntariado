package com.sistemadevoluntariado.repository;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class UsuarioRepositoryImpl implements UsuarioRepositoryCustom {

    private static final Logger logger = Logger.getLogger(UsuarioRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean registrarUsuarioConVoluntario(int voluntarioId, int rolSistemaId,
                                                  String username, String password) {
        try {
            String hash = new BCryptPasswordEncoder().encode(password);
            try {
                Object resultado = em.createNativeQuery(
                        "CALL sp_registrar_usuario_con_voluntario(?1, ?2, ?3, ?4)")
                        .setParameter(1, voluntarioId)
                        .setParameter(2, rolSistemaId)
                        .setParameter(3, username)
                        .setParameter(4, hash)
                        .getSingleResult();
                int nuevoId = ((Number) resultado).intValue();
                if (nuevoId > 0) {
                    logger.info("Usuario creado con ID: " + nuevoId + " - username: " + username);
                    return true;
                }
            } catch (Exception ignored) {
                // Fallback para esquemas que no tienen sp_registrar_usuario_con_voluntario.
            }

            Object[] datosVol = (Object[]) em.createNativeQuery(
                    "SELECT nombres, apellidos, correo, dni FROM voluntario WHERE id_voluntario = ?1")
                    .setParameter(1, voluntarioId)
                    .getSingleResult();
            String nombres = (String) datosVol[0];
            String apellidos = (String) datosVol[1];
            String correo = (String) datosVol[2];
            String dni = (String) datosVol[3];

            Object resultado = em.createNativeQuery(
                    "CALL sp_crear_usuario(?1, ?2, ?3, ?4, ?5, ?6)")
                    .setParameter(1, nombres)
                    .setParameter(2, apellidos)
                    .setParameter(3, correo)
                    .setParameter(4, username)
                    .setParameter(5, dni)
                    .setParameter(6, hash)
                    .getSingleResult();
            int nuevoId = ((Number) resultado).intValue();
            if (nuevoId > 0) {
                em.createNativeQuery("UPDATE voluntario SET id_usuario = ?1 WHERE id_voluntario = ?2")
                        .setParameter(1, nuevoId)
                        .setParameter(2, voluntarioId)
                        .executeUpdate();
                logger.info("Usuario creado con ID: " + nuevoId + " - username: " + username);
                return true;
            }
            logger.warning("No se pudo crear el usuario: " + username);
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al registrar usuario con voluntario", e);
            return false;
        }
    }

    @Override
    public int cambiarEstado(int idUsuario, String estado) {
        try {
            Object resultado = em.createNativeQuery(
                    "CALL sp_cambiar_estado_usuario(?1, ?2)")
                    .setParameter(1, idUsuario)
                    .setParameter(2, estado)
                    .getSingleResult();
            return ((Number) resultado).intValue();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al cambiar estado de usuario", e);
            return 0;
        }
    }

    @Override
    public int actualizarFotoPerfil(int idUsuario, String fotoPerfil) {
        try {
            Object resultado = em.createNativeQuery(
                    "CALL sp_actualizar_foto_perfil(?1, ?2)")
                    .setParameter(1, idUsuario)
                    .setParameter(2, fotoPerfil)
                    .getSingleResult();
            return ((Number) resultado).intValue();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar foto de perfil", e);
            return 0;
        }
    }
}
