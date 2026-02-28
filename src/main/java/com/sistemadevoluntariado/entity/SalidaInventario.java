package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "salida_inventario")
public class SalidaInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salida_inv")
    private int idSalidaInv;

    @Column(name = "id_actividad")
    private Integer idActividad;

    private String motivo;

    private String observacion;

    @Column(name = "id_usuario_registro")
    private int idUsuarioRegistro;

    @Column(name = "registrado_en", insertable = false, updatable = false)
    private String registradoEn;

    private String estado;

    @Column(name = "anulado_en")
    private String anuladoEn;

    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;

    // ── Campos de display (no columnas de la tabla) ──

    @Transient
    private String actividadNombre;

    @Transient
    private String usuarioRegistro;

    @Transient
    private int totalItems;

    @Transient
    private double totalCantidad;

    // ── Constructores ──

    public SalidaInventario() {
    }

    // ── Getters y Setters ──

    public int getIdSalidaInv() {
        return idSalidaInv;
    }

    public void setIdSalidaInv(int idSalidaInv) {
        this.idSalidaInv = idSalidaInv;
    }

    public Integer getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(Integer idActividad) {
        this.idActividad = idActividad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public int getIdUsuarioRegistro() {
        return idUsuarioRegistro;
    }

    public void setIdUsuarioRegistro(int idUsuarioRegistro) {
        this.idUsuarioRegistro = idUsuarioRegistro;
    }

    public String getRegistradoEn() {
        return registradoEn;
    }

    public void setRegistradoEn(String registradoEn) {
        this.registradoEn = registradoEn;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getAnuladoEn() {
        return anuladoEn;
    }

    public void setAnuladoEn(String anuladoEn) {
        this.anuladoEn = anuladoEn;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public String getActividadNombre() {
        return actividadNombre;
    }

    public void setActividadNombre(String actividadNombre) {
        this.actividadNombre = actividadNombre;
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public double getTotalCantidad() {
        return totalCantidad;
    }

    public void setTotalCantidad(double totalCantidad) {
        this.totalCantidad = totalCantidad;
    }
}
