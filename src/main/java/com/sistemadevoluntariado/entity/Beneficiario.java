package com.sistemadevoluntariado.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "beneficiario")
public class Beneficiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_beneficiario")
    private int idBeneficiario;

    private String organizacion;

    private String direccion;

    private String distrito;

    @Column(name = "necesidad_principal")
    private String necesidadPrincipal;

    private String observaciones;

    @Column(name = "nombre_responsable")
    private String nombreResponsable;

    @Column(name = "apellidos_responsable")
    private String apellidosResponsable;

    private String dni;

    private String telefono;

    private String estado;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    // Constructores
    public Beneficiario() {
    }

    public Beneficiario(String nombreResponsable, String apellidosResponsable, String dni,
            String telefono, String direccion, String distrito,
            String organizacion, String necesidadPrincipal) {
        this.nombreResponsable = nombreResponsable;
        this.apellidosResponsable = apellidosResponsable;
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
        this.distrito = distrito;
        this.organizacion = organizacion;
        this.necesidadPrincipal = necesidadPrincipal;
        this.estado = "ACTIVO";
    }

    // Getters y Setters
    public int getIdBeneficiario() {
        return idBeneficiario;
    }

    public void setIdBeneficiario(int idBeneficiario) {
        this.idBeneficiario = idBeneficiario;
    }

    public String getOrganizacion() {
        return organizacion;
    }

    public void setOrganizacion(String organizacion) {
        this.organizacion = organizacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getNecesidadPrincipal() {
        return necesidadPrincipal;
    }

    public void setNecesidadPrincipal(String necesidadPrincipal) {
        this.necesidadPrincipal = necesidadPrincipal;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getNombreResponsable() {
        return nombreResponsable;
    }

    public void setNombreResponsable(String nombreResponsable) {
        this.nombreResponsable = nombreResponsable;
    }

    public String getApellidosResponsable() {
        return apellidosResponsable;
    }

    public void setApellidosResponsable(String apellidosResponsable) {
        this.apellidosResponsable = apellidosResponsable;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
