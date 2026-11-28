package com.canchapp.configuracion.domain;

import com.canchapp.shared.domain.ExcepcionNegocio;

/**
 * RF-03: un dato de la configuración falta o está fuera de rango. El mensaje dice cuál y qué
 * valor se espera, para que la app y el panel lo muestren tal cual.
 */
public class ConfiguracionInvalidaException extends ExcepcionNegocio {

    public ConfiguracionInvalidaException(String mensaje) {
        super("CONFIGURACION_INVALIDA", mensaje, Tipo.DATOS_INVALIDOS);
    }
}
