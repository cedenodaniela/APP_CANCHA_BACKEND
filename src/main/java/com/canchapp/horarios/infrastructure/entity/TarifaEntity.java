package com.canchapp.horarios.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Entidad JPA de la tabla {@code tarifa} (migración V5, RF-05). */
@Entity
@Table(name = "tarifa")
public class TarifaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Un bit por día: lunes = 1 … domingo = 64. */
    @Column(name = "dias_semana", nullable = false)
    private Short diasSemana;

    @Column(name = "inicio_minutos", nullable = false)
    private Integer inicioMinutos;

    @Column(name = "fin_minutos", nullable = false)
    private Integer finMinutos;

    @Column(name = "precio_hora", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioHora;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Short getDiasSemana() { return diasSemana; }
    public void setDiasSemana(Short diasSemana) { this.diasSemana = diasSemana; }
    public Integer getInicioMinutos() { return inicioMinutos; }
    public void setInicioMinutos(Integer inicioMinutos) { this.inicioMinutos = inicioMinutos; }
    public Integer getFinMinutos() { return finMinutos; }
    public void setFinMinutos(Integer finMinutos) { this.finMinutos = finMinutos; }
    public BigDecimal getPrecioHora() { return precioHora; }
    public void setPrecioHora(BigDecimal precioHora) { this.precioHora = precioHora; }
}
