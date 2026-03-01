package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "actividad_beneficiario")
public class ActividadBeneficiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad_beneficiario")
    private int idActividadBeneficiario;

    @Column(name = "id_actividad")
    private int idActividad;

    @Column(name = "id_beneficiario")
    private int idBeneficiario;

    private String observacion;

    @Transient
    private String organizacion;

    @Transient
    private String direccion;

    @Transient
    private String distrito;

    @Transient
    private String necesidadPrincipal;

    @Transient
    private String observaciones;

    @Transient
    private String nombreResponsable;

    @Transient
    private String apellidosResponsable;

    @Transient
    private String dni;

    @Transient
    private String telefono;

    public ActividadBeneficiario() {}

    public int getIdActividadBeneficiario() { return idActividadBeneficiario; }
    public void setIdActividadBeneficiario(int id) { this.idActividadBeneficiario = id; }
    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }
    public int getIdBeneficiario() { return idBeneficiario; }
    public void setIdBeneficiario(int idBeneficiario) { this.idBeneficiario = idBeneficiario; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getOrganizacion() { return organizacion; }
    public void setOrganizacion(String organizacion) { this.organizacion = organizacion; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
    public String getNecesidadPrincipal() { return necesidadPrincipal; }
    public void setNecesidadPrincipal(String necesidadPrincipal) { this.necesidadPrincipal = necesidadPrincipal; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getNombreResponsable() { return nombreResponsable; }
    public void setNombreResponsable(String nombreResponsable) { this.nombreResponsable = nombreResponsable; }
    public String getApellidosResponsable() { return apellidosResponsable; }
    public void setApellidosResponsable(String apellidosResponsable) { this.apellidosResponsable = apellidosResponsable; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
