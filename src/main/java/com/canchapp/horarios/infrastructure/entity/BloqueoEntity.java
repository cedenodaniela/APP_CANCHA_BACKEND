package com.canchapp.horarios.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Entidad JPA de la tabla {@code bloqueo} (migración V5, RF-05). */
@Entity
@Table(name = "bloqueo")
public class BloqueoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** {@code null}: bloquea todas las canchas. */
    @Column(name = "cancha_id")
    private Long canchaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "inicio_minutos", nullable = false)
    private Integer inicioMinutos;

    @Column(name = "fin_minutos", nullable = false)
    private Integer finMinutos;

    @Column(name = "motivo", nullable = false, length = 200)
    private String motivo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCanchaId() { return canchaId; }
    public void setCanchaId(Long canchaId) { this.canchaId = canchaId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Integer getInicioMinutos() { return inicioMinutos; }
    public void setInicioMinutos(Integer inicioMinutos) { this.inicioMinutos = inicioMinutos; }
    public Integer getFinMinutos() { return finMinutos; }
    public void setFinMinutos(Integer finMinutos) { this.finMinutos = finMinutos; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
