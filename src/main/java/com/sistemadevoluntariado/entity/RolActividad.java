package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol_actividad")
public class RolActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol_actividad")
    private int idRolActividad;

    @Column(name = "nombre_rol")
    private String nombreRol;

    private String descripcion;

    public RolActividad() {}

    public int getIdRolActividad() { return idRolActividad; }
    public void setIdRolActividad(int idRolActividad) { this.idRolActividad = idRolActividad; }
    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
