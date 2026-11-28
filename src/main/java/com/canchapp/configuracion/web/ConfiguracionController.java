package com.canchapp.configuracion.web;

import com.canchapp.configuracion.application.ActualizarConfiguracionUseCase;
import com.canchapp.configuracion.application.ActualizarConfiguracionUseCase.DatosConfiguracion;
import com.canchapp.configuracion.application.ConsultarConfiguracionUseCase;
import com.canchapp.configuracion.domain.ConfiguracionComplejo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Configuración del complejo (RF-03).
 * <p>
 * Cualquier usuario con sesión la consulta: el cliente necesita los datos bancarios para pagar
 * por transferencia (RF-08). Solo el administrador la cambia.
 */
@RestController
@RequestMapping("/api/v1/configuracion")
public class ConfiguracionController {

    /** Datos generales del complejo. */
    public record ComplejoDto(String nombre, String direccion, String telefono) {
    }

    /** Cuenta a la que el cliente transfiere. {@code tipoCuenta} es «AHORROS» o «CORRIENTE». */
    public record DatosBancariosDto(String banco, String tipoCuenta, String numeroCuenta, String titular) {
    }

    /** Parámetros de las reglas de reserva (RN-02, RN-05, RN-07, RN-08, RN-09). */
    public record PoliticaDto(Integer minutosExpiracion, Integer horasPlazoCancelacion, Integer maxReservasEfectivo,
                              Integer diasAnticipacion, Boolean limitarTrasInasistencia) {
    }

    /** Cuerpo de {@code PUT}: la configuración completa. */
    public record SolicitudConfiguracion(ComplejoDto complejo, DatosBancariosDto datosBancarios, PoliticaDto politica) {
    }

    /** Respuesta de {@code GET} y {@code PUT}. */
    public record ConfiguracionRespuesta(ComplejoDto complejo, DatosBancariosDto datosBancarios, PoliticaDto politica,
                                         Instant actualizadaEn) {
    }

    private final ConsultarConfiguracionUseCase consultar;
    private final ActualizarConfiguracionUseCase actualizar;

    public ConfiguracionController(ConsultarConfiguracionUseCase consultar, ActualizarConfiguracionUseCase actualizar) {
        this.consultar = consultar;
        this.actualizar = actualizar;
    }

    /** @return 200 con la configuración; 401 sin sesión */
    @GetMapping
    public ConfiguracionRespuesta consultar() {
        return aRespuesta(consultar.ejecutar());
    }

    /**
     * Reemplaza la configuración completa.
     *
     * @return 200 con lo guardado; 400 {@code CONFIGURACION_INVALIDA} si falta un dato o un valor está
     *         fuera de rango; 403 si no es administrador
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ConfiguracionRespuesta actualizar(@RequestBody SolicitudConfiguracion solicitud) {
        ComplejoDto complejo = solicitud.complejo() != null ? solicitud.complejo() : new ComplejoDto(null, null, null);
        DatosBancariosDto banco = solicitud.datosBancarios() != null
                ? solicitud.datosBancarios() : new DatosBancariosDto(null, null, null, null);
        PoliticaDto politica = solicitud.politica() != null
                ? solicitud.politica() : new PoliticaDto(null, null, null, null, null);

        return aRespuesta(actualizar.ejecutar(new DatosConfiguracion(
                complejo.nombre(), complejo.direccion(), complejo.telefono(),
                banco.banco(), banco.tipoCuenta(), banco.numeroCuenta(), banco.titular(),
                politica.minutosExpiracion(), politica.horasPlazoCancelacion(), politica.maxReservasEfectivo(),
                politica.diasAnticipacion(), politica.limitarTrasInasistencia())));
    }

    static ConfiguracionRespuesta aRespuesta(ConfiguracionComplejo c) {
        var complejo = c.complejo();
        var politica = c.politica();
        return new ConfiguracionRespuesta(
                new ComplejoDto(complejo.nombre(), complejo.direccion(), complejo.telefono()),
                new DatosBancariosDto(complejo.banco(), complejo.tipoCuenta().name(), complejo.numeroCuenta(),
                        complejo.titularCuenta()),
                new PoliticaDto(politica.minutosExpiracion(), politica.horasPlazoCancelacion(),
                        politica.maxReservasEfectivo(), politica.diasAnticipacion(), politica.limitarTrasInasistencia()),
                c.actualizadaEn());
    }
}
