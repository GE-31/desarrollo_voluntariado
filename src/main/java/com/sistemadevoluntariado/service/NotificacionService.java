package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Notificacion;
import com.sistemadevoluntariado.repository.NotificacionRepository;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Transactional
    public List<Notificacion> listarPorUsuario(int idUsuario) {
        return notificacionRepository.listarPorUsuario(idUsuario);
    }

    @Transactional
    public int contarNoLeidas(int idUsuario) {
        return notificacionRepository.contarNoLeidas(idUsuario);
    }

    @Transactional
    public void marcarLeida(int idNotificacion) {
        notificacionRepository.marcarLeida(idNotificacion);
    }

    @Transactional
    public void marcarTodasLeidas(int idUsuario) {
        notificacionRepository.marcarTodasLeidas(idUsuario);
    }

    @Transactional
    public void generarNotificacionesActividadesHoy(int idUsuario) {
        notificacionRepository.generarNotificacionesActividadesHoy(idUsuario);
    }

    @Transactional
    public void generarNotificacionesEventosHoy(int idUsuario) {
        notificacionRepository.generarNotificacionesEventosHoy(idUsuario);
    }
}
