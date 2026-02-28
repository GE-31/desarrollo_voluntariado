package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "actividad_lugar")
public class ActividadLugar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad_lugar")
    private int idActividadLugar;

    @Column(name = "id_actividad")
    private int idActividad;

    @Column(name = "id_lugar")
    private int idLugar;

    @Transient
    private String nombreLugar;

    public ActividadLugar() {}

    public int getIdActividadLugar() { return idActividadLugar; }
    public void setIdActividadLugar(int idActividadLugar) { this.idActividadLugar = idActividadLugar; }
    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }
    public int getIdLugar() { return idLugar; }
    public void setIdLugar(int idLugar) { this.idLugar = idLugar; }
    public String getNombreLugar() { return nombreLugar; }
    public void setNombreLugar(String nombreLugar) { this.nombreLugar = nombreLugar; }
}
