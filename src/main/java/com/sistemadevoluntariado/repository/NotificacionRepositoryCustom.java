package com.sistemadevoluntariado.repository;

import java.util.List;

import com.sistemadevoluntariado.entity.Notificacion;

public interface NotificacionRepositoryCustom {

    List<Notificacion> listarPorUsuario(int idUsuario);

    int contarNoLeidas(int idUsuario);

    void marcarLeida(int idNotificacion);

    void marcarTodasLeidas(int idUsuario);

    void generarNotificacionesActividadesHoy(int idUsuario);

    void generarNotificacionesEventosHoy(int idUsuario);
}
