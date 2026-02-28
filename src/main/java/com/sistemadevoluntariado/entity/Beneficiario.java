package com.sistemadevoluntariado.entity;

import java.time.LocalDate;
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

    private String nombres;

    private String apellidos;

    private String dni;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String telefono;

    private String direccion;

    private String distrito;

    @Column(name = "tipo_beneficiario")
    private String tipoBeneficiario;   // INDIVIDUAL, FAMILIA, COMUNIDAD

    @Column(name = "necesidad_principal")
    private String necesidadPrincipal; // ALIMENTACIÓN, SALUD, EDUCACIÓN, VIVIENDA, OTRO

    private String observaciones;

    private String estado;             // ACTIVO, INACTIVO

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    // Constructores
    public Beneficiario() {
    }

    public Beneficiario(String nombres, String apellidos, String dni, String telefono,
            String direccion, String distrito, String tipoBeneficiario,
            String necesidadPrincipal) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
        this.distrito = distrito;
        this.tipoBeneficiario = tipoBeneficiario;
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

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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

    public String getTipoBeneficiario() {
        return tipoBeneficiario;
    }

    public void setTipoBeneficiario(String tipoBeneficiario) {
        this.tipoBeneficiario = tipoBeneficiario;
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
