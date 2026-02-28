package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
 
@Entity
@Table(name = "certificados")
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_certificado")
    private int idCertificado;

    @Column(name = "codigo_certificado")
    private String codigoCertificado;

    @Column(name = "id_voluntario")
    private int idVoluntario;

    @Column(name = "id_actividad")
    private int idActividad;

    @Column(name = "horas_voluntariado")
    private int horasVoluntariado;

    @Column(name = "fecha_emision")
    private String fechaEmision;

    private String estado;

    private String observaciones;

    @Column(name = "id_usuario_emite")
    private int idUsuarioEmite;

    @Column(name = "creado_en")
    private String creadoEn;

    // Campos de display (vienen de JOINs, no son columnas de la tabla certificados)
    @Transient
    private String nombreVoluntario;
    @Transient
    private String dniVoluntario;
    @Transient
    private String nombreActividad;
    @Transient
    private String usuarioEmite;

    // Constructores
    public Certificado() {
    }

    public Certificado(int idVoluntario, int idActividad, int horasVoluntariado) {
        this.idVoluntario = idVoluntario;
        this.idActividad = idActividad;
        this.horasVoluntariado = horasVoluntariado;
        this.estado = "EMITIDO";
    }

    // Getters y Setters
    public int getIdCertificado() {
        return idCertificado;
    }

    public void setIdCertificado(int idCertificado) {
        this.idCertificado = idCertificado;
    }

    public String getCodigoCertificado() {
        return codigoCertificado;
    }

    public void setCodigoCertificado(String codigoCertificado) {
        this.codigoCertificado = codigoCertificado;
    }

    public int getIdVoluntario() {
        return idVoluntario;
    }

    public void setIdVoluntario(int idVoluntario) {
        this.idVoluntario = idVoluntario;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
    }

    public int getHorasVoluntariado() {
        return horasVoluntariado;
    }

    public void setHorasVoluntariado(int horasVoluntariado) {
        this.horasVoluntariado = horasVoluntariado;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public int getIdUsuarioEmite() {
        return idUsuarioEmite;
    }

    public void setIdUsuarioEmite(int idUsuarioEmite) {
        this.idUsuarioEmite = idUsuarioEmite;
    }

    public String getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(String creadoEn) {
        this.creadoEn = creadoEn;
    }

    // Getters para campos de joins
    public String getNombreVoluntario() {
        return nombreVoluntario;
    }

    public void setNombreVoluntario(String nombreVoluntario) {
        this.nombreVoluntario = nombreVoluntario;
    }

    public String getDniVoluntario() {
        return dniVoluntario;
    }

    public void setDniVoluntario(String dniVoluntario) {
        this.dniVoluntario = dniVoluntario;
    }

    public String getNombreActividad() {
        return nombreActividad;
    }

    public void setNombreActividad(String nombreActividad) {
        this.nombreActividad = nombreActividad;
    }

    public String getUsuarioEmite() {
        return usuarioEmite;
    }

    public void setUsuarioEmite(String usuarioEmite) {
        this.usuarioEmite = usuarioEmite;
    }
}
