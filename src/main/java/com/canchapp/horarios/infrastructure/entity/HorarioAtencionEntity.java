package com.canchapp.horarios.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Entidad JPA de la tabla {@code horario_atencion} (migración V5, RF-05). Una fila por día. */
@Entity
@Table(name = "horario_atencion")
public class HorarioAtencionEntity {

    /** 1 = lunes … 7 = domingo. */
    @Id
    @Column(name = "dia_semana")
    private Short diaSemana;

    @Column(name = "abierto", nullable = false)
    private Boolean abierto;

    @Column(name = "apertura_minutos")
    private Integer aperturaMinutos;

    @Column(name = "cierre_minutos")
    private Integer cierreMinutos;

    public Short getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Short diaSemana) { this.diaSemana = diaSemana; }
    public Boolean getAbierto() { return abierto; }
    public void setAbierto(Boolean abierto) { this.abierto = abierto; }
    public Integer getAperturaMinutos() { return aperturaMinutos; }
    public void setAperturaMinutos(Integer aperturaMinutos) { this.aperturaMinutos = aperturaMinutos; }
    public Integer getCierreMinutos() { return cierreMinutos; }
    public void setCierreMinutos(Integer cierreMinutos) { this.cierreMinutos = cierreMinutos; }
}
