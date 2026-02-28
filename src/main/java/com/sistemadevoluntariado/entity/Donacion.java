package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "donacion")
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_donacion")
    private int idDonacion;

    private Double cantidad;

    private String descripcion;

    @Column(name = "id_tipo_donacion")
    private int idTipoDonacion;

    @Column(name = "subtipo_donacion")
    private String subtipoDonacion;

    @Column(name = "id_actividad")
    private int idActividad;

    @Column(name = "id_usuario_registro")
    private int idUsuarioRegistro;

    @Column(name = "registrado_en", insertable = false, updatable = false)
    private String registradoEn;

    // Campos de display/formulario - NO son columnas de la tabla donacion
    @Transient
    private String tipoDonacion;
    @Transient
    private String actividad;
    @Transient
    private String usuarioRegistro;
    @Transient
    private String donanteNombre;
    @Transient
    private boolean donacionAnonima;
    @Transient
    private String tipoDonante;
    @Transient
    private String nombreDonante;
    @Transient
    private String correoDonante;
    @Transient
    private String telefonoDonante;
    @Transient
    private String dniDonante;
    @Transient
    private String rucDonante;
    @Transient
    private Integer idItem;
    @Transient
    private Double cantidadItem;
    @Transient
    private boolean crearNuevoItem;
    @Transient
    private String itemNombre;
    @Transient
    private String itemCategoria;
    @Transient
    private String itemUnidadMedida;
    @Transient
    private Double itemStockMinimo;
    @Column(name = "estado")
    private String estado;
    @Column(name = "anulado_en")
    private String anuladoEn;
    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;

    public Donacion() {
    }

    // Getters y Setters
    public int getIdDonacion() {
        return idDonacion;
    }

    public void setIdDonacion(int idDonacion) {
        this.idDonacion = idDonacion;
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

    public int getIdTipoDonacion() {
        return idTipoDonacion;
    }

    public void setIdTipoDonacion(int idTipoDonacion) {
        this.idTipoDonacion = idTipoDonacion;
    }

    public String getTipoDonacion() {
        return tipoDonacion;
    }

    public void setTipoDonacion(String tipoDonacion) {
        this.tipoDonacion = tipoDonacion;
    }

    public String getSubtipoDonacion() {
        return subtipoDonacion;
    }

    public void setSubtipoDonacion(String subtipoDonacion) {
        this.subtipoDonacion = subtipoDonacion;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public int getIdUsuarioRegistro() {
        return idUsuarioRegistro;
    }

    public void setIdUsuarioRegistro(int idUsuarioRegistro) {
        this.idUsuarioRegistro = idUsuarioRegistro;
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    public String getRegistradoEn() {
        return registradoEn;
    }

    public void setRegistradoEn(String registradoEn) {
        this.registradoEn = registradoEn;
    }

    public String getDonanteNombre() {
        return donanteNombre;
    }

    public void setDonanteNombre(String donanteNombre) {
        this.donanteNombre = donanteNombre;
    }

    public boolean isDonacionAnonima() {
        return donacionAnonima;
    }

    public void setDonacionAnonima(boolean donacionAnonima) {
        this.donacionAnonima = donacionAnonima;
    }

    public String getTipoDonante() {
        return tipoDonante;
    }

    public void setTipoDonante(String tipoDonante) {
        this.tipoDonante = tipoDonante;
    }

    public String getNombreDonante() {
        return nombreDonante;
    }

    public void setNombreDonante(String nombreDonante) {
        this.nombreDonante = nombreDonante;
    }

    public String getCorreoDonante() {
        return correoDonante;
    }

    public void setCorreoDonante(String correoDonante) {
        this.correoDonante = correoDonante;
    }

    public String getTelefonoDonante() {
        return telefonoDonante;
    }

    public void setTelefonoDonante(String telefonoDonante) {
        this.telefonoDonante = telefonoDonante;
    }

    public String getDniDonante() {
        return dniDonante;
    }

    public void setDniDonante(String dniDonante) {
        this.dniDonante = dniDonante;
    }

    public String getRucDonante() {
        return rucDonante;
    }

    public void setRucDonante(String rucDonante) {
        this.rucDonante = rucDonante;
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

    public boolean isCrearNuevoItem() {
        return crearNuevoItem;
    }

    public void setCrearNuevoItem(boolean crearNuevoItem) {
        this.crearNuevoItem = crearNuevoItem;
    }

    public String getItemNombre() {
        return itemNombre;
    }

    public void setItemNombre(String itemNombre) {
        this.itemNombre = itemNombre;
    }

    public String getItemCategoria() {
        return itemCategoria;
    }

    public void setItemCategoria(String itemCategoria) {
        this.itemCategoria = itemCategoria;
    }

    public String getItemUnidadMedida() {
        return itemUnidadMedida;
    }

    public void setItemUnidadMedida(String itemUnidadMedida) {
        this.itemUnidadMedida = itemUnidadMedida;
    }

    public Double getItemStockMinimo() {
        return itemStockMinimo;
    }

    public void setItemStockMinimo(Double itemStockMinimo) {
        this.itemStockMinimo = itemStockMinimo;
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
}
