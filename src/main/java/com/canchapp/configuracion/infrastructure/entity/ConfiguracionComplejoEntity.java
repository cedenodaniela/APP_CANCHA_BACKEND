package com.canchapp.configuracion.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Entidad JPA de la tabla {@code configuracion_complejo} (migración V3, RF-03). Siempre con id 1. */
@Entity
@Table(name = "configuracion_complejo")
public class ConfiguracionComplejoEntity {

    /** Única fila de la tabla. */
    public static final short ID_UNICO = 1;

    @Id
    @Column(name = "id")
    private Short id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "banco", nullable = false, length = 100)
    private String banco;

    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private String tipoCuenta;

    @Column(name = "numero_cuenta", nullable = false, length = 30)
    private String numeroCuenta;

    @Column(name = "titular_cuenta", nullable = false, length = 120)
    private String titularCuenta;

    @Column(name = "minutos_expiracion", nullable = false)
    private Integer minutosExpiracion;

    @Column(name = "horas_plazo_cancelacion", nullable = false)
    private Integer horasPlazoCancelacion;

    @Column(name = "max_reservas_efectivo", nullable = false)
    private Integer maxReservasEfectivo;

    @Column(name = "dias_anticipacion", nullable = false)
    private Integer diasAnticipacion;

    @Column(name = "limitar_tras_inasistencia", nullable = false)
    private Boolean limitarTrasInasistencia;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }
    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }
    public String getTitularCuenta() { return titularCuenta; }
    public void setTitularCuenta(String titularCuenta) { this.titularCuenta = titularCuenta; }
    public Integer getMinutosExpiracion() { return minutosExpiracion; }
    public void setMinutosExpiracion(Integer minutosExpiracion) { this.minutosExpiracion = minutosExpiracion; }
    public Integer getHorasPlazoCancelacion() { return horasPlazoCancelacion; }
    public void setHorasPlazoCancelacion(Integer horasPlazoCancelacion) { this.horasPlazoCancelacion = horasPlazoCancelacion; }
    public Integer getMaxReservasEfectivo() { return maxReservasEfectivo; }
    public void setMaxReservasEfectivo(Integer maxReservasEfectivo) { this.maxReservasEfectivo = maxReservasEfectivo; }
    public Integer getDiasAnticipacion() { return diasAnticipacion; }
    public void setDiasAnticipacion(Integer diasAnticipacion) { this.diasAnticipacion = diasAnticipacion; }
    public Boolean getLimitarTrasInasistencia() { return limitarTrasInasistencia; }
    public void setLimitarTrasInasistencia(Boolean limitarTrasInasistencia) { this.limitarTrasInasistencia = limitarTrasInasistencia; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
