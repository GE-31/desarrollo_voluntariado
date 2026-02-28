package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "salida_inventario_detalle")
public class SalidaInventarioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private int idDetalle;

    @Column(name = "id_salida_inv")
    private int idSalidaInv;

    @Column(name = "id_item")
    private int idItem;

    private double cantidad;

    @Column(name = "stock_antes")
    private double stockAntes;

    @Column(name = "stock_despues")
    private double stockDespues;

    // ── Campos de display (Transient) ──

    @Transient
    private String itemNombre;

    @Transient
    private String itemCategoria;

    @Transient
    private String itemUnidad;

    // ── Constructores ──

    public SalidaInventarioDetalle() {
    }

    // ── Getters y Setters ──

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdSalidaInv() {
        return idSalidaInv;
    }

    public void setIdSalidaInv(int idSalidaInv) {
        this.idSalidaInv = idSalidaInv;
    }

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getStockAntes() {
        return stockAntes;
    }

    public void setStockAntes(double stockAntes) {
        this.stockAntes = stockAntes;
    }

    public double getStockDespues() {
        return stockDespues;
    }

    public void setStockDespues(double stockDespues) {
        this.stockDespues = stockDespues;
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

    public String getItemUnidad() {
        return itemUnidad;
    }

    public void setItemUnidad(String itemUnidad) {
        this.itemUnidad = itemUnidad;
    }
}
