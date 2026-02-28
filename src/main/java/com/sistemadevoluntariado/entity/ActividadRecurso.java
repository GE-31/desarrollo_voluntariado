package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "actividad_recurso")
public class ActividadRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad_recurso")
    private int idActividadRecurso;

    @Column(name = "id_actividad")
    private int idActividad;

    @Column(name = "id_recurso")
    private int idRecurso;

    @Column(name = "cantidad_requerida")
    private double cantidadRequerida;

    @Column(name = "cantidad_conseguida")
    private double cantidadConseguida;

    private String prioridad;       // ALTA, MEDIA, BAJA

    private String observacion;

    /* ── Campos transient para la vista ── */
    @Transient
    private String nombreRecurso;

    @Transient
    private String unidadMedida;

    @Transient
    private String tipoRecurso;

    @Transient
    private String nombreActividad;

    public ActividadRecurso() {}

    public int getIdActividadRecurso() { return idActividadRecurso; }
    public void setIdActividadRecurso(int idActividadRecurso) { this.idActividadRecurso = idActividadRecurso; }
    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }
    public int getIdRecurso() { return idRecurso; }
    public void setIdRecurso(int idRecurso) { this.idRecurso = idRecurso; }
    public double getCantidadRequerida() { return cantidadRequerida; }
    public void setCantidadRequerida(double cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }
    public double getCantidadConseguida() { return cantidadConseguida; }
    public void setCantidadConseguida(double cantidadConseguida) { this.cantidadConseguida = cantidadConseguida; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getNombreRecurso() { return nombreRecurso; }
    public void setNombreRecurso(String nombreRecurso) { this.nombreRecurso = nombreRecurso; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public String getTipoRecurso() { return tipoRecurso; }
    public void setTipoRecurso(String tipoRecurso) { this.tipoRecurso = tipoRecurso; }
    public String getNombreActividad() { return nombreActividad; }
    public void setNombreActividad(String nombreActividad) { this.nombreActividad = nombreActividad; }

    /** Porcentaje de avance */
    public int getPorcentaje() {
        if (cantidadRequerida <= 0) return 0;
        return (int) Math.min(100, Math.round(cantidadConseguida * 100.0 / cantidadRequerida));
    }
}
