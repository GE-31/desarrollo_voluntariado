package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "participacion")
public class Participacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participacion")
    private int idParticipacion;

    @Column(name = "id_voluntario")
    private int idVoluntario;

    @Column(name = "id_actividad")
    private int idActividad;

    @Column(name = "id_rol_actividad")
    private Integer idRolActividad;

    /* ── Campos transient para la vista ── */
    @Transient
    private String nombreVoluntario;

    @Transient
    private String dniVoluntario;

    @Transient
    private String nombreActividad;

    @Transient
    private String nombreRol;

    @Transient
    private String carreraVoluntario;

    public Participacion() {}

    public int getIdParticipacion() { return idParticipacion; }
    public void setIdParticipacion(int idParticipacion) { this.idParticipacion = idParticipacion; }
    public int getIdVoluntario() { return idVoluntario; }
    public void setIdVoluntario(int idVoluntario) { this.idVoluntario = idVoluntario; }
    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }
    public Integer getIdRolActividad() { return idRolActividad; }
    public void setIdRolActividad(Integer idRolActividad) { this.idRolActividad = idRolActividad; }
    public String getNombreVoluntario() { return nombreVoluntario; }
    public void setNombreVoluntario(String nombreVoluntario) { this.nombreVoluntario = nombreVoluntario; }
    public String getDniVoluntario() { return dniVoluntario; }
    public void setDniVoluntario(String dniVoluntario) { this.dniVoluntario = dniVoluntario; }
    public String getNombreActividad() { return nombreActividad; }
    public void setNombreActividad(String nombreActividad) { this.nombreActividad = nombreActividad; }
    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
    public String getCarreraVoluntario() { return carreraVoluntario; }
    public void setCarreraVoluntario(String carreraVoluntario) { this.carreraVoluntario = carreraVoluntario; }
}
