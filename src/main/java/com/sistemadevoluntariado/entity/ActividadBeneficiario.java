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
    private String nombreBeneficiario;

    @Transient
    private String dniBeneficiario;

    @Transient
    private String tipoBeneficiario;

    public ActividadBeneficiario() {}

    public int getIdActividadBeneficiario() { return idActividadBeneficiario; }
    public void setIdActividadBeneficiario(int id) { this.idActividadBeneficiario = id; }
    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }
    public int getIdBeneficiario() { return idBeneficiario; }
    public void setIdBeneficiario(int idBeneficiario) { this.idBeneficiario = idBeneficiario; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getNombreBeneficiario() { return nombreBeneficiario; }
    public void setNombreBeneficiario(String nombreBeneficiario) { this.nombreBeneficiario = nombreBeneficiario; }
    public String getDniBeneficiario() { return dniBeneficiario; }
    public void setDniBeneficiario(String dniBeneficiario) { this.dniBeneficiario = dniBeneficiario; }
    public String getTipoBeneficiario() { return tipoBeneficiario; }
    public void setTipoBeneficiario(String tipoBeneficiario) { this.tipoBeneficiario = tipoBeneficiario; }
}
