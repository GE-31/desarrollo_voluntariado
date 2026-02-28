package com.sistemadevoluntariado.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private int idNotificacion;

    @Column(name = "id_usuario")
    private int idUsuario;

    @Column(name = "tipo")
    private String tipo; // ACTIVIDAD_HOY, BIENVENIDA, EVENTO_CALENDARIO

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "mensaje")
    private String mensaje;

    @Column(name = "icono")
    private String icono;

    @Column(name = "color")
    private String color;

    @Column(name = "leida")
    private boolean leida;

    @Column(name = "referencia_id")
    private int referenciaId;

    @Column(name = "fecha_creacion")
    private String fechaCreacion;

    public Notificacion() {
    }

    public Notificacion(int idUsuario, String tipo, String titulo, String mensaje, String icono, String color,
            int referenciaId) {
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.icono = icono;
        this.color = color;
        this.referenciaId = referenciaId;
    }

    // Getters y Setters
    public int getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(int idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public int getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(int referenciaId) {
        this.referenciaId = referenciaId;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
