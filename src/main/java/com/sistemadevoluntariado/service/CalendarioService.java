package com.sistemadevoluntariado.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemadevoluntariado.entity.Calendario;
import com.sistemadevoluntariado.repository.CalendarioRepository;

@Service
public class CalendarioService {

    @Autowired
    private CalendarioRepository calendarioRepository;

    @Transactional
    public List<Calendario> listarEventos() {
        return calendarioRepository.findAllByOrderByFechaInicioAsc();
    }

    @Transactional
    public boolean crearEvento(Calendario evento) {
        try {
            calendarioRepository.save(evento);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
