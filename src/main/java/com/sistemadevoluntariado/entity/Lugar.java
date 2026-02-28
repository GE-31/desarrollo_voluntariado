package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lugar")
public class Lugar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lugar")
    private int idLugar;

    private String departamento;
    private String provincia;
    private String distrito;

    @Column(name = "direccion_referencia")
    private String direccionReferencia;

    public Lugar() {}

    public Lugar(String departamento, String provincia, String distrito, String direccionReferencia) {
        this.departamento = departamento;
        this.provincia = provincia;
        this.distrito = distrito;
        this.direccionReferencia = direccionReferencia;
    }

    public int getIdLugar() { return idLugar; }
    public void setIdLugar(int idLugar) { this.idLugar = idLugar; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
    public String getDireccionReferencia() { return direccionReferencia; }
    public void setDireccionReferencia(String direccionReferencia) { this.direccionReferencia = direccionReferencia; }

    /** Representación legible: "Distrito, Provincia – Departamento" */
    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        if (distrito != null && !distrito.isBlank()) sb.append(distrito);
        if (provincia != null && !provincia.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(provincia);
        }
        if (departamento != null && !departamento.isBlank()) {
            if (!sb.isEmpty()) sb.append(" – ");
            sb.append(departamento);
        }
        return sb.toString();
    }
}
