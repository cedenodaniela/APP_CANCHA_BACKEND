package com.canchapp.configuracion.application;

import com.canchapp.configuracion.domain.Complejo;
import com.canchapp.configuracion.domain.ConfiguracionComplejo;
import com.canchapp.configuracion.domain.ConfiguracionRepository;
import com.canchapp.configuracion.domain.PoliticaReserva;

import java.time.Clock;

/**
 * El administrador cambia los datos del complejo y la política de reservas (RF-03).
 * <p>
 * Reemplaza la configuración completa: así no queda guardada a medias si un valor está fuera de
 * rango. Los cambios aplican a las reservas nuevas; las ya creadas conservan sus plazos.
 *
 * @see "RF-03 · Configuración del complejo"
 */
public class ActualizarConfiguracionUseCase {

    /** Lo que envía el formulario de ajustes, sin validar todavía. */
    public record DatosConfiguracion(String nombre, String direccion, String telefono,
                                     String banco, String tipoCuenta, String numeroCuenta, String titularCuenta,
                                     Integer minutosExpiracion, Integer horasPlazoCancelacion,
                                     Integer maxReservasEfectivo, Integer diasAnticipacion,
                                     Boolean limitarTrasInasistencia) {
    }

    private final ConfiguracionRepository configuraciones;
    private final Clock reloj;

    public ActualizarConfiguracionUseCase(ConfiguracionRepository configuraciones, Clock reloj) {
        this.configuraciones = configuraciones;
        this.reloj = reloj;
    }

    /**
     * @throws com.canchapp.configuracion.domain.ConfiguracionInvalidaException si falta un dato o
     *         un parámetro está fuera de rango; en ese caso no se guarda nada
     */
    public ConfiguracionComplejo ejecutar(DatosConfiguracion datos) {
        Complejo complejo = new Complejo(datos.nombre(), datos.direccion(), datos.telefono(),
                datos.banco(), Complejo.tipoCuenta(datos.tipoCuenta()), datos.numeroCuenta(), datos.titularCuenta());
        PoliticaReserva politica = new PoliticaReserva(datos.minutosExpiracion(), datos.horasPlazoCancelacion(),
                datos.maxReservasEfectivo(), datos.diasAnticipacion(), datos.limitarTrasInasistencia());
        return configuraciones.guardar(new ConfiguracionComplejo(complejo, politica, reloj.instant()));
    }
}
