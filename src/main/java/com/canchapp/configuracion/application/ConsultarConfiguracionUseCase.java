package com.canchapp.configuracion.application;

import com.canchapp.configuracion.domain.ConfiguracionComplejo;
import com.canchapp.configuracion.domain.ConfiguracionRepository;

/**
 * Devuelve la configuración vigente del complejo (RF-03).
 * <p>
 * La consultan el administrador para editarla, el cliente para ver los datos bancarios al pagar
 * (RF-08) y, desde la iteración 2, los casos de uso de reservas y pagos para leer la política.
 *
 * @see "RF-03 · Configuración del complejo"
 */
public class ConsultarConfiguracionUseCase {

    private final ConfiguracionRepository configuraciones;

    public ConsultarConfiguracionUseCase(ConfiguracionRepository configuraciones) {
        this.configuraciones = configuraciones;
    }

    public ConfiguracionComplejo ejecutar() {
        return configuraciones.obtener();
    }
}
