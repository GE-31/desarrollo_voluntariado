package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "salida_donacion")
public class SalidaDonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salida")
    private int idSalida;

    @Column(name = "id_donacion")
    private int idDonacion;

    @Column(name = "id_actividad")
    private int idActividad;

    @Column(name = "tipo_salida")
    private String tipoSalida;

    private Double cantidad;

    private String descripcion;

    @Column(name = "id_item")
    private Integer idItem;

    @Column(name = "cantidad_item")
    private Double cantidadItem;

    @Column(name = "id_usuario_registro")
    private int idUsuarioRegistro;

    @Column(name = "registrado_en", insertable = false, updatable = false)
    private String registradoEn;

    @Column(name = "estado")
    private String estado;

    @Column(name = "anulado_en")
    private String anuladoEn;

    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;

    // ── Campos de display (no columnas de la tabla) ──

    @Transient
    private Double donacionCantidad;

    @Transient
    private String tipoDonacionNombre;

    @Transient
    private String actividadNombre;

    @Transient
    private String usuarioRegistro;

    @Transient
    private String itemNombre;

    @Transient
    private String itemUnidadMedida;

    @Transient
    private String donanteNombre;

    @Transient
    private String donacionDescripcion;

    public SalidaDonacion() {
    }

    // ── Getters y Setters ──

    public int getIdSalida() {
        return idSalida;
    }

    public void setIdSalida(int idSalida) {
        this.idSalida = idSalida;
    }

    public int getIdDonacion() {
        return idDonacion;
    }

    public void setIdDonacion(int idDonacion) {
        this.idDonacion = idDonacion;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
    }

    public String getTipoSalida() {
        return tipoSalida;
    }

    public void setTipoSalida(String tipoSalida) {
        this.tipoSalida = tipoSalida;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getIdItem() {
        return idItem;
    }

    public void setIdItem(Integer idItem) {
        this.idItem = idItem;
    }

    public Double getCantidadItem() {
        return cantidadItem;
    }

    public void setCantidadItem(Double cantidadItem) {
        this.cantidadItem = cantidadItem;
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

    // ── Transient getters/setters ──

    public Double getDonacionCantidad() {
        return donacionCantidad;
    }

    public void setDonacionCantidad(Double donacionCantidad) {
        this.donacionCantidad = donacionCantidad;
    }

    public String getTipoDonacionNombre() {
        return tipoDonacionNombre;
    }

    public void setTipoDonacionNombre(String tipoDonacionNombre) {
        this.tipoDonacionNombre = tipoDonacionNombre;
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

    public String getItemNombre() {
        return itemNombre;
    }

    public void setItemNombre(String itemNombre) {
        this.itemNombre = itemNombre;
    }

    public String getItemUnidadMedida() {
        return itemUnidadMedida;
    }

    public void setItemUnidadMedida(String itemUnidadMedida) {
        this.itemUnidadMedida = itemUnidadMedida;
    }

    public String getDonanteNombre() {
        return donanteNombre;
    }

    public void setDonanteNombre(String donanteNombre) {
        this.donanteNombre = donanteNombre;
    }

    public String getDonacionDescripcion() {
        return donacionDescripcion;
    }

    public void setDonacionDescripcion(String donacionDescripcion) {
        this.donacionDescripcion = donacionDescripcion;
    }
}
