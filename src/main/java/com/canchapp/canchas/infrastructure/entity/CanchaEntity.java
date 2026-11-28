package com.canchapp.canchas.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Entidad JPA de la tabla {@code cancha} (migración V4, RF-04). */
@Entity
@Table(name = "cancha")
public class CanchaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Column(name = "tipo_cesped", nullable = false, length = 40)
    private String tipoCesped;

    @Column(name = "ancho_metros", nullable = false)
    private Integer anchoMetros;

    @Column(name = "largo_metros", nullable = false)
    private Integer largoMetros;

    @Column(name = "clave_foto", length = 100)
    private String claveFoto;

    @Column(name = "activa", nullable = false)
    private Boolean activa;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipoCesped() { return tipoCesped; }
    public void setTipoCesped(String tipoCesped) { this.tipoCesped = tipoCesped; }
    public Integer getAnchoMetros() { return anchoMetros; }
    public void setAnchoMetros(Integer anchoMetros) { this.anchoMetros = anchoMetros; }
    public Integer getLargoMetros() { return largoMetros; }
    public void setLargoMetros(Integer largoMetros) { this.largoMetros = largoMetros; }
    public String getClaveFoto() { return claveFoto; }
    public void setClaveFoto(String claveFoto) { this.claveFoto = claveFoto; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
